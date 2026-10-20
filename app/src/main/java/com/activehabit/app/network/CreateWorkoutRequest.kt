package com.activehabit.app.network

data class CreateWorkoutRequest(
    val workoutType: String,
    val durationMinutes: Int
)

data class CreateWorkoutResponse(
    val message: String,
    val workout: WorkoutItem
)


