package com.kieronquinn.app.smartspacer.plugin.parcel.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class RuleItem(
    @PrimaryKey val provider: String,
    val priority: Int,
    val matchKeywords: String, // Comma separated
    val pickupCodeRegex: String,
    val locationRegex: String?,
    val isCustom: Boolean = true
)
