package com.kieronquinn.app.smartspacer.plugin.qweather.utils

import com.kieronquinn.app.smartspacer.plugin.qweather.data.Daily

object AdviceGenerator {

    // [宜] Activities based on level
    private val GOOD_ACTIVITIES = mapOf(
        "运动指数" to setOf("1", "2"),
        "洗车指数" to setOf("1", "2"),
        "钓鱼指数" to setOf("1", "2"),
        "旅游指数" to setOf("1", "2", "3"),
        "晾晒指数" to setOf("1", "2", "3")
    )

    // [不宜] Activities based on level
    private val BAD_ACTIVITIES = mapOf(
        "运动指数" to setOf("3"),
        "洗车指数" to setOf("3", "4"),
        "钓鱼指数" to setOf("3"),
        "旅游指数" to setOf("4", "5"),
        "晾晒指数" to setOf("4", "5", "6")
    )

    // Status advice based on type and level
    private val STATUS_MAP = mapOf(
        "穿衣指数" to mapOf(
            "1" to "寒冷", "2" to "冷", "3" to "较冷", "4" to "较舒适",
            "5" to "舒适", "6" to "热", "7" to "炎热"
        ),
        "紫外线指数" to mapOf(
            "1" to "最弱", "2" to "弱", "3" to "中等", "4" to "强", "5" to "很强"
        ),
        "化妆指数" to mapOf(
            "1" to "保湿", "2" to "保湿防晒", "3" to "去油防晒", "4" to "防脱水防晒",
            "5" to "去油", "6" to "防脱水", "7" to "防晒", "8" to "滋润保湿"
        )
    )

    /**
     * Generates a summary of "Good for" and "Bad for" activities.
     */
    fun generateActivityAdvice(dailyItems: List<Daily>): String? {
        val goodFor = mutableListOf<String>()
        val badFor = mutableListOf<String>()

        dailyItems.forEach { daily ->
            val activityName = daily.name.replace("指数", "")
            if (GOOD_ACTIVITIES[daily.name]?.contains(daily.level) == true) {
                goodFor.add(activityName)
            } else if (BAD_ACTIVITIES[daily.name]?.contains(daily.level) == true) {
                badFor.add(activityName)
            }
        }

        if (goodFor.isEmpty() && badFor.isEmpty()) {
            return null
        }

        val goodStr = if (goodFor.isNotEmpty()) "宜: ${goodFor.joinToString("、 ")}" else ""
        val badStr = if (badFor.isNotEmpty()) "不宜: ${badFor.joinToString("、 ")}" else ""

        return listOf(goodStr, badStr).filter { it.isNotBlank() }.joinToString(" | ")
    }

    /**
     * Generates a summary of status-based advice (clothing, UV, makeup).
     */
    fun generateStatusAdvice(dailyItems: List<Daily>): String? {
        val statusList = mutableListOf<String>()

        dailyItems.forEach { daily ->
            STATUS_MAP[daily.name]?.get(daily.level)?.let { category ->
                val prefix = daily.name.replace("指数", "")
                statusList.add("$prefix: $category")
            }
        }

        return if (statusList.isEmpty()) null else statusList.joinToString("， ")
    }
}
