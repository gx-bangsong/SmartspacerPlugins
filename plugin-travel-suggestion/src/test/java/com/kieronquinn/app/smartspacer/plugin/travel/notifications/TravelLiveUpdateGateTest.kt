package com.kieronquinn.app.smartspacer.plugin.travel.notifications

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.ExactAlarmCompat
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.plugin.travel.receivers.TravelAlarmReceiver
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelSchedulerImpl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TravelLiveUpdateGateTest {

    private val departure = 1_700_000_000_000L

    private fun trip(id: Int = 7, used: Boolean = false) = TravelInfoItem(
        id = id,
        trainNumber = "G5507",
        departureStation = "A",
        arrivalStation = "B",
        departureTime = departure,
        seat = null,
        passengerName = null,
        source = "manual",
        isUsed = used
    )

    @Test
    fun `posts immediately at T-30`() {
        val now = departure - TravelNotificationController.DEPARTURE_WINDOW_MS
        assertTrue(TravelLiveUpdateGate.shouldPostNow(trip(), now, suppressed = false))
    }

    @Test
    fun `posts immediately inside the departure window`() {
        val now = departure - 10 * 60 * 1000L
        assertTrue(TravelLiveUpdateGate.shouldPostNow(trip(), now, suppressed = false))
    }

    @Test
    fun `does not post before T-30 - the alarm covers that`() {
        val now = departure - TravelNotificationController.DEPARTURE_WINDOW_MS - 1
        assertFalse(TravelLiveUpdateGate.shouldPostNow(trip(), now, suppressed = false))
        val planned = TravelSchedulerImpl.planAlarms(trip(), now, hasExactPermission = true)
        assertTrue(planned.any { it.action == TravelAlarmReceiver.ACTION_REMINDER })
        assertTrue(planned.all { it.path == ExactAlarmCompat.Path.EXACT })
    }

    @Test
    fun `at T-30 the reminder alarm is not scheduled because the update is posted now`() {
        val now = departure - TravelNotificationController.DEPARTURE_WINDOW_MS
        val planned = TravelSchedulerImpl.planAlarms(trip(), now, hasExactPermission = true)
        assertFalse(planned.any { it.action == TravelAlarmReceiver.ACTION_REMINDER })
        assertTrue(planned.any { it.action == TravelAlarmReceiver.ACTION_CLEANUP })
    }

    @Test
    fun `used or suppressed trips never post`() {
        val now = departure - 10 * 60 * 1000L
        assertFalse(TravelLiveUpdateGate.shouldPostNow(trip(used = true), now, suppressed = false))
        assertFalse(TravelLiveUpdateGate.shouldPostNow(trip(), now, suppressed = true))
    }
}
