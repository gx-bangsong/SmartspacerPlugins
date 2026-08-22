package com.kieronquinn.app.smartspacer.plugin.parcel.notifications

/**
 * Text shown in the promoted Live Update status chip (the lock-screen / status-bar capsule).
 * The chip is too small for a label like "Pickup code:", so it carries the code itself.
 */
object ParcelLiveUpdateCapsule {

    /** Fits typical locker codes (e.g. 888888, 9-2-1004) without overflowing the chip. */
    const val MAX_CHARS = 12

    fun text(pickupCode: String): String {
        val cleaned = pickupCode.trim().replace(WHITESPACE, "")
        if (cleaned.isEmpty()) return ""
        return if (cleaned.length <= MAX_CHARS) cleaned else cleaned.take(MAX_CHARS)
    }

    private val WHITESPACE = Regex("\\s+")
}
