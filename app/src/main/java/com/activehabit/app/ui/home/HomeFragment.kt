package com.activehabit.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.activehabit.app.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Кнопка "Мой профиль"
        val btnProfile = view.findViewById<View>(R.id.btnProfile)

        btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }

        // Кнопка "Привычки"
        val btnHabits = view.findViewById<View>(R.id.btnHabits)

        btnHabits.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_habits)
        }
        // Кнопка "Тренировки"
        val btnWorkouts = view.findViewById<View>(R.id.btnWorkouts)

        btnWorkouts.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_workouts)
        }
        // Кнопка "Статистика"
        val btnStatistics = view.findViewById<View>(R.id.btnStatistics)

        btnStatistics.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_statistics)
        }
        val btnHelp = view.findViewById<View>(R.id.btnHelp)

        btnHelp.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_help)
        }
    }
}