package com.kieronquinn.app.smartspacer.plugin.parcel.notifications

/**
 * Text shown in the promoted Live Update status chip (the lock-screen / status-bar capsule).
 * The chip is too small for a label like "Pickup code:", so it carries the code itself.
 */
object ParcelLiveUpdateCapsule {

    /**
     * Status-bar chips are tiny. Android hides the text (icon only) when less than half of
     * [setShortCriticalText] would fit — 6 characters is the practical ceiling for a locker code.
     */
    const val MAX_CHARS = 6

    fun text(pickupCode: String): String {
        val cleaned = pickupCode.trim().replace(WHITESPACE, "")
        if (cleaned.isEmpty()) return ""
        return if (cleaned.length <= MAX_CHARS) cleaned else cleaned.takeLast(MAX_CHARS)
    }

    private val WHITESPACE = Regex("\\s+")
}
