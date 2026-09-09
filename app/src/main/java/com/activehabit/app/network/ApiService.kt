package com.activehabit.app.network
import retrofit2.http.GET
interface ApiService{
    @GET("api/health")
    suspend fun getHealth():HealthResponse
}