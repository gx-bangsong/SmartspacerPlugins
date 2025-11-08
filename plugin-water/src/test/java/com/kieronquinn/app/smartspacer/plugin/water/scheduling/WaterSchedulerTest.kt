package com.kieronquinn.app.smartspacer.plugin.water.scheduling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class WaterSchedulerTest {

    private val scheduler = WaterScheduler()
    private val testDate = LocalDate.of(2023, 1, 1)

    @Test
    fun `computeDailySchedule returns correct number of reminders`() {
        val reminders = scheduler.computeDailySchedule(
            date = testDate,
            startMinutes = 8 * 60,
            endMinutes = 22 * 60,
            totalCups = 8,
            centerInWindow = true
        )
        assertEquals(8, reminders.size)
    }

    @Test
    fun `computeDailySchedule returns empty list for zero cups`() {
        val reminders = scheduler.computeDailySchedule(
            date = testDate,
            startMinutes = 8 * 60,
            endMinutes = 22 * 60,
            totalCups = 0,
            centerInWindow = true
        )
        assertTrue(reminders.isEmpty())
    }

    @Test
    fun `computeDailySchedule reminders are within active hours`() {
        val startMinutes = 9 * 60
        val endMinutes = 17 * 60
        val reminders = scheduler.computeDailySchedule(
            date = testDate,
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            totalCups = 5,
            centerInWindow = true
        )
        val startTs = testDate.atStartOfDay(ZoneId.systemDefault()).plusMinutes(startMinutes.toLong()).toInstant().toEpochMilli()
        val endTs = testDate.atStartOfDay(ZoneId.systemDefault()).plusMinutes(endMinutes.toLong()).toInstant().toEpochMilli()

        reminders.forEach {
            assertTrue(it in startTs..endTs)
        }
    }
}