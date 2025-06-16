package com.flavicox.insurapp.model

data class PaymentRequest(
    val amount: Int,
    val clientName: String,
    val productName: String,
    val phone: String,
    val email: String
)

data class PaymentResponse(
    val clientSecret: String,
    val ephemeralSecret: String,
    val id: String,
    val paymentIntentId: String
)
