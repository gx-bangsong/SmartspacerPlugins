package com.kieronquinn.app.smartspacer.plugin.travel.notifications

/**
 * Text shown in the promoted Live Update status chip.
 *
 * The chip is tiny: only the ticket-gate / boarding-gate identifier and the seat.
 * Never a station name, and never a label like "检票口：" — those make Android
 * drop the chip down to an icon.
 */
object TravelLiveUpdateCapsule {

    fun text(gate: String?, seat: String?): String {
        val parts = listOf(clean(gate), clean(seat)).filter { it.isNotEmpty() }
        return parts.joinToString(" ")
    }

    private fun clean(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return value
            .replace(LABEL, "")
            .replace(WHITESPACE, "")
            .trim()
    }

    private val LABEL = Regex(
        "^(检票口|登机口|座位|座位号|站台|Gate|Seat)\\s*[:：]?",
        RegexOption.IGNORE_CASE
    )
    private val WHITESPACE = Regex("\\s+")
}
