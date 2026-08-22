package com.kieronquinn.app.smartspacer.plugin.parcel.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class ParcelLiveUpdateCapsuleTest {

    @Test
    fun `capsule shows the pickup code itself`() {
        assertEquals("888888", ParcelLiveUpdateCapsule.text("888888"))
        assertEquals("123456", ParcelLiveUpdateCapsule.text("123456"))
        assertEquals("9-2-1004", ParcelLiveUpdateCapsule.text("9-2-1004"))
    }

    @Test
    fun `capsule never includes the pickup-code label`() {
        assertEquals("888888", ParcelLiveUpdateCapsule.text("取件码：888888"))
        assertEquals("888888", ParcelLiveUpdateCapsule.text("取件码:888888"))
        assertEquals("9-2-1004", ParcelLiveUpdateCapsule.text("Pickup code: 9-2-1004"))
    }

    @Test
    fun `capsule strips surrounding whitespace`() {
        assertEquals("123456", ParcelLiveUpdateCapsule.text("  123456  "))
        assertEquals("A1-1004", ParcelLiveUpdateCapsule.text("A1 - 1004"))
    }

    @Test
    fun `blank codes do not put a placeholder in the chip`() {
        assertEquals("", ParcelLiveUpdateCapsule.text("   "))
        assertEquals("", ParcelLiveUpdateCapsule.text("取件码："))
    }
}
