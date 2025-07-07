package com.flavicox.insurapp.network

// ─────────────────────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────────────────────
import com.flavicox.insurapp.model.*
import com.flavicox.insurapp.model.Field
import retrofit2.Response
import retrofit2.http.*

// ─────────────────────────────────────────────────────────────
// Interface: AuthApiService
// Define todos los endpoints de red para la autenticación,
// gestión de perfil, reservas, pagos y campos deportivos.
// Utiliza Retrofit para la comunicación con el backend.
// ─────────────────────────────────────────────────────────────
interface AuthApiService {

    // ─── Autenticación ──────────────────────────────────────

    /**
     * Registro de usuario con datos básicos.
     */
    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<Unit>

    /**
     * Validación de código de verificación enviado al correo.
     */
    @GET("api/auth/validate-code/{code}")
    suspend fun validateCode(@Path("code") code: String): Response<Unit>

    /**
     * Inicio de sesión con email y contraseña.
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // ─── Perfil de Usuario ──────────────────────────────────

    /**
     * Obtiene el perfil actual del usuario autenticado.
     */
    @GET("api/users/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): UserProfile

    /**
     * Actualiza los campos del perfil del usuario.
     */
    @PATCH("api/users/profile")
    suspend fun updateUserProfile(
        @Body updatedProfile: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<Unit>

    /**
     * Cambia la contraseña del usuario.
     */
    @PATCH("api/users/password")
    suspend fun updatePassword(
        @Body passwordBody: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<Unit>

    // ─── Campos Deportivos ──────────────────────────────────

    /**
     * Retorna todos los campos disponibles.
     */
    @GET("api/fields/search")
    suspend fun getAvailableFields(@Header("Authorization") token: String): List<Field>

    /**
     * Retorna los horarios disponibles para un campo específico.
     */
    @GET("/api/fields/{fieldId}/available-times")
    suspend fun getAvailableTimes(
        @Path("fieldId") fieldId: Int,
        @Query("bookingDate") bookingDate: String,
        @Header("Authorization") token: String
    ): List<TimeSlot>

    /**
     * Retorna los datos de un campo por su ID.
     */
    @GET("api/fields/{fieldId}")
    suspend fun getFieldById(
        @Path("fieldId") fieldId: Int,
        @Header("Authorization") token: String
    ): Field

    // ─── Reservas ───────────────────────────────────────────

    /**
     * Crea una nueva reserva de campo.
     */
    @POST("api/reservations/createReserve")
    suspend fun createReservation(
        @Body request: CreateReserveRequest,
        @Header("Authorization") token: String
    ): ReservationResponse

    /**
     * Retorna todas las reservas hechas por el usuario.
     */
    @GET("api/users/my")
    suspend fun getMyReservations(
        @Header("Authorization") token: String
    ): List<ReservationResponse>

    /**
     * Valida la información de una reserva (GET).
     */
    @GET("api/reservations/validate-info")
    suspend fun validateReservation(
        @Query("id") id: Int,
        @Header("Authorization") token: String
    ): ValidateReservationResponse

    /**
     * Marca una reserva como validada (PATCH).
     */
    @PATCH("api/reservations/validate/{id}")
    suspend fun validateReservationPatch(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): ValidateReservationResponse

    /**
     * Retorna los datos completos de una reserva específica.
     */
    @GET("api/reservations/{id}")
    suspend fun getReservationById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): ReservationByIdResponse

    // ─── Pagos ──────────────────────────────────────────────

    /**
     * Inicia el proceso de pago para una reserva.
     */
    @POST("api/payment/reserve/{id}")
    suspend fun initiatePayment(
        @Path("id") reservationId: Int,
        @Body body: PaymentRequest,
        @Header("Authorization") token: String
    ): PaymentResponse
}
