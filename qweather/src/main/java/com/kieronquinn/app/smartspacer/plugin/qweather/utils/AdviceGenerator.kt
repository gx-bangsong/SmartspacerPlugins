package com.kieronquinn.app.smartspacer.plugin.qweather.utils

import com.kieronquinn.app.smartspacer.plugin.qweather.data.Daily

object AdviceGenerator {

    fun generateSummaryAdvice(dailyList: List<Daily>): Pair<String, String> {
        val suitable = mutableListOf<String>()
        val unsuitable = mutableListOf<String>()

        val suitableKeywords = setOf("适宜", "良好", "舒适", "需要", "较舒适", "极不易发", "基本适宜")
        val unsuitableKeywords = setOf("不宜", "不适宜", "较差", "易发", "较易发", "较不宜")

        for (daily in dailyList) {
            val activity = daily.name.replace("指数", "")
            when {
                suitableKeywords.any { daily.category.contains(it) } -> suitable.add(activity)
                unsuitableKeywords.any { daily.category.contains(it) } -> unsuitable.add(activity)
            }
        }

        val summary = StringBuilder()
        if (suitable.isNotEmpty()) {
            summary.append("宜: ${suitable.joinToString(" ")}")
        }
        if (unsuitable.isNotEmpty()) {
            if (summary.isNotEmpty()) summary.append(" ")
            summary.append("不宜: ${unsuitable.joinToString(" ")}")
        }

        if (summary.isEmpty()) {
            // Fallback to the first item if no summary can be generated
            val firstItem = dailyList.firstOrNull()
            return if (firstItem != null) {
                Pair("${firstItem.name}: ${firstItem.category}", firstItem.text)
            } else {
                Pair("QWeather", "No data available")
            }
        }

        return Pair("生活指数", summary.toString())
    }

    // Kept for reference, but new logic in generateSummaryAdvice is preferred.
    private fun shortenAdvice(daily: Daily): String {
        return when (daily.name) {
            "化妆指数" -> daily.category
            "紫外线指数", "防晒指数" -> {
                val spfRegex = "SPF在[\\d-]+之间".toRegex()
                val paRegex = "PA\\+".toRegex()
                val spf = spfRegex.find(daily.text)?.value
                val pa = paRegex.find(daily.text)?.value
                val result = listOfNotNull(spf, pa).joinToString(", ")
                if (result.isNotBlank()) result else daily.text
            }
            else -> daily.category
        }
    }

    fun generateAdvice(daily: Daily, previousDaily: Daily?): Pair<String, String> {
        val primaryText = "${daily.name}: ${daily.category}"
        var secondaryText = shortenAdvice(daily)

        if (previousDaily != null && daily.type == previousDaily.type) {
            val currentLevel = daily.level.toIntOrNull()
            val previousLevel = previousDaily.level.toIntOrNull()

            if (currentLevel != null && previousLevel != null && currentLevel != previousLevel) {
                val change = if (currentLevel > previousLevel) "more intense" else "less intense"
                secondaryText = "Note: This is ${change} than before. $secondaryText"
            }
        }

        return Pair(primaryText, secondaryText)
    }
}