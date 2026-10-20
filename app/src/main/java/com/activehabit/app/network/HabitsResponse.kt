package com.activehabit.app.network

data class HabitsResponse(
    val habits: List<HabitItem>
)

data class HabitItem(
    val id: Int,
    val title: String,
    val createdAt: String,
    val completedToday: Boolean = false,
    val streak: Int = 0
)