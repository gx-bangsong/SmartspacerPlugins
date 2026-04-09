package com.kieronquinn.app.smartspacer.plugin.parcel.engine

import com.google.gson.annotations.SerializedName

data class ParsingEngineConfig(
    @SerializedName("rules") val rules: List<ParcelRule>
)

data class ParcelRule(
    @SerializedName("provider") val provider: String,
    @SerializedName("priority") val priority: Int = 0,
    @SerializedName("match_keywords") val matchKeywords: List<String>,
    @SerializedName("rules") val extractionRules: ExtractionRules
)

data class ExtractionRules(
    @SerializedName("pickup_code") val pickupCodeRegex: String,
    @SerializedName("location") val locationRegex: String? = null
)
