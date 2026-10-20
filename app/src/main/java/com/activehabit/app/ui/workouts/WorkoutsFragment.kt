package com.activehabit.app.ui.workouts

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.activehabit.app.R
import com.activehabit.app.network.RetrofitClient
import kotlinx.coroutines.launch

class WorkoutsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_workouts,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        loadWorkouts()

        val btnAddWorkout = view.findViewById<View>(
            R.id.btnAddWorkout
        )

        btnAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }
    }

    private fun getToken(): String? {
        val prefs = requireContext().getSharedPreferences(
            "ActiveHabitPrefs",
            android.content.Context.MODE_PRIVATE
        )
        return prefs.getString("auth_token", null)
    }

    private fun loadWorkouts() {
        val container = view?.findViewById<LinearLayout>(
            R.id.workoutsContainer
        ) ?: return

        val token = getToken()

        if (token.isNullOrEmpty()) {
            container.removeAllViews()
            showMessage(container, "Необходимо войти в аккаунт")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getWorkouts(
                    "Bearer $token"
                )

                container.removeAllViews()

                if (response.workouts.isEmpty()) {
                    showMessage(container, "У вас пока нет тренировок")
                } else {
                    response.workouts.forEach { workout ->

                        val card = LinearLayout(requireContext())
                        card.orientation = LinearLayout.VERTICAL
                        card.setPadding(32, 28, 32, 28)

                        val background = GradientDrawable()
                        background.setColor(Color.WHITE)
                        background.cornerRadius = 24f
                        card.background = background

                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.bottomMargin = 16

                        container.addView(card, params)

                        val title = TextView(requireContext())
                        title.text = workout.workoutType
                        title.textSize = 18f
                        title.setTextColor(Color.rgb(37, 35, 58))
                        card.addView(title)

                        val duration = TextView(requireContext())
                        duration.text =
                            "Продолжительность: ${workout.durationMinutes} мин"
                        duration.textSize = 15f
                        card.addView(duration)

                        val date = TextView(requireContext())
                        date.text = "Дата: ${workout.workoutDate.take(10)}"
                        date.textSize = 14f
                        card.addView(date)
                    }
                }

            } catch (error: Exception) {
                container.removeAllViews()
                showMessage(
                    container,
                    "Не удалось загрузить тренировки"
                )
            }
        }
    }

    private fun showMessage(
        container: LinearLayout,
        message: String
    ) {
        val textView = TextView(requireContext())
        textView.text = message
        textView.textSize = 16f
        textView.setPadding(16, 16, 16, 16)
        container.addView(textView)
    }
    private fun showAddWorkoutDialog() {
        val context = requireContext()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
        }

        val typeInput = android.widget.EditText(context).apply {
            hint = "Вид тренировки (например, бег)"
            setSingleLine(true)
        }

        val durationInput = android.widget.EditText(context).apply {
            hint = "Продолжительность в минутах"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        container.addView(typeInput)
        container.addView(durationInput)

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Новая тренировка")
            .setView(container)
            .setPositiveButton("Добавить") { _, _ ->
                val workoutType = typeInput.text.toString().trim()
                val duration = durationInput.text.toString().toIntOrNull()

                if (workoutType.isEmpty() || duration == null || duration <= 0) {
                    android.widget.Toast.makeText(
                        context,
                        "Введите вид тренировки и время больше 0",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    createWorkout(workoutType, duration)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    private fun createWorkout(
        workoutType: String,
        durationMinutes: Int
    ) {
        val token = getToken()

        if (token.isNullOrEmpty()) {
            android.widget.Toast.makeText(
                requireContext(),
                "Необходимо войти в аккаунт",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = com.activehabit.app.network.CreateWorkoutRequest(
                    workoutType = workoutType,
                    durationMinutes = durationMinutes
                )

                RetrofitClient.apiService.createWorkout(
                    "Bearer $token",
                    request
                )

                android.widget.Toast.makeText(
                    requireContext(),
                    "Тренировка добавлена!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                loadWorkouts()

            } catch (error: Exception) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Не удалось сохранить тренировку",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}