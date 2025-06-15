// File: com/flavicox/insurapp/model/ReservationModels.kt
package com.flavicox.insurapp.model

import java.io.Serializable

data class CreateReserveRequest(
    val bookingDate: String,
    val timetableStart: String,
    val timetableEnd: String,
    val totalPrice: Double,
    val fieldId: Int
)

data class ReservationField(
    val fieldId: Int,
    val price: Double,
    val typeField: String,
    val numberField: Int
) : Serializable

data class ReservationResponse(
    val bookingDate: String,
    val timetableStart: String,
    val timetableEnd: String,
    val totalPrice: Double,
    val qrUrl: String,
    val status: String,
    val isValidated: Boolean,
    val field: ReservationField,
    val user: ReservationUser,
    val payments: List<PaymentModel>
) : Serializable

data class ReservationUser(
    val name: String,
    val surname: String,
    val email: String,
    val phone: String,
    val dni: String
) : Serializable

data class PaymentModel(
    val paymentId: Int,
    val amount: Double,
    val date: String,
    // agrega otros campos si tu API los envía
) : Serializable