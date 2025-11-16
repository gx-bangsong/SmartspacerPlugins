package com.kieronquinn.app.smartspacer.plugin.qweather.data

import com.google.gson.annotations.SerializedName

data class CityLookupResponse(
    @SerializedName("location") val locations: List<Location>
)

data class Location(
    @SerializedName("id") val id: String
)
