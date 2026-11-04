package com.activehabit.app.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.activehabit.app.R
import com.activehabit.app.databinding.FragmentLoginBinding
import com.activehabit.app.network.RequestLogin
import com.activehabit.app.network.RetrofitClient
import com.activehabit.app.validators.getEmailValidationError
import com.activehabit.app.validators.getPasswordValidationError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(
                R.id.action_login_to_register
            )
        }
    }

    private fun login() {
        val email = binding.etEmail.text
            .toString()
            .trim()

        val password = binding.etPassword.text
            .toString()

        binding.etEmail.error = null
        binding.etPassword.error = null

        val emailError = getEmailValidationError(email)
        val passwordError = getPasswordValidationError(password)

        if (emailError != null) {
            binding.etEmail.error = emailError
        }

        if (passwordError != null) {
            binding.etPassword.error = passwordError
        }

        if (emailError != null || passwordError != null) {
            return
        }

        binding.btnLogin.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val requestLogin = RequestLogin(
                    email = email,
                    password = password
                )

                val loginResponse = loginWithRetry(requestLogin)

                val sharedPreferences =
                    requireContext().getSharedPreferences(
                        "ActiveHabitPrefs",
                        Context.MODE_PRIVATE
                    )

                val tokenSaved = sharedPreferences.edit()
                    .putString(
                        "auth_token",
                        loginResponse.token
                    )
                    .commit()

                if (!tokenSaved) {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось сохранить данные входа",
                        Toast.LENGTH_LONG
                    ).show()

                    binding.btnLogin.isEnabled = true
                    return@launch
                }

                Toast.makeText(
                    requireContext(),
                    "Вход выполнен успешно",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigate(
                    R.id.action_login_to_home
                )

            } catch (error: CancellationException) {
                throw error

            } catch (error: HttpException) {
                showLoginError(getHttpErrorMessage(error))

            } catch (error: IOException) {
                showLoginError(
                    "Сервер недоступен. Проверьте интернет и попробуйте ещё раз"
                )

            } catch (error: Exception) {
                showLoginError("Не удалось выполнить вход")
            }
        }
    }

    private suspend fun loginWithRetry(
        requestLogin: RequestLogin
    ) = try {
        RetrofitClient.apiService.login(requestLogin)
    } catch (error: IOException) {
        delay(1500)
        RetrofitClient.apiService.login(requestLogin)
    }

    private fun getHttpErrorMessage(error: HttpException): String {
        val serverMessage = try {
            val errorText = error.response()
                ?.errorBody()
                ?.string()

            if (errorText.isNullOrBlank()) {
                null
            } else {
                JSONObject(errorText).optString("message")
            }
        } catch (_: Exception) {
            null
        }

        if (!serverMessage.isNullOrBlank()) {
            return serverMessage
        }

        return when (error.code()) {
            401 -> "Неверный email или пароль"
            404 -> "Сервер не нашёл запрос входа"
            in 500..599 -> "Ошибка сервера. Попробуйте немного позже"
            else -> "Ошибка входа: код ${error.code()}"
        }
    }

    private fun showLoginError(message: String) {
        if (_binding == null) {
            return
        }

        binding.btnLogin.isEnabled = true

        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
