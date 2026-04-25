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
        ),
        "感冒指数" to mapOf(
            "1" to "少发", "2" to "较易发", "3" to "易发", "4" to "极易发"
        )
    )

    private val EMOJI_MAP = mapOf(
        "运动" to "🏃",
        "洗车" to "🚗",
        "钓鱼" to "🎣",
        "旅游" to "✈️",
        "晾晒" to "👕",
        "穿衣" to "👔",
        "紫外线" to "☀️",
        "化妆" to "💄",
        "感冒" to "🤒"
    )

    /**
     * Generates a list of summaries for "Good for" and "Bad for" activities.
     */
    fun generateActivityAdvice(dailyItems: List<Daily>, useEmoji: Boolean): List<String> {
        val goodFor = mutableListOf<String>()
        val badFor = mutableListOf<String>()

        dailyItems.forEach { daily ->
            val activityName = daily.name.replace("指数", "")
            if (GOOD_ACTIVITIES[daily.name]?.contains(daily.level) == true) {
                goodFor.add(if (useEmoji) EMOJI_MAP[activityName] ?: activityName else activityName)
            } else if (BAD_ACTIVITIES[daily.name]?.contains(daily.level) == true) {
                badFor.add(if (useEmoji) EMOJI_MAP[activityName] ?: activityName else activityName)
            }
        }

        val result = mutableListOf<String>()
        if (useEmoji) {
            result.addAll(splitAdvice("✅ ", goodFor))
            result.addAll(splitAdvice("❌ ", badFor))
        } else {
            result.addAll(splitAdvice("宜:", goodFor))
            result.addAll(splitAdvice("不宜:", badFor))
        }
        return result
    }

    private fun splitAdvice(prefix: String, items: List<String>): List<String> {
        if (items.isEmpty()) return emptyList()
        val result = mutableListOf<String>()
        var currentBuilder = StringBuilder(prefix)
        items.forEach { item ->
            val potential = if (currentBuilder.length == prefix.length) item else " $item"
            if (currentBuilder.length + potential.length > 8) {
                if (currentBuilder.length > prefix.length) {
                    result.add(currentBuilder.toString())
                    currentBuilder = StringBuilder(prefix).append(item)
                } else {
                    // Even a single item is too long
                    currentBuilder.append(item)
                    result.add(currentBuilder.toString())
                    currentBuilder = StringBuilder(prefix)
                }
            } else {
                currentBuilder.append(potential)
            }
        }
        if (currentBuilder.length > prefix.length) {
            result.add(currentBuilder.toString())
        }
        return result
    }

    /**
     * Generates a list of status-based advice (clothing, UV, makeup, flu).
     */
    fun generateStatusAdvice(dailyItems: List<Daily>, useEmoji: Boolean): List<String> {
        val statusList = mutableListOf<String>()

        dailyItems.forEach { daily ->
            STATUS_MAP[daily.name]?.get(daily.level)?.let { category ->
                val prefix = daily.name.replace("指数", "")
                if (useEmoji && EMOJI_MAP.containsKey(prefix)) {
                    statusList.add("${EMOJI_MAP[prefix]} $category")
                } else {
                    statusList.add("$prefix:$category")
                }
            }
        }

        return statusList
    }
}
