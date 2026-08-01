package com.kieronquinn.app.smartspacer.shared.smsparser

data class TravelInfo(
    val trainNumber: String,
    val departureStation: String,
    val arrivalStation: String?,
    val departureTime: Long, // Epoch milliseconds
    val seat: String?,
    val passengerName: String?,
    val rawText: String
)
