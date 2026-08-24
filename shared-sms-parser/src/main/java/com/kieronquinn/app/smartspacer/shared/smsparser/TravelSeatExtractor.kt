package com.kieronquinn.app.smartspacer.shared.smsparser

/**
 * Fallback seat parser for itinerary SMS that the rule regex did not map, e.g.
 * `15车12A号` / `座位05A` / `无座`.
 */
object TravelSeatExtractor {

    private val PATTERNS = listOf(
        Regex("(\\d{1,2}车\\d{1,3}[A-Za-z])"),
        Regex("座位\\s*[:：为是]?\\s*([A-Za-z0-9]{1,8})"),
        Regex("(?<!\\d)(无座)(?!\\d)")
    )

    fun extract(rawText: String): String? {
        for (pattern in PATTERNS) {
            val match = pattern.find(rawText) ?: continue
            val value = match.groupValues[1].trim()
            if (value.isNotBlank()) return value
        }
        return null
    }
}
