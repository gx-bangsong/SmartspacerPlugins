package com.kieronquinn.app.smartspacer.plugin.travel.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class TravelLiveUpdateCapsuleTest {

    @Test
    fun `capsule shows gate then seat`() {
        assertEquals("A12 15车12A", TravelLiveUpdateCapsule.text("A12", "15车12A"))
        assertEquals("H12 13F", TravelLiveUpdateCapsule.text("H12", "13F"))
    }

    @Test
    fun `capsule is only the gate when there is no seat`() {
        assertEquals("A12", TravelLiveUpdateCapsule.text("A12", null))
        assertEquals("B15-B16", TravelLiveUpdateCapsule.text("B15-B16", "  "))
    }

    @Test
    fun `capsule is only the seat when there is no gate`() {
        assertEquals("05A", TravelLiveUpdateCapsule.text(null, "05A"))
    }

    @Test
    fun `capsule never includes labels`() {
        assertEquals("A12 05A", TravelLiveUpdateCapsule.text("检票口：A12", "座位 05A"))
        assertEquals("H12", TravelLiveUpdateCapsule.text("Gate: H12", null))
    }

    @Test
    fun `blank values do not put a placeholder in the chip`() {
        assertEquals("", TravelLiveUpdateCapsule.text(null, null))
        assertEquals("", TravelLiveUpdateCapsule.text("  ", "检票口："))
    }
}
