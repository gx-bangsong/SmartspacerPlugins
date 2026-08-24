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
    fun `preference values map to paging modes`() {
        assertEquals(AdvicePaging.NATIVE, AdvicePaging.fromPreference("native"))
        assertEquals(AdvicePaging.WIDGET, AdvicePaging.fromPreference("widget"))
        assertEquals(AdvicePaging.NATIVE, AdvicePaging.fromPreference(null))
        assertEquals(AdvicePaging.NATIVE, AdvicePaging.fromPreference("nope"))
    }

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
    fun `generateActivityAdvice with long list triggers split`() {
        val longItems = listOf(
            Daily("2024-01-01", "1", "运动指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "洗车指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "钓鱼指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "晾晒指数", "1", "极适宜", "Test")
        )
        // "宜:运动 洗车" is length 7
        // "宜:运动 洗车 钓鱼" would be length 10 -> Split!
        val advice = AdviceGenerator.generateActivityAdvice(longItems, false, AdvicePaging.WIDGET)
        assertEquals(listOf("宜:运动 洗车", "宜:钓鱼 晾晒"), advice)
    }

    @Test
    fun `generateActivityAdvice with emoji long list triggers split`() {
        val longItems = listOf(
            Daily("2024-01-01", "1", "运动指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "洗车指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "钓鱼指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "晾晒指数", "1", "极适宜", "Test")
        )
        // "✅ 🏃 🚗" -> 2 + 2 + 1 + 2 = 7
        // "✅ 🏃 🚗 🎣" -> 2 + 2 + 1 + 2 + 1 + 2 = 10 -> Split!
        val advice = AdviceGenerator.generateActivityAdvice(longItems, true, AdvicePaging.WIDGET)
        assertEquals(listOf("✅ 🏃 🚗", "✅ 🎣 👕"), advice)
    }

    @Test
    fun `native paging splits at the 12-character threshold`() {
        val longItems = listOf(
            Daily("2024-01-01", "1", "运动指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "洗车指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "钓鱼指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "晾晒指数", "1", "极适宜", "Test")
        )
        // "宜:运动 洗车 钓鱼" is 10; adding " 晾晒" would be 13 > 12.
        val advice = AdviceGenerator.generateActivityAdvice(longItems, false, AdvicePaging.NATIVE)
        assertEquals(listOf("宜:运动 洗车 钓鱼", "宜:晾晒"), advice)
    }

    @Test
    fun `custom threshold overrides the native preset`() {
        val longItems = listOf(
            Daily("2024-01-01", "1", "运动指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "洗车指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "钓鱼指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "晾晒指数", "1", "极适宜", "Test")
        )
        val advice = AdviceGenerator.generateActivityAdvice(
            longItems, false, AdvicePaging.NATIVE, maxChars = 16
        )
        assertEquals(listOf("宜:运动 洗车 钓鱼 晾晒"), advice)
    }

    @Test
    fun `widget paging still splits at eight units`() {
        val longItems = listOf(
            Daily("2024-01-01", "1", "运动指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "洗车指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "钓鱼指数", "1", "极适宜", "Test"),
            Daily("2024-01-01", "1", "晾晒指数", "1", "极适宜", "Test")
        )
        val advice = AdviceGenerator.generateActivityAdvice(longItems, false, AdvicePaging.WIDGET)
        assertEquals(listOf("宜:运动 洗车", "宜:钓鱼 晾晒"), advice)
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
