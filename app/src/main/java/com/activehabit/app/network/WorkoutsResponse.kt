package com.activehabit.app.network

data class WorkoutsResponse(
    val workouts: List<WorkoutItem>
)

data class WorkoutItem(
    val id: Int,
    val workoutType: String,
    val durationMinutes: Int,
    val workoutDate: String,
    val distanceMeters: Int? = null,
    val createdAt: String
)