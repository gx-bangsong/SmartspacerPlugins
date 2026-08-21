package com.kieronquinn.app.smartspacer.plugin.travel.notifications

import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the T-30 "imminent departure" window used to decide between a plain result notification
 * and a promoted Live Update.
 */
class TravelDepartureWindowTest {

    private val departureTime = 1_700_000_000_000L

    private fun trip(departure: Long = departureTime) = TravelInfoItem(
        trainNumber = "G5507",
        departureStation = "A",
        arrivalStation = "B",
        departureTime = departure,
        seat = null,
        passengerName = null,
        source = "sms"
    )

    @Test
    fun `exactly at T-30 the trip is inside the window`() {
        val now = departureTime - TravelNotificationController.DEPARTURE_WINDOW_MS
        assertTrue(trip().isWithinDepartureWindow(now))
    }

    @Test
    fun `just before T-30 the trip is not inside the window yet`() {
        val now = departureTime - TravelNotificationController.DEPARTURE_WINDOW_MS - 1
        assertFalse(trip().isWithinDepartureWindow(now))
    }

    @Test
    fun `days before departure the trip is not inside the window`() {
        val now = departureTime - 7 * 24 * 60 * 60 * 1000L
        assertFalse(trip().isWithinDepartureWindow(now))
    }

    @Test
    fun `after departure the trip is never inside the window`() {
        val now = departureTime + 1
        assertFalse(trip().isWithinDepartureWindow(now))
    }

    @Test
    fun `exactly at departure the trip is still inside the window`() {
        assertTrue(trip().isWithinDepartureWindow(departureTime))
    }
}
