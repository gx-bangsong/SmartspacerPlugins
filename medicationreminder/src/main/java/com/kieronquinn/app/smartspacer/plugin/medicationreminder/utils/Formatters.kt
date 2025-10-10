package com.kieronquinn.app.smartspacer.plugin.medicationreminder.utils

import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun Long.formatDate(): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(this))
}

fun LocalTime.formatTime(): String {
    return this.format(DateTimeFormatter.ofPattern("h:mm a"))
}