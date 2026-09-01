package com.kieronquinn.app.smartspacer.plugin.qweather.utils

/**
 * Presets for how long each lifestyle-advice page may be.
 *
 * Native At a Glance truncates to a fixed line — stuffing more characters into one
 * complication does not make them visible. The number we change is this split
 * threshold so extra items become the next page instead of being cut off.
 */
enum class AdvicePaging(val prefValue: String, val maxChars: Int) {
    NATIVE("native", 12),
    WIDGET("widget", 8);

    companion object {
        const val DEFAULT_PREF = "native"
        const val DEFAULT_LIMIT = 12
        val LIMIT_OPTIONS = listOf(8, 10, 12, 14, 16, 20, 24)

        fun fromPreference(value: String?): AdvicePaging {
            return entries.firstOrNull { it.prefValue == value } ?: NATIVE
        }

        fun sanitizeLimit(value: Int): Int = value.coerceIn(6, 32)
    }
}
