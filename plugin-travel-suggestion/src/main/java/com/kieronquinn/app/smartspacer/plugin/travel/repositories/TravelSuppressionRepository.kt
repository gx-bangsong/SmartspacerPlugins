package com.kieronquinn.app.smartspacer.plugin.travel.repositories

import android.content.Context

/**
 * Remembers trips whose notifications the user dismissed, so they are never automatically
 * re-posted by later rescheduling (reboot, timezone change, re-scan, ...).
 */
class TravelSuppressionRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "travel_notification_suppression"
        private const val KEY_SUPPRESSED = "suppressed_trip_ids"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun suppressTrip(tripId: Int) {
        val set = prefs.getStringSet(KEY_SUPPRESSED, emptySet())!!.toMutableSet()
        set.add(tripId.toString())
        prefs.edit().putStringSet(KEY_SUPPRESSED, set).apply()
    }

    fun isSuppressed(tripId: Int): Boolean {
        return prefs.getStringSet(KEY_SUPPRESSED, emptySet())?.contains(tripId.toString()) == true
    }

    fun clearForTrip(tripId: Int) {
        val set = prefs.getStringSet(KEY_SUPPRESSED, emptySet())!!.toMutableSet()
        if (set.remove(tripId.toString())) {
            prefs.edit().putStringSet(KEY_SUPPRESSED, set).apply()
        }
    }
}
