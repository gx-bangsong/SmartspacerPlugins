package com.kieronquinn.app.smartspacer.plugin.qweather.utils

import com.kieronquinn.app.smartspacer.plugin.qweather.data.Daily

object AdviceGenerator {

    fun generateAdvice(daily: Daily, previousDaily: Daily?): Pair<String, String> {
        val primaryText = "${daily.name}: ${daily.category}"
        var secondaryText = daily.text

        if (previousDaily != null && daily.type == previousDaily.type) {
            val currentLevel = daily.level.toIntOrNull()
            val previousLevel = previousDaily.level.toIntOrNull()

            if (currentLevel != null && previousLevel != null && currentLevel != previousLevel) {
                val change = if (currentLevel > previousLevel) "more intense" else "less intense"
                secondaryText = "Note: This is ${change} than before. ${daily.text}"
            }
        }

        return Pair(primaryText, secondaryText)
    }
}