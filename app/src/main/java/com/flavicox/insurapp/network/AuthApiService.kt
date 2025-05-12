package com.flavicox.insurapp.network

import com.flavicox.insurapp.model.Field
import com.flavicox.insurapp.model.LoginRequest
import com.flavicox.insurapp.model.LoginResponse
import com.flavicox.insurapp.model.RegisterRequest
import com.flavicox.insurapp.model.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface AuthApiService {

    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<Unit>

    @GET("api/auth/validate-code/{code}")
    suspend fun validateCode(@Path("code") code: String): Response<Unit>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/users/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): UserProfile

    @GET("api/fields/search")
    suspend fun getAvailableFields(@Header("Authorization") token: String): List<Field>

    @GET("/api/fields/{fieldId}/available-times")
    suspend fun getAvailableTimes(
        @Path("fieldId") fieldId: Int,
        @Query("bookingDate") bookingDate: String,
        @Header("Authorization") token: String
    ): List<String>
}
