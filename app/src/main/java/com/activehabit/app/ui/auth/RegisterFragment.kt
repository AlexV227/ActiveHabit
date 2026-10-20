
package com.activehabit.app.ui.auth
import androidx.navigation.fragment.findNavController
import com.activehabit.app.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.activehabit.app.databinding.FragmentRegisterBinding
import android.util.Patterns
import com.activehabit.app.validators.getEmailValidationError
import android.widget.Toast
import com.activehabit.app.validators.getPasswordValidationError
import androidx.lifecycle.lifecycleScope
import com.activehabit.app.network.RegisterRequest
import com.activehabit.app.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            binding.etName.error = null
            binding.etEmail.error = null
            binding.etPassword.error = null

            var isValid = true

            if (name.isBlank()) {
                binding.etName.error = "Введите имя"
                isValid = false
            }
            val emailError = getEmailValidationError(email)

            if (emailError != null) {
                binding.etEmail.error = emailError
                isValid = false
            }
            /*

            if (email.isBlank()) {
                binding.etEmail.error = "Введите email"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Введите корректный email"
                isValid = false
            }

 */

            val passwordError = getPasswordValidationError(password)

            if (passwordError != null) {
                binding.etPassword.error = passwordError
                isValid = false
            }

            if (!isValid) {
                return@setOnClickListener
            }


            binding.btnRegister.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.register(
                        RegisterRequest(
                            name = name,
                            email = email,
                            password = password
                        )
                    )

                    Toast.makeText(
                        requireContext(),
                        response.message,
                        Toast.LENGTH_LONG
                    ).show()

                    findNavController().popBackStack()

                } catch (e: HttpException) {
                    val message = when (e.code()) {
                        409 -> "Пользователь с таким email уже существует"
                        400 -> "Проверьте введённые данные"
                        else -> "Ошибка сервера: ${e.code()}"
                    }

                    Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_LONG
                    ).show()

                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось подключиться к серверу",
                        Toast.LENGTH_LONG
                    ).show()

                } finally {
                    _binding?.btnRegister?.isEnabled = true
                }
            }



            /* if (password.isBlank()) {
                binding.etPassword.error = "Введите пароль"
                isValid = false
            } else if (password.length < 6) {
                binding.etPassword.error = "Минимальная длина пароля — 6 символов"
                isValid = false
            } else if (password.length > 15) {
                binding.etPassword.error = "Максимальная длина пароля — 15 символов"
                isValid = false
            } else if (!password.all { character -> character.code in 33..126 }) {
                binding.etPassword.error =
                    "Используйте только латинские буквы, цифры и спецсимволы без пробелов"
                isValid = false
            }
*/

        }

        binding.tvLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}