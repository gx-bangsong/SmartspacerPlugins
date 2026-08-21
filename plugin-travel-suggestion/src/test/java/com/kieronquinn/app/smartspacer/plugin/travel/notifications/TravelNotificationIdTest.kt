package com.kieronquinn.app.smartspacer.plugin.travel.notifications

import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Pins the notification-ID contract: share operations live in their own namespace, saved trips
 * in another, the same operation/trip always maps to the same ID and the two namespaces never
 * collide.
 */
class TravelNotificationIdTest {

    @Test
    fun `operation ids are stable per operation uuid`() {
        val opId = UUID.randomUUID().toString()
        assertEquals(
            TravelNotificationController.shareOpNotificationId(opId),
            TravelNotificationController.shareOpNotificationId(opId)
        )
    }

    @Test
    fun `operation and trip ids live in different namespaces`() {
        val opId = UUID.randomUUID().toString()
        val op = TravelNotificationController.shareOpNotificationId(opId)
        val trip = TravelNotificationController.tripNotificationId(1)
        assertNotEquals(op, trip)
        assertNotEquals(
            NotificationIds.forEntity(
                NotificationIds.NAMESPACE_TRAVEL_SHARE_OP,
                TravelNotificationController.opIdToRequestCode(opId)
            ),
            NotificationIds.forEntity(NotificationIds.NAMESPACE_TRAVEL_TRIP, 1L)
        )
    }

    @Test
    fun `different operations get different notification ids`() {
        val a = TravelNotificationController.shareOpNotificationId(UUID.randomUUID().toString())
        val b = TravelNotificationController.shareOpNotificationId(UUID.randomUUID().toString())
        assertNotEquals(a, b)
    }

    @Test
    fun `different trips get different notification ids and they are positive`() {
        val ids = (1..200).map { TravelNotificationController.tripNotificationId(it) }
        assertEquals(200, ids.toSet().size)
        ids.forEach { assertTrue(it > 0) }
    }

    @Test
    fun `op id to request code is deterministic and stable`() {
        val opId = "123e4567-e89b-12d3-a456-426614174000"
        assertEquals(
            TravelNotificationController.opIdToRequestCode(opId),
            TravelNotificationController.opIdToRequestCode(opId)
        )
    }

    @Test
    fun `same operation can never collide with a trip id across the used range`() {
        val opId = UUID.randomUUID().toString()
        val op = TravelNotificationController.shareOpNotificationId(opId)
        for (tripId in 1..2000) {
            assertNotEquals(op, TravelNotificationController.tripNotificationId(tripId))
        }
    }
}
