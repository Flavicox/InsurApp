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
    val reserveId: Int,
    val bookingDate: String,
    val timetableStart: String,
    val timetableEnd: String,
    val totalPrice: Double,
    val fieldId: ReservationField,
    val userId: Int,
    val qrUrl: String,
    val isValidated: Boolean
) : Serializable
