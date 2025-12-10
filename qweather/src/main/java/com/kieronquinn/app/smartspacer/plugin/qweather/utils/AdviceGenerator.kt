package com.kieronquinn.app.smartspacer.plugin.qweather.utils

import com.kieronquinn.app.smartspacer.plugin.qweather.data.Daily

object AdviceGenerator {

    // Mappings based on the user-provided image
    private val activitySuitability = mapOf(
        "运动" to Pair(setOf("1", "2"), setOf("3", "4")),
        "洗车" to Pair(setOf("1", "2"), setOf("3", "4")),
        "钓鱼" to Pair(setOf("1", "2"), setOf("3")),
        "旅游" to Pair(setOf("1", "2", "3"), setOf("4", "5")),
        "晾晒" to Pair(setOf("1", "2", "3"), setOf("4", "5", "6"))
    )

    private val statusTypes = setOf("穿衣", "紫外线", "化妆")

    /**
     * Generates a summary for activities like sports, car wash, etc.
     * Example: "宜: 洗车 旅游 | 不宜: 运动"
     */
    fun generateActivityAdvice(dailyList: List<Daily>): String? {
        val suitable = mutableListOf<String>()
        val unsuitable = mutableListOf<String>()

        for (daily in dailyList) {
            val activity = daily.name.replace("指数", "")
            val levels = activitySuitability[activity] ?: continue

            when (daily.level) {
                in levels.first -> suitable.add(activity)
                in levels.second -> unsuitable.add(activity)
            }
        }

        val summary = StringBuilder()
        if (suitable.isNotEmpty()) {
            summary.append("宜: ${suitable.joinToString(" ")}")
        }
        if (unsuitable.isNotEmpty()) {
            if (summary.isNotEmpty()) summary.append(" | ")
            summary.append("不宜: ${unsuitable.joinToString(" ")}")
        }

        return if (summary.isNotEmpty()) summary.toString() else null
    }

    /**
     * Generates a summary for statuses like clothing, UV index, etc.
     * Example: "穿衣: 炎热, 紫外线: 弱, 化妆: 保湿"
     */
    fun generateStatusAdvice(dailyList: List<Daily>): String? {
        val statuses = mutableListOf<String>()

        for (daily in dailyList) {
            val statusType = daily.name.replace("指数", "")
            if (statusType in statusTypes) {
                statuses.add("$statusType: ${daily.category}")
            }
        }

        return if (statuses.isNotEmpty()) statuses.joinToString(", ") else null
    }
}
