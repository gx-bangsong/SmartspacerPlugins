package com.kieronquinn.app.smartspacer.plugin.parcel.notifications

import android.content.Context

/**
 * Remembers parcels whose notifications the user dismissed / unpinned, so periodic database
 * sweeps or re-scans never automatically re-post them.
 */
class ParcelSuppressionRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "parcel_notification_suppression"
        private const val KEY_SUPPRESSED = "suppressed_parcel_ids"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun suppressParcel(parcelId: Long) {
        val set = prefs.getStringSet(KEY_SUPPRESSED, emptySet())!!.toMutableSet()
        set.add(parcelId.toString())
        prefs.edit().putStringSet(KEY_SUPPRESSED, set).apply()
    }

    fun isSuppressed(parcelId: Long): Boolean {
        return prefs.getStringSet(KEY_SUPPRESSED, emptySet())?.contains(parcelId.toString()) == true
    }

    fun clearForParcel(parcelId: Long) {
        val set = prefs.getStringSet(KEY_SUPPRESSED, emptySet())!!.toMutableSet()
        if (set.remove(parcelId.toString())) {
            prefs.edit().putStringSet(KEY_SUPPRESSED, set).apply()
        }
    }
}
