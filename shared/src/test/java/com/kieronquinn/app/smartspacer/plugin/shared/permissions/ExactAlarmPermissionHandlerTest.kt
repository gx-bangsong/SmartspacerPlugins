package com.kieronquinn.app.smartspacer.plugin.shared.permissions

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ExactAlarmPermissionHandlerTest {

    private class FakeRescheduler : ExactAlarmRescheduler {
        var calls = 0
        override suspend fun rescheduleAll() {
            calls++
        }
    }

    @Test
    fun `returning from system settings granted calls rescheduleAll`() = runBlocking {
        val rescheduler = FakeRescheduler()
        val handler = ExactAlarmPermissionHandler(rescheduler)
        handler.onResumeRecheck(nowGranted = false)
        assertEquals(0, rescheduler.calls)
        handler.onResumeRecheck(nowGranted = true)
        assertEquals(1, rescheduler.calls)
    }

    @Test
    fun `exact-alarm permission changed broadcast always reschedules`() = runBlocking {
        val rescheduler = FakeRescheduler()
        val handler = ExactAlarmPermissionHandler(rescheduler)
        handler.onPermissionStateChanged()
        handler.onPermissionStateChanged()
        assertEquals(2, rescheduler.calls)
    }
}
