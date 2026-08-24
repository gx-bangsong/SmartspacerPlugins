package com.kieronquinn.app.smartspacer.shared.smsparser

/**
 * Pulls the departure-station ticket gate / boarding gate out of a travel SMS.
 *
 * 12306 and airline messages write this many ways (`检票口A12`, `检票口：B15-B16`,
 * `请从A12检票口进站`, `登机口H12`, `Gate H12`). The value is the identifier only —
 * never a label — so it can sit in a Live Update chip.
 */
object TravelGateExtractor {

    private val PATTERNS = listOf(
        Regex("检票口\\s*[:：为是]?\\s*([A-Za-z东西南北]?\\d{1,3}(?:\\s*[-~～至到、，,]\\s*[A-Za-z东西南北]?\\d{1,3})?)"),
        Regex("([A-Za-z东西南北]?\\d{1,3})\\s*号?检票口"),
        Regex("登机口\\s*[:：为是]?\\s*([A-Za-z]\\d{1,3})"),
        Regex("(?i)\\bGate\\s*[:：]?\\s*([A-Za-z]?\\d{1,3})")
    )

    fun extract(rawText: String): String? {
        for (pattern in PATTERNS) {
            val match = pattern.find(rawText) ?: continue
            val value = match.groupValues[1].replace(WHITESPACE, "")
            if (value.isNotBlank()) return value
        }
        return null
    }

    private val WHITESPACE = Regex("\\s+")
}
