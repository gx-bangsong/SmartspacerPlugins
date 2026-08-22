package com.kieronquinn.app.smartspacer.plugin.travel.notifications

import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem

/**
 * Decides when a saved trip must show its departure Live Update *now*, rather than waiting for
 * the T-30 alarm. Used by the scheduler (manual / share / SMS / boot / permission-grant
 * reschedule) so a trip that is already inside the departure window is never left without a
 * notification just because `reminderTime` is no longer in the future.
 */
object TravelLiveUpdateGate {

    fun shouldPostNow(item: TravelInfoItem, now: Long, suppressed: Boolean): Boolean {
        return !item.isUsed && !suppressed && item.isWithinDepartureWindow(now)
    }
}
