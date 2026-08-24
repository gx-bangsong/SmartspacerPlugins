package com.kieronquinn.app.smartspacer.plugin.water.notifications

import kotlin.math.ceil

data class WaterProgress(
    val drankMl: Int,
    val goalMl: Int,
    val cupMl: Int
) {
    val percent: Int = if (goalMl > 0) ((drankMl * 100L) / goalMl).toInt().coerceIn(0, 100) else 0
    val cupsDrunk: Int = if (cupMl > 0) drankMl / cupMl else 0
    val cupsTotal: Int = if (cupMl > 0) ceil(goalMl.toDouble() / cupMl).toInt().coerceAtLeast(1) else 1
    val reached: Boolean = goalMl > 0 && drankMl >= goalMl

    /** Status-chip text: compact fraction, no unit label. */
    val capsule: String = "$drankMl/$goalMl"
}
