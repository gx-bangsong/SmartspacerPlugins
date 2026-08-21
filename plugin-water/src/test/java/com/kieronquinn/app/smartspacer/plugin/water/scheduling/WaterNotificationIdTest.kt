package com.kieronquinn.app.smartspacer.plugin.water.scheduling

import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The water reminder notification ID is a stable function of the reminder timestamp, so the same
 * reminder updates in place and different reminders never collide.
 */
class WaterNotificationIdTest {

    @Test
    fun `same reminder time maps to the same notification id`() {
        val t = 1_700_000_000_000L
        assertEquals(
            NotificationIds.forEntity(NotificationIds.NAMESPACE_WATER, t),
            NotificationIds.forEntity(NotificationIds.NAMESPACE_WATER, t)
        )
    }

    @Test
    fun `different reminder times map to different ids`() {
        val t1 = 1_700_000_000_000L
        val t2 = 1_700_000_006_000L // one minute later
        assertNotEquals(
            NotificationIds.forEntity(NotificationIds.NAMESPACE_WATER, t1),
            NotificationIds.forEntity(NotificationIds.NAMESPACE_WATER, t2)
        )
    }

    @Test
    fun `water ids never collide with travel or parcel namespaces`() {
        val water = NotificationIds.forEntity(NotificationIds.NAMESPACE_WATER, 123L)
        val travel = NotificationIds.forEntity(NotificationIds.NAMESPACE_TRAVEL_TRIP, 123L)
        val parcel = NotificationIds.forEntity(NotificationIds.NAMESPACE_PARCEL, 123L)
        assertTrue(water > 0)
        assertNotEquals(water, travel)
        assertNotEquals(water, parcel)
    }
}
