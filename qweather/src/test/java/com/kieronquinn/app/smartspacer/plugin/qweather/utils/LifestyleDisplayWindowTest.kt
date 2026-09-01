package com.kieronquinn.app.smartspacer.plugin.qweather.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LifestyleDisplayWindowTest {

    @Test
    fun `always visible when both windows are off`() {
        assertTrue(
            LifestyleDisplayWindow.isVisible(
                nowMinutesOfDay = 3 * 60,
                bedtimeEnabled = false,
                bedtimeStartMinutes = 22 * 60,
                wakeEnabled = false,
                wakeStartMinutes = 7 * 60,
                durationMinutes = 60
            )
        )
    }

    @Test
    fun `bedtime window shows then hides`() {
        assertTrue(
            LifestyleDisplayWindow.isVisible(
                nowMinutesOfDay = 22 * 60 + 15,
                bedtimeEnabled = true,
                bedtimeStartMinutes = 22 * 60,
                wakeEnabled = false,
                wakeStartMinutes = 7 * 60,
                durationMinutes = 60
            )
        )
        assertFalse(
            LifestyleDisplayWindow.isVisible(
                nowMinutesOfDay = 23 * 60 + 15,
                bedtimeEnabled = true,
                bedtimeStartMinutes = 22 * 60,
                wakeEnabled = false,
                wakeStartMinutes = 7 * 60,
                durationMinutes = 60
            )
        )
    }

    @Test
    fun `window wrapping midnight stays visible after 00 00`() {
        assertTrue(
            LifestyleDisplayWindow.inWindow(
                nowMinutesOfDay = 23 * 60 + 45,
                startMinutes = 23 * 60 + 30,
                durationMinutes = 60
            )
        )
        assertTrue(
            LifestyleDisplayWindow.inWindow(
                nowMinutesOfDay = 15,
                startMinutes = 23 * 60 + 30,
                durationMinutes = 60
            )
        )
        assertFalse(
            LifestyleDisplayWindow.inWindow(
                nowMinutesOfDay = 45,
                startMinutes = 23 * 60 + 30,
                durationMinutes = 60
            )
        )
    }

    @Test
    fun `clock formatting is zero padded`() {
        assertEquals("07:05", LifestyleDisplayWindow.formatClock(7 * 60 + 5))
        assertEquals("22:00", LifestyleDisplayWindow.formatClock(22 * 60))
    }
}
