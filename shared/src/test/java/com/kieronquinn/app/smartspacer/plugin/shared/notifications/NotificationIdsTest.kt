package com.kieronquinn.app.smartspacer.plugin.shared.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationIdsTest {

    @Test
    fun `same entity always maps to the same id`() {
        val a = NotificationIds.forEntity("travel_trip", 42L)
        val b = NotificationIds.forEntity("travel_trip", 42L)
        assertEquals(a, b)
    }

    @Test
    fun `different entities in the same namespace map to different ids`() {
        val ids = (1..500L).map { NotificationIds.forEntity("travel_trip", it) }.toSet()
        assertEquals(500, ids.size)
    }

    @Test
    fun `different namespaces never collide for the same numeric id`() {
        // The operation namespace and the saved-trip namespace must never overwrite each other.
        val op = NotificationIds.forEntity(NotificationIds.NAMESPACE_TRAVEL_SHARE_OP, 7L)
        val trip = NotificationIds.forEntity(NotificationIds.NAMESPACE_TRAVEL_TRIP, 7L)
        assertNotEquals(op, trip)
        assertFalse(NotificationIds.collides(NotificationIds.NAMESPACE_TRAVEL_SHARE_OP, 7L, NotificationIds.NAMESPACE_TRAVEL_TRIP, 7L))
    }

    @Test
    fun `ids are always positive and never zero`() {
        val ids = listOf(0L, 1L, Long.MAX_VALUE, Long.MIN_VALUE, -1L, 123456789L)
            .map { NotificationIds.forEntity("any_namespace", it) }
        ids.forEach {
            assertTrue("id must be positive, was $it", it > 0)
        }
    }

    @Test
    fun `all app namespaces produce pairwise disjoint id ranges for typical entity ids`() {
        val namespaces = listOf(
            NotificationIds.NAMESPACE_TRAVEL_SHARE_OP,
            NotificationIds.NAMESPACE_TRAVEL_TRIP,
            NotificationIds.NAMESPACE_PARCEL,
            NotificationIds.NAMESPACE_MEDICATION,
            NotificationIds.NAMESPACE_FOOD,
            NotificationIds.NAMESPACE_WATER
        )
        // For every pair of namespaces, no collision across a wide range of entity ids.
        for (i in namespaces.indices) {
            for (j in i + 1 until namespaces.size) {
                val a = namespaces[i]
                val b = namespaces[j]
                for (entity in 0L..300L) {
                    assertFalse(
                        "collision: $a#$entity vs $b#$entity",
                        NotificationIds.collides(a, entity, b, entity)
                    )
                }
            }
        }
    }

    @Test
    fun `int and long overloads agree`() {
        assertEquals(
            NotificationIds.forEntity("parcel", 12L),
            NotificationIds.forEntity("parcel", 12)
        )
    }
}
