package com.kieronquinn.app.smartspacer.plugin.qweather.utils

import com.kieronquinn.app.smartspacer.plugin.qweather.data.Daily

object AdviceGenerator {

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
            else -> {
                val goodKeywords = setOf("适宜", "良好", "舒适", "需要", "较舒适")
                val badKeywords = setOf("不宜", "不适宜", "较差", "易发", "较易发", "较少开启")
                goodKeywords.forEach {
                    if (daily.category.contains(it)) return "[宜：]${daily.text}"
                }
                badKeywords.forEach {
                    if (daily.category.contains(it)) return "[不宜：]${daily.text}"
                }
                daily.text
            }
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