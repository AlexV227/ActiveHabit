package com.activehabit.app.ui.statistics

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.activehabit.app.R
import com.activehabit.app.network.RetrofitClient
import kotlinx.coroutines.launch

class StatisticsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_statistics,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        loadStatistics(view)
    }

    private fun loadStatistics(view: View) {

        val tvTotalWorkouts =
            view.findViewById<TextView>(R.id.tvTotalWorkouts)

        val tvTotalMinutes =
            view.findViewById<TextView>(R.id.tvTotalMinutes)

        val tvTotalDistance =
            view.findViewById<TextView>(R.id.tvTotalDistance)

        val tvTotalHabits =
            view.findViewById<TextView>(R.id.tvTotalHabits)

        val prefs = requireContext().getSharedPreferences(
            "ActiveHabitPrefs",
            Context.MODE_PRIVATE
        )

        val token = prefs.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            tvTotalWorkouts.text = "Необходимо войти в аккаунт"
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {

            try {
                val workoutsResponse =
                    RetrofitClient.apiService.getWorkouts(
                        "Bearer $token"
                    )

                val workouts = workoutsResponse.workouts

                val totalWorkouts = workouts.size

                val totalMinutes = workouts.sumOf {
                    it.durationMinutes
                }
                val totalDistance = workouts.sumOf {
                    it.distanceMeters ?: 0
                }
                tvTotalWorkouts.text =
                    "Тренировок: $totalWorkouts"

                tvTotalMinutes.text =
                    "Время тренировок: $totalMinutes мин"
                tvTotalDistance.text =
                    "Дистанция бега: $totalDistance м"
            } catch (error: Exception) {
                tvTotalWorkouts.text =
                    "Не удалось загрузить тренировки"
                tvTotalMinutes.text =
                    "Время тренировок: —"
                tvTotalDistance.text =
                    "Дистанция бега: —"
            }

            try {
                val habitsResponse =
                    RetrofitClient.apiService.getHabits(
                        "Bearer $token"
                    )

                val totalHabits = habitsResponse.habits.size

                tvTotalHabits.text =
                    "Полезных привычек: $totalHabits"

            } catch (error: Exception) {
                tvTotalHabits.text =
                    "Не удалось загрузить привычки"
            }
        }
    }
}