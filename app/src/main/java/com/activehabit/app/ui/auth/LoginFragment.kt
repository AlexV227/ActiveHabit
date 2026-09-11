package com.activehabit.app.ui.auth
import androidx.navigation.fragment.findNavController
import com.activehabit.app.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.activehabit.app.validators.getEmailValidationError
import androidx.lifecycle.lifecycleScope
import com.activehabit.app.network.RetrofitClient
import kotlinx.coroutines.launch
import com.activehabit.app.databinding.FragmentLoginBinding
import android.util.Patterns
import android.widget.Toast
import com.activehabit.app.validators.getPasswordValidationError
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {




            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            binding.etEmail.error = null
            binding.etPassword.error = null
            var isValid = true
                /*
            if (email.isBlank()) {
                binding.etEmail.error = "Введите email"
                isValid = false
            }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Введите корректный email"
                isValid = false



            }
*/
            val emailError = getEmailValidationError(email)

            if (emailError != null) {
                binding.etEmail.error = emailError
                isValid = false
            }
            val passwordError = getPasswordValidationError(password)

            if (passwordError != null) {
                binding.etPassword.error = passwordError
                isValid = false
            }
            if (!isValid) {
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val healthResponse = RetrofitClient.apiService.getHealth()

                    Toast.makeText(
                        requireContext(),
                        "Server: ${healthResponse.status}, db: ${healthResponse.database}",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (error: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось подключиться к серверу",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        /* новый пароль*/
/*
           if (password.isBlank()) {
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
            } старый пароль
*/

            }

            binding.tvRegister.setOnClickListener {
                findNavController().navigate(R.id.action_login_to_register)
            }
        }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}