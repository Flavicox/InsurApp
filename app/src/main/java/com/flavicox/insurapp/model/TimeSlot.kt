package com.flavicox.insurapp.model

data class TimeSlot(
    val time: String,
    val client: String?,
    val reserved: Boolean
)
