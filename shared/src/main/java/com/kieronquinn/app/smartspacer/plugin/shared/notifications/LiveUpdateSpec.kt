package com.kieronquinn.app.smartspacer.plugin.shared.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat

/**
 * A single, declarative description of a notification that the
 * [LiveUpdateNotificationController] can render either as a promoted ongoing ("Live Update")
 * notification (platform builder, API 36+) or as a regular [NotificationCompat] notification on
 * older devices / when promotion is unavailable.
 */
data class LiveUpdateSpec(
    val channelId: String,
    val channelNameRes: Int,
    val channelImportance: Int = NotificationManager.IMPORTANCE_DEFAULT,
    val notificationId: Int,
    val smallIconRes: Int,
    val contentTitle: CharSequence,
    val contentText: CharSequence? = null,
    val subText: CharSequence? = null,
    /** Must be true for promoted Live Updates; also makes the notification non-dismissable on most OEM skins. */
    val ongoing: Boolean = false,
    /** Requests promotion. Ignored (falls back to a normal notification) when not eligible. */
    val requestPromoted: Boolean = false,
    val autoCancel: Boolean = true,
    val priority: Int = NotificationCompat.PRIORITY_DEFAULT,
    val category: String? = null,
    /** Absolute timestamp shown in the header; with [usesChronometer] drives the countdown chip. */
    val whenTime: Long? = null,
    val usesChronometer: Boolean = false,
    val chronometerCountDown: Boolean = false,
    /** Short critical text shown in the status chip (API 36+ only, ignored below). */
    val shortCriticalText: CharSequence? = null,
    /** Indeterminate progress style; used for the "parsing" state of the travel share flow. */
    val progressIndeterminate: Boolean = false,
    val contentIntent: PendingIntent? = null,
    /** Handles user dismissal / un-pinning so the notification is not re-posted. */
    val deleteIntent: PendingIntent? = null,
    val actions: List<LiveUpdateAction> = emptyList(),
    val visibility: Int = NotificationCompat.VISIBILITY_PRIVATE,
    /** Redacted public version (shown on the lock screen). Only used when [visibility] is set. */
    val publicVersionTitle: CharSequence? = null,
    val publicVersionText: CharSequence? = null
) {
    /** The requirements checklist for a promotable notification (docs: "Create live update notifications"). */
    val isPromotableShape: Boolean
        get() = requestPromoted && ongoing &&
            !contentTitle.isNullOrEmpty() &&
            channelImportance != NotificationManager.IMPORTANCE_MIN
}

data class LiveUpdateAction(
    @DrawableRes val iconRes: Int,
    val title: CharSequence,
    val pendingIntent: PendingIntent
)
