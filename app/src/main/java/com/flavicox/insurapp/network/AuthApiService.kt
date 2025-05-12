package com.flavicox.insurapp.network

import com.flavicox.insurapp.model.LoginRequest
import com.flavicox.insurapp.model.LoginResponse
import com.flavicox.insurapp.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface AuthApiService {

    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<Unit>

    @GET("api/auth/validate-code/{code}")
    suspend fun validateCode(@Path("code") code: String): Response<Unit>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
