// 1. Definir modelos en com/flavicox/insurapp/model/ValidateReservationModels.kt
package com.flavicox.insurapp.model

import java.io.Serializable

data class ValidateReservationResponse(
    val bookingDate: String,
    val timetableStart: String,
    val timetableEnd: String,
    val totalPrice: Double,
    val qrUrl: String,
    val isValidated: Boolean,
    val field: ReservationFieldDetail,
    val user: ReservationUserDetail,
    val payments: List<Any> // Por ahora vacíos, no se usan
) : Serializable

data class ReservationFieldDetail(
    val fieldId: Int,
    val price: Double,
    val typeField: String,
    val numberField: Int
) : Serializable

data class ReservationUserDetail(
    val name: String,
    val surname: String,
    val email: String,
    val phone: String,
    val dni: String
) : Serializable
