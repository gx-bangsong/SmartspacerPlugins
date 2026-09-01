package com.kieronquinn.app.smartspacer.plugin.qweather.utils

/**
 * Optional bedtime / wake windows. When neither window is enabled the complication is
 * always visible. Otherwise it is shown only from each enabled start time for [durationMinutes],
 * then hidden until the next window.
 */
object LifestyleDisplayWindow {

    const val MINUTES_PER_DAY = 24 * 60

    fun isVisible(
        nowMinutesOfDay: Int,
        bedtimeEnabled: Boolean,
        bedtimeStartMinutes: Int,
        wakeEnabled: Boolean,
        wakeStartMinutes: Int,
        durationMinutes: Int
    ): Boolean {
        if (!bedtimeEnabled && !wakeEnabled) return true
        val duration = durationMinutes.coerceAtLeast(1)
        if (bedtimeEnabled && inWindow(nowMinutesOfDay, bedtimeStartMinutes, duration)) return true
        if (wakeEnabled && inWindow(nowMinutesOfDay, wakeStartMinutes, duration)) return true
        return false
    }

    fun inWindow(nowMinutesOfDay: Int, startMinutes: Int, durationMinutes: Int): Boolean {
        val start = startMinutes.mod(MINUTES_PER_DAY)
        val duration = durationMinutes.coerceAtLeast(1)
        val now = nowMinutesOfDay.mod(MINUTES_PER_DAY)
        val end = start + duration
        return if (end <= MINUTES_PER_DAY) {
            now in start until end
        } else {
            now >= start || now < (end - MINUTES_PER_DAY)
        }
    }

    /**
     * Milliseconds until the next show/hide boundary, or null when windows are disabled
     * (always visible, no refresh needed).
     */
    fun millisUntilNextTransition(
        nowEpochMillis: Long,
        bedtimeEnabled: Boolean,
        bedtimeStartMinutes: Int,
        wakeEnabled: Boolean,
        wakeStartMinutes: Int,
        durationMinutes: Int
    ): Long? {
        if (!bedtimeEnabled && !wakeEnabled) return null
        val duration = durationMinutes.coerceAtLeast(1)
        val boundaries = mutableListOf<Int>()
        if (bedtimeEnabled) {
            boundaries += bedtimeStartMinutes.mod(MINUTES_PER_DAY)
            boundaries += (bedtimeStartMinutes + duration).mod(MINUTES_PER_DAY)
        }
        if (wakeEnabled) {
            boundaries += wakeStartMinutes.mod(MINUTES_PER_DAY)
            boundaries += (wakeStartMinutes + duration).mod(MINUTES_PER_DAY)
        }
        val unique = boundaries.distinct().sorted()
        if (unique.isEmpty()) return null

        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = nowEpochMillis }
        val nowMinutes = calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
            calendar.get(java.util.Calendar.MINUTE)
        val nowSeconds = calendar.get(java.util.Calendar.SECOND)
        val nowMillis = calendar.get(java.util.Calendar.MILLISECOND)
        val elapsedInMinute = nowSeconds * 1000L + nowMillis

        val nextToday = unique.firstOrNull { it > nowMinutes }
        val minutesUntil = if (nextToday != null) {
            nextToday - nowMinutes
        } else {
            MINUTES_PER_DAY - nowMinutes + unique.first()
        }
        return minutesUntil * 60_000L - elapsedInMinute
    }

    fun formatClock(minutesOfDay: Int): String {
        val clamped = minutesOfDay.mod(MINUTES_PER_DAY)
        return "%02d:%02d".format(clamped / 60, clamped % 60)
    }
}
