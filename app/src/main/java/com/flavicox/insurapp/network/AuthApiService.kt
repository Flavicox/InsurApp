package com.flavicox.insurapp.network

import com.flavicox.insurapp.model.CreateReserveRequest
import com.flavicox.insurapp.model.Field
import com.flavicox.insurapp.model.LoginRequest
import com.flavicox.insurapp.model.LoginResponse
import com.flavicox.insurapp.model.PaymentRequest
import com.flavicox.insurapp.model.PaymentResponse
import com.flavicox.insurapp.model.RegisterRequest
import com.flavicox.insurapp.model.ReservationByIdResponse
import com.flavicox.insurapp.model.ReservationResponse
import com.flavicox.insurapp.model.TimeSlot
import com.flavicox.insurapp.model.UserProfile
import com.flavicox.insurapp.model.ValidateReservationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
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

    // Cambio: ahora devuelve List<TimeSlot>
    @GET("/api/fields/{fieldId}/available-times")
    suspend fun getAvailableTimes(
        @Path("fieldId") fieldId: Int,
        @Query("bookingDate") bookingDate: String,
        @Header("Authorization") token: String
    ): List<TimeSlot>

    @GET("api/fields/{fieldId}")
    suspend fun getFieldById(
        @Path("fieldId") fieldId: Int,
        @Header("Authorization") token: String
    ): Field

    @POST("api/reservations/createReserve")
    suspend fun createReservation(
        @Body request: CreateReserveRequest,
        @Header("Authorization") token: String
    ): ReservationResponse

    @GET("api/reservations/validate-info")
    suspend fun validateReservation(
        @Query("id") id: Int,
        @Header("Authorization") token: String
    ): ValidateReservationResponse

    @PATCH("api/reservations/validate/{id}")
    suspend fun validateReservationPatch(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): ValidateReservationResponse


    @GET("api/users/my")
    suspend fun getMyReservations(
        @Header("Authorization") token: String
    ): List<ReservationResponse>

    @POST("api/payment/reserve/{id}")
    suspend fun initiatePayment(
        @Path("id") reservationId: Int,
        @Body body: PaymentRequest,
        @Header("Authorization") token: String
    ): PaymentResponse

    @PATCH("api/users/profile")
    suspend fun updateUserProfile(
        @Body updatedProfile: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<Unit>

    @PATCH("api/users/password")
    suspend fun updatePassword(
        @Body passwordBody: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("api/reservations/{id}")
    suspend fun getReservationById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): ReservationByIdResponse

}
