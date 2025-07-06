// File: com/flavicox/insurapp/model/ReservationByIdResponse.kt
package com.flavicox.insurapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReservationByIdResponse(
    val reserveId: Int,
    val bookingDate: String,
    val timetableStart: String,
    val timetableEnd: String,
    val totalPrice: Int,

    // 🚩 El backend lo llama fieldId, lo mapeamos al mismo nombre "field"
    @SerializedName("fieldId")
    val field: ReservationField,

    val qrUrl: String,
    val isValidated: Boolean,
    val status: String
) : Serializable
