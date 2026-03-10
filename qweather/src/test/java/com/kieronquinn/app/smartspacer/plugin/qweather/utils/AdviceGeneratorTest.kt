package com.kieronquinn.app.smartspacer.plugin.qweather.utils

import com.kieronquinn.app.smartspacer.plugin.qweather.data.Daily
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AdviceGeneratorTest {

    private val dailyItems = listOf(
        Daily("2024-01-01", "1", "运动指数", "1", "极适宜", "Test"),
        Daily("2024-01-01", "2", "洗车指数", "3", "不宜", "Test"),
        Daily("2024-01-01", "3", "穿衣指数", "5", "舒适", "Test"),
        Daily("2024-01-01", "5", "紫外线指数", "2", "弱", "Test")
    )

    @Test
    fun `generateActivityAdvice without emoji`() {
        val advice = AdviceGenerator.generateActivityAdvice(dailyItems, false)
        assertEquals(listOf("宜:运动", "不宜:洗车"), advice)
    }

    @Test
    fun `generateActivityAdvice with emoji`() {
        val advice = AdviceGenerator.generateActivityAdvice(dailyItems, true)
        assertEquals(listOf("✅ 🏃", "❌ 🚗"), advice)
    }

    @Test
    fun `generateStatusAdvice without emoji`() {
        val advice = AdviceGenerator.generateStatusAdvice(dailyItems, false)
        assertEquals(listOf("穿衣:舒适", "紫外线:弱"), advice)
    }

    @Test
    fun `generateStatusAdvice with emoji`() {
        val advice = AdviceGenerator.generateStatusAdvice(dailyItems, true)
        assertEquals(listOf("👔 舒适", "☀️ 弱"), advice)
    }
}
