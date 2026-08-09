package com.kieronquinn.app.smartspacer.shared.smsparser

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class SmsParserTest {
    private lateinit var parser: SmsParser

    @Before
    fun setUp() {
        var rulesFile = File("src/main/assets/travel_sms_rules.json")
        if (!rulesFile.exists()) {
            rulesFile = File("shared-sms-parser/src/main/assets/travel_sms_rules.json")
        }
        assertTrue("travel_sms_rules.json should exist", rulesFile.exists())
        val rulesJson = rulesFile.readText()
        parser = SmsParser(rulesJson)
    }

    @Test
    fun testUser12306Sms() {
        val rawSms = "【12306】邓棚焕购票成功，2月28日G5507次，南宁东站16:28开。铁警提示与详情s.12306.cn/s/y/wHQ4YF"
        val result = parser.parseTravelInfo(rawSms)
        assertEquals(ParseResultStatus.SUCCESS, result.status)
        assertNotNull(result.travelInfo)
        val info = result.travelInfo!!
        assertEquals("G5507", info.trainNumber)
        assertEquals("南宁东站", info.departureStation)
        assertNull(info.arrivalStation)
        assertEquals("邓棚焕", info.passengerName)

        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = info.departureTime
        assertEquals(16, cal.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(28, cal.get(java.util.Calendar.MINUTE))
        assertEquals(1, cal.get(java.util.Calendar.MONTH)) // February is index 1
        assertEquals(28, cal.get(java.util.Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testFlightSms() {
        val flightSms = "【国航】您的航班CA1234，8月1日北京首都-上海虹桥，起飞时间14:30，座位13F。"
        val result = parser.parseTravelInfo(flightSms)
        assertEquals(ParseResultStatus.SUCCESS, result.status)
        assertNotNull(result.travelInfo)
        val info = result.travelInfo!!
        assertEquals("CA1234", info.trainNumber)
        assertEquals("北京首都", info.departureStation)
        assertEquals("上海虹桥", info.arrivalStation)
        assertEquals("13F", info.seat)
    }

    @Test
    fun testManualPasteFormat() {
        val manualText = "G123 北京南 → 上海虹桥 2026-08-01 14:30 05A"
        val result = parser.parseTravelInfo(manualText)
        assertEquals(ParseResultStatus.SUCCESS, result.status)
        assertNotNull(result.travelInfo)
        val info = result.travelInfo!!
        assertEquals("G123", info.trainNumber)
        assertEquals("北京南", info.departureStation)
        assertEquals("上海虹桥", info.arrivalStation)
        assertEquals("05A", info.seat)

        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = info.departureTime
        assertEquals(2026, cal.get(java.util.Calendar.YEAR))
        assertEquals(7, cal.get(java.util.Calendar.MONTH)) // August is 7
        assertEquals(1, cal.get(java.util.Calendar.DAY_OF_MONTH))
        assertEquals(14, cal.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(30, cal.get(java.util.Calendar.MINUTE))
    }

    @Test
    fun testNoMatchSms() {
        val unrelatedText = "【验证码】您的验证码是 123456，请勿泄露给他人。"
        val result = parser.parseTravelInfo(unrelatedText)
        assertEquals(ParseResultStatus.NO_MATCH, result.status)
        assertNull(result.travelInfo)
    }
}
