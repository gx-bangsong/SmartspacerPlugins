package com.kieronquinn.app.smartspacer.plugin.qweather.utils

/**
 * Complication text is paged differently on Native Smartspace vs the Smartspacer widget.
 *
 * The user picks the surface in settings — auto-detecting the default launcher is unreliable
 * because Native Smartspace can run on the lock screen regardless of home app.
 */
enum class AdvicePaging(val prefValue: String, val maxChars: Int) {
    NATIVE("native", 28),
    WIDGET("widget", 8);

    companion object {
        const val DEFAULT_PREF = "native"

        fun fromPreference(value: String?): AdvicePaging {
            return entries.firstOrNull { it.prefValue == value } ?: NATIVE
        }
    }
}
