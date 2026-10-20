package com.activehabit.app.ui.habits

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.graphics.Color
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.activehabit.app.R
import com.activehabit.app.network.CreateHabitRequest
import com.activehabit.app.network.HabitItem
import com.activehabit.app.network.RetrofitClient
import kotlinx.coroutines.launch

class HabitsFragment : Fragment() {

    private var habits = listOf<HabitItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_habits,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAddHabit = view.findViewById<Button>(R.id.btnAddHabit)

        loadHabits()

        btnAddHabit.setOnClickListener {
            showAddHabitDialog()
        }

        view.findViewById<TextView>(R.id.tvHabitsList)
            .setOnClickListener {
                showHabitsSelectionDialog()
            }
    }

    private fun getToken(): String? {
        val preferences = requireContext().getSharedPreferences(
            "ActiveHabitPrefs",
            Context.MODE_PRIVATE
        )

        return preferences.getString("auth_token", null)
    }

    private fun loadHabits() {
        val token = getToken()

        val container = view?.findViewById<LinearLayout>(
            R.id.habitsContainer
        ) ?: return

        if (token.isNullOrEmpty()) {
            container.removeAllViews()
            val message = TextView(requireContext())
            message.text = "Необходимо войти в аккаунт"
            container.addView(message)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getHabits(
                    "Bearer $token"
                )

                habits = response.habits
                container.removeAllViews()

                if (habits.isEmpty()) {
                    val message = TextView(requireContext())
                    message.text = "У вас пока нет привычек"
                    message.textSize = 16f
                    container.addView(message)
                } else {
                    habits.forEach { habit ->

                        val card = LinearLayout(requireContext())
                        card.orientation = LinearLayout.HORIZONTAL
                        card.gravity = android.view.Gravity.CENTER_VERTICAL
                        card.setPadding(32, 28, 32, 28)
                        val background = android.graphics.drawable.GradientDrawable()
                        background.setColor(Color.WHITE)
                        background.cornerRadius = 24f
                        card.background = background

                        val cardParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        cardParams.bottomMargin = 16
                        container.addView(card, cardParams)

                        val title = TextView(requireContext())
                        title.text = habit.title
                        title.textSize = 17f
                        title.setTextColor(Color.rgb(37, 35, 58))

                        val titleParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                        card.addView(title, titleParams)
                        val streakText = TextView(requireContext())

                        streakText.text = "🔥 ${habit.streak}"
                        streakText.textSize = 14f
                        streakText.setTextColor(Color.rgb(104, 80, 164))
                        streakText.setPadding(12, 0, 12, 0)

                        card.addView(streakText)

                        val status = TextView(requireContext())
                        status.text = if (habit.completedToday) {
                            "✅"
                        } else {
                            "○"
                        }
                        status.textSize = 25f
                        status.setPadding(20, 0, 0, 0)
                        card.addView(status)

                        card.setOnClickListener {
                            if (habit.completedToday) {
                                Toast.makeText(
                                    requireContext(),
                                    "Эта привычка уже выполнена сегодня",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                completeHabit(habit.id)
                            }
                        }
                        card.setOnLongClickListener {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Удалить привычку?")
                                .setMessage("Вы действительно хотите удалить «${habit.title}»?")
                                .setPositiveButton("Удалить") { _, _ ->
                                    deleteHabit(habit.id)
                                }
                                .setNegativeButton("Отмена", null)
                                .show()

                            true
                        }
                    }
                }

            } catch (error: Exception) {
                container.removeAllViews()

                val message = TextView(requireContext())
                message.text = "Не удалось загрузить привычки"
                container.addView(message)

                error.printStackTrace()
            }
        }
    }

    private fun showHabitsSelectionDialog() {
        if (habits.isEmpty()) {
            return
        }

        val titles = habits.map { habit ->
            val mark = if (habit.completedToday) "✅" else "○"
            "$mark ${habit.title}"
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Выберите выполненную привычку")
            .setItems(titles) { _, which ->
                completeHabit(habits[which].id)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun completeHabit(habitId: Int) {
        val token = getToken() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.completeHabit(
                    "Bearer $token",
                    habitId
                )

                Toast.makeText(
                    requireContext(),
                    response.message,
                    Toast.LENGTH_LONG
                ).show()

                // Обновляем статусы после выполнения
                loadHabits()

            } catch (error: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Ошибка отметки привычки",
                    Toast.LENGTH_SHORT
                ).show()

                error.printStackTrace()
            }
        }
    }

    private fun showAddHabitDialog() {
        val input = EditText(requireContext())

        input.hint = "Название привычки"
        input.setSingleLine(true)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Новая привычка")
            .setView(input)
            .setPositiveButton("Сохранить", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val title = input.text.toString().trim()

                    if (title.isEmpty()) {
                        input.error = "Введите название привычки"
                        return@setOnClickListener
                    }

                    if (title.length > 100) {
                        input.error = "Не более 100 символов"
                        return@setOnClickListener
                    }

                    dialog.dismiss()
                    createHabit(title)
                }
        }

        dialog.show()
    }

    private fun createHabit(title: String) {
        val token = getToken() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                RetrofitClient.apiService.createHabit(
                    "Bearer $token",
                    CreateHabitRequest(title)
                )

                Toast.makeText(
                    requireContext(),
                    "Привычка добавлена!",
                    Toast.LENGTH_SHORT
                ).show()

                loadHabits()

            } catch (error: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Не удалось добавить привычку",
                    Toast.LENGTH_SHORT
                ).show()

                error.printStackTrace()
            }
        }
    }
    private fun deleteHabit(habitId: Int) {
        val token = getToken() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteHabit(
                    "Bearer $token",
                    habitId
                )

                Toast.makeText(
                    requireContext(),
                    response.message,
                    Toast.LENGTH_SHORT
                ).show()

                loadHabits()

            } catch (error: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Не удалось удалить привычку",
                    Toast.LENGTH_SHORT
                ).show()

                error.printStackTrace()
            }
        }
    }
}