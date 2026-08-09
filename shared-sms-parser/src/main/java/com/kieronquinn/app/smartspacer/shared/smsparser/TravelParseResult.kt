package com.kieronquinn.app.smartspacer.shared.smsparser

data class TravelParseResult(
    val status: ParseResultStatus,
    val travelInfo: TravelInfo? = null,
    val errorMessage: String? = null
)
