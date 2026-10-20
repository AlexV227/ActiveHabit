package com.activehabit.app.network

data class CreateHabitResponse(
    val message: String,
    val habit: CreatedHabit
)

data class CreatedHabit(
    val id: Int,
    val userId: Int,
    val title: String,
    val createdAt: String
)

