package com.kieronquinn.app.smartspacer.plugin.travel.logic

/**
 * Pure duplicate detection shared by the SMS receiver and the share flow, so both entry points
 * agree on what "the same trip" means and neither can double-insert or double-notify.
 */
object TravelDedupe {

    /** Trips are considered identical when train number matches and departure times differ by no more than 5 minutes. */
    const val DUPLICATE_WINDOW_MS = 5L * 60 * 1000L

    fun isDuplicate(existing: Collection<TripKey>, trainNumber: String, departureTime: Long): Boolean {
        return existing.any {
            it.trainNumber == trainNumber &&
                Math.abs(it.departureTime - departureTime) <= DUPLICATE_WINDOW_MS
        }
    }
}

data class TripKey(val trainNumber: String, val departureTime: Long)
