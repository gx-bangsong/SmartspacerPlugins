package com.kieronquinn.app.smartspacer.plugin.checkin.providers

import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInItem

/**
 * After a successful punch the target should disappear. It only comes back when the
 * next punch is still outstanding (checkout, if that mode is enabled and the end
 * time has been reached).
 */
object CheckInVisibility {

    fun shouldShow(
        record: CheckInItem?,
        checkInOnly: Boolean,
        endReached: Boolean
    ): Boolean {
        val checkedIn = record?.checkInTime != null
        val checkedOut = record?.checkOutTime != null
        return when {
            !checkedIn -> true
            checkInOnly -> false
            checkedOut -> false
            endReached -> true
            else -> false
        }
    }
}
