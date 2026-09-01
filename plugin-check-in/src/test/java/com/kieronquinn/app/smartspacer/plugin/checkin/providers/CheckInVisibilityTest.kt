package com.kieronquinn.app.smartspacer.plugin.checkin.providers

import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckInVisibilityTest {

    private fun record(checkIn: Long? = 1L, checkOut: Long? = null) = CheckInItem(
        date = "2026-08-24",
        checkInTime = checkIn,
        checkOutTime = checkOut
    )

    @Test
    fun `shows before any punch`() {
        assertTrue(CheckInVisibility.shouldShow(null, checkInOnly = false, endReached = false))
        assertTrue(CheckInVisibility.shouldShow(record(checkIn = null), checkInOnly = true, endReached = false))
    }

    @Test
    fun `hides after morning punch until checkout is due`() {
        assertFalse(CheckInVisibility.shouldShow(record(), checkInOnly = false, endReached = false))
        assertTrue(CheckInVisibility.shouldShow(record(), checkInOnly = false, endReached = true))
    }

    @Test
    fun `check-in-only hides for the rest of the day after punch`() {
        assertFalse(CheckInVisibility.shouldShow(record(), checkInOnly = true, endReached = false))
        assertFalse(CheckInVisibility.shouldShow(record(), checkInOnly = true, endReached = true))
    }

    @Test
    fun `hides after checkout`() {
        assertFalse(
            CheckInVisibility.shouldShow(
                record(checkOut = 2L),
                checkInOnly = false,
                endReached = true
            )
        )
    }
}
