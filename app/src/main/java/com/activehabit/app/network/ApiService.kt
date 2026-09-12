package com.activehabit.app.network
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
interface ApiService{
    @GET("api/health")
    suspend fun getHealth():HealthResponse
    @POST("api/auth/login")
    suspend fun login(
        @Body requestLogin: RequestLogin
    ): ResponceLogin
}