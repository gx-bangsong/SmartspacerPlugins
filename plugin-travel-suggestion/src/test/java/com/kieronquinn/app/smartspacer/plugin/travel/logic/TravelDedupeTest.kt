package com.kieronquinn.app.smartspacer.plugin.travel.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TravelDedupeTest {

    private val existing = listOf(
        TripKey("G5507", 1_700_000_000_000L),
        TripKey("CA1234", 1_700_500_000_000L)
    )

    @Test
    fun `same train and departure within five minutes is a duplicate`() {
        assertTrue(
            TravelDedupe.isDuplicate(existing, "G5507", 1_700_000_002_000L)
        )
    }

    @Test
    fun `same train but more than five minutes apart is not a duplicate`() {
        assertFalse(
            TravelDedupe.isDuplicate(existing, "G5507", 1_700_000_400_000L)
        )
    }

    @Test
    fun `different train is not a duplicate even at the same time`() {
        assertFalse(
            TravelDedupe.isDuplicate(existing, "D123", 1_700_000_000_000L)
        )
    }

    @Test
    fun `empty existing list is never a duplicate`() {
        assertFalse(TravelDedupe.isDuplicate(emptyList(), "G5507", 1_700_000_000_000L))
    }

    @Test
    fun `exact boundary of five minutes is a duplicate`() {
        assertTrue(
            TravelDedupe.isDuplicate(existing, "G5507", 1_700_000_000_000L + TravelDedupe.DUPLICATE_WINDOW_MS)
        )
    }
}
