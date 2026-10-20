package com.activehabit.app.ui.profile
import androidx.navigation.fragment.findNavController
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.activehabit.app.R
import com.activehabit.app.network.RetrofitClient
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_profile,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = view.findViewById<View>(R.id.btnLogout)

        btnLogout.setOnClickListener {

            // Удаляем сохранённый токен
            val preferences = requireContext().getSharedPreferences(
                "ActiveHabitPrefs",
                Context.MODE_PRIVATE
            )

            preferences.edit()
                .remove("auth_token")
                .apply()

            // Возвращаемся на экран входа
            findNavController().navigate(R.id.action_profile_to_login)
        }

        // Получаем сохранённый токен
        val sharedPreferences = requireContext().getSharedPreferences(
            "ActiveHabitPrefs",
            Context.MODE_PRIVATE
        )

        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            tvName.text = "Не авторизован"
            tvEmail.text = "Необходимо войти в аккаунт"
            return
        }

        // Запрашиваем данные профиля с сервера
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val profileResponse = RetrofitClient.apiService.getProfile(
                    "Bearer $token"
                )

                tvName.text = profileResponse.user.name
                tvEmail.text = profileResponse.user.email

            } catch (error: Exception) {
                tvName.text = "Ошибка загрузки"
                tvEmail.text = "Не удалось получить данные"

                Toast.makeText(
                    requireContext(),
                    "Ошибка загрузки профиля",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}