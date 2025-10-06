package com.kieronquinn.app.smartspacer.plugin.qweather.data

import com.google.gson.annotations.SerializedName

data class QWeatherResponse(
    @SerializedName("code")
    val code: String,
    @SerializedName("updateTime")
    val updateTime: String,
    @SerializedName("fxLink")
    val fxLink: String,
    @SerializedName("daily")
    val daily: List<Daily>,
    @SerializedName("refer")
    val refer: Refer
)

data class Daily(
    @SerializedName("date")
    val date: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("level")
    val level: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("text")
    val text: String
)

data class Refer(
    @SerializedName("sources")
    val sources: List<String>,
    @SerializedName("license")
    val license: List<String>
)