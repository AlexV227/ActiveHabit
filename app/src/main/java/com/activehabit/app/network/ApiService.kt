package com.activehabit.app.network

import retrofit2.http.Header
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @GET("api/health")
    suspend fun getHealth(): HealthResponse

    @POST("api/auth/login")
    suspend fun login(
        @Body requestLogin: RequestLogin
    ): ResponceLogin

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

    @GET("api/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): ProfileResponse

    // Получение списка привычек
    @GET("api/habits")
    suspend fun getHabits(
        @Header("Authorization") token: String
    ): HabitsResponse
    @POST("api/habits")
    suspend fun createHabit(
        @Header("Authorization") token: String,
        @Body request: CreateHabitRequest
    ): CreateHabitResponse
    @POST("api/habits/{id}/complete")
    suspend fun completeHabit(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") habitId: Int
    ): CompleteHabitResponse
    @retrofit2.http.DELETE("api/habits/{id}")
    suspend fun deleteHabit(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") habitId: Int
    ): DeleteHabitResponse
    @retrofit2.http.GET("api/workouts")
    suspend fun getWorkouts(
        @retrofit2.http.Header("Authorization") token: String
    ): WorkoutsResponse
    @retrofit2.http.POST("api/workouts")
    suspend fun createWorkout(
        @retrofit2.http.Header("Authorization") token: String,
        @retrofit2.http.Body request: CreateWorkoutRequest
    ): CreateWorkoutResponse
}