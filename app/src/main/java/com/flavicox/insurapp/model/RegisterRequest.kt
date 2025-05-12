package com.flavicox.insurapp.model

data class RegisterRequest(
    val name: String,
    val surname: String,
    val dni: String,
    val phone: String,
    val email: String,
    val password: String
)
