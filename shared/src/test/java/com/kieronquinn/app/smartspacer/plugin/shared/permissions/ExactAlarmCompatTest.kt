package com.kieronquinn.app.smartspacer.plugin.shared.permissions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExactAlarmCompatTest {

    @Test
    fun `granted exact alarm permission uses the exact path`() {
        assertEquals(ExactAlarmCompat.Path.EXACT, ExactAlarmCompat.path(hasExactPermission = true))
    }

    @Test
    fun `denied exact alarm permission uses the inexact fallback path`() {
        assertEquals(
            ExactAlarmCompat.Path.INEXACT_FALLBACK,
            ExactAlarmCompat.path(hasExactPermission = false)
        )
    }

    @Test
    fun `legacy platforms are treated as having exact permission`() {
        assertTrue(ExactAlarmCompat.hasPermission(sdkInt = 30, canScheduleExactAlarms = false))
        assertFalse(ExactAlarmCompat.hasPermission(sdkInt = 31, canScheduleExactAlarms = false))
        assertTrue(ExactAlarmCompat.hasPermission(sdkInt = 31, canScheduleExactAlarms = true))
    }
}
