package com.kieronquinn.app.smartspacer.plugin.travel.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.LiveUpdateAction
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.LiveUpdateNotificationController
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.LiveUpdateSpec
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationIds
import com.kieronquinn.app.smartspacer.plugin.travel.R
import com.kieronquinn.app.shared.R as SharedR
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TravelShareDraft
import com.kieronquinn.app.smartspacer.plugin.travel.receivers.TravelActionReceiver
import com.kieronquinn.app.smartspacer.plugin.travel.ui.activities.TravelActionActivity
import com.kieronquinn.app.smartspacer.plugin.travel.ui.activities.TravelShareParseActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Builds and posts every notification the travel plugin produces.
 *
 * ID namespaces (see [NotificationIds]):
 *  - `travel_share_op` : the share-parse operation notification (PARSING → REVIEW_REQUIRED →
 *    CONFIRMED/CANCELLED/FAILED), keyed by the operation UUID;
 *  - `travel_trip`     : the saved trip notification (result notification upgraded in place to a
 *    promoted Live Update inside the departure window), keyed by the Room row id.
 *
 * These two namespaces are intentionally separate so a share operation can never overwrite a
 * saved trip's Live Update (and vice versa).
 */
class TravelNotificationController(context: Context) {

    private val context = context.applicationContext
    private val controller = LiveUpdateNotificationController(context)

    companion object {
        const val CHANNEL_REMINDERS = "travel_reminders"
        const val CHANNEL_SHARE_OP = "travel_share_parsing"
        const val CHANNEL_RESULTS = "travel_results"
        const val DEPARTURE_WINDOW_MS = 30L * 60 * 1000L
        const val GRACE_PERIOD_MS = 2L * 60 * 60 * 1000L

        private val timeFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

        fun opIdToRequestCode(opId: String): Long = try {
            UUID.fromString(opId).mostSignificantBits
        } catch (e: IllegalArgumentException) {
            opId.hashCode().toLong()
        }

        fun shareOpNotificationId(opId: String): Int =
            NotificationIds.forEntity(NotificationIds.NAMESPACE_TRAVEL_SHARE_OP, opIdToRequestCode(opId))

        fun tripNotificationId(tripId: Int): Int =
            NotificationIds.forEntity(NotificationIds.NAMESPACE_TRAVEL_TRIP, tripId.toLong())
    }

    // ------------------------------------------------------------------ share operation

    fun postShareParsing(opId: String) {
        controller.post(
            LiveUpdateSpec(
                channelId = CHANNEL_SHARE_OP,
                channelNameRes = R.string.notification_channel_share_op,
                channelImportance = NotificationManager.IMPORTANCE_DEFAULT,
                notificationId = shareOpNotificationId(opId),
                smallIconRes = R.mipmap.ic_launcher,
                contentTitle = context.getString(R.string.share_op_parsing_title),
                contentText = context.getString(R.string.share_op_parsing_content),
                ongoing = true,
                requestPromoted = true,
                autoCancel = false,
                priority = NotificationCompat.PRIORITY_DEFAULT,
                category = Notification.CATEGORY_PROGRESS,
                progressIndeterminate = true,
                contentIntent = shareOpContentIntent(opId),
                deleteIntent = shareOpCancelIntent(opId),
                actions = listOf(
                    LiveUpdateAction(
                        SharedR.drawable.ic_close,
                        context.getString(R.string.share_op_cancel),
                        shareOpCancelIntent(opId)
                    )
                ),
                visibility = android.app.Notification.VISIBILITY_PRIVATE,
                publicVersionTitle = context.getString(R.string.share_op_public_title),
                publicVersionText = context.getString(R.string.share_op_public_content)
            )
        )
    }

    fun postShareReview(draft: TravelShareDraft) {
        val time = timeFormat.format(Date(draft.departureTime))
        val title = draft.trainNumber
        val content = buildString {
            append(draft.departureStation)
            if (!draft.arrivalStation.isNullOrBlank()) append(" → ").append(draft.arrivalStation)
            append(" · ").append(time)
            if (!draft.seat.isNullOrBlank()) append(" · ").append(draft.seat)
        }
        controller.post(
            LiveUpdateSpec(
                channelId = CHANNEL_SHARE_OP,
                channelNameRes = R.string.notification_channel_share_op,
                channelImportance = NotificationManager.IMPORTANCE_DEFAULT,
                notificationId = shareOpNotificationId(draft.opId),
                smallIconRes = R.mipmap.ic_launcher,
                contentTitle = context.getString(R.string.share_op_review_title, title),
                contentText = content,
                ongoing = true,
                requestPromoted = true,
                autoCancel = false,
                priority = NotificationCompat.PRIORITY_DEFAULT,
                category = Notification.CATEGORY_PROGRESS,
                contentIntent = shareOpReviewIntent(draft.opId),
                deleteIntent = shareOpCancelIntent(draft.opId),
                actions = listOf(
                    LiveUpdateAction(
                        SharedR.drawable.ic_smartspacer,
                        context.getString(R.string.share_op_review_confirm),
                        shareOpReviewIntent(draft.opId)
                    ),
                    LiveUpdateAction(
                        SharedR.drawable.ic_close,
                        context.getString(R.string.share_op_cancel),
                        shareOpCancelIntent(draft.opId)
                    )
                ),
                visibility = android.app.Notification.VISIBILITY_PRIVATE,
                publicVersionTitle = context.getString(R.string.share_op_public_title),
                publicVersionText = context.getString(R.string.share_op_public_content)
            )
        )
    }

    fun postShareFailed(opId: String) {
        controller.post(
            LiveUpdateSpec(
                channelId = CHANNEL_SHARE_OP,
                channelNameRes = R.string.notification_channel_share_op,
                channelImportance = NotificationManager.IMPORTANCE_DEFAULT,
                notificationId = shareOpNotificationId(opId),
                smallIconRes = R.mipmap.ic_launcher,
                contentTitle = context.getString(R.string.share_op_failed_title),
                contentText = context.getString(R.string.share_op_failed_content),
                ongoing = false,
                requestPromoted = false,
                autoCancel = true,
                priority = NotificationCompat.PRIORITY_DEFAULT,
                category = Notification.CATEGORY_ERROR,
                contentIntent = shareOpReviewIntent(opId),
                actions = listOf(
                    LiveUpdateAction(
                        SharedR.drawable.ic_search,
                        context.getString(R.string.share_op_retry_manual),
                        shareOpReviewIntent(opId)
                    )
                ),
                visibility = android.app.Notification.VISIBILITY_PRIVATE,
                publicVersionTitle = context.getString(R.string.share_op_public_title),
                publicVersionText = context.getString(R.string.share_op_public_content)
            )
        )
    }

    fun postShareSaved(summary: String) {
        controller.post(
            LiveUpdateSpec(
                channelId = CHANNEL_RESULTS,
                channelNameRes = R.string.notification_channel_results,
                channelImportance = NotificationManager.IMPORTANCE_DEFAULT,
                notificationId = NotificationIds.forEntity("travel_share_saved", 1L),
                smallIconRes = R.mipmap.ic_launcher,
                contentTitle = context.getString(R.string.share_op_saved_title),
                contentText = summary,
                ongoing = false,
                autoCancel = true,
                priority = NotificationCompat.PRIORITY_DEFAULT,
                category = Notification.CATEGORY_REMINDER,
                visibility = android.app.Notification.VISIBILITY_PRIVATE,
                publicVersionTitle = context.getString(R.string.share_op_saved_title),
                publicVersionText = context.getString(R.string.share_op_public_content)
            )
        )
    }

    fun cancelShareOp(opId: String) {
        controller.cancel(shareOpNotificationId(opId))
    }


    // ------------------------------------------------------------------ saved trips

    /**
     * Normal result notification shown right after a successful automatic SMS parse. If the trip
     * is already inside the departure window, [postTripLiveUpdate] is used instead so the same
     * notification ID is upgraded to a promoted Live Update at T-30 without re-notifying.
     */
    fun postTripResult(item: TravelInfoItem) {
        controller.post(
            LiveUpdateSpec(
                channelId = CHANNEL_RESULTS,
                channelNameRes = R.string.notification_channel_results,
                channelImportance = NotificationManager.IMPORTANCE_HIGH,
                notificationId = tripNotificationId(item.id),
                smallIconRes = R.mipmap.ic_launcher,
                contentTitle = context.getString(R.string.notification_result_title, item.trainNumber),
                contentText = tripContent(item),
                ongoing = false,
                autoCancel = true,
                priority = NotificationCompat.PRIORITY_HIGH,
                category = Notification.CATEGORY_REMINDER,
                contentIntent = tripViewIntent(item),
                visibility = android.app.Notification.VISIBILITY_PRIVATE,
                publicVersionTitle = context.getString(R.string.notification_public_title),
                publicVersionText = context.getString(R.string.notification_public_content)
            )
        )
    }

    /**
     * Promoted Live Update for a trip inside the departure window. Uses the trip's stable
     * notification ID and the system countdown (`setWhen(departureTime)` + chronometer), so the
     * app is not woken up every minute to tick.
     */
    fun postTripLiveUpdate(item: TravelInfoItem, allowPromoted: Boolean = true) {
        controller.post(
            LiveUpdateSpec(
                channelId = CHANNEL_REMINDERS,
                channelNameRes = R.string.notification_channel_reminders,
                channelImportance = NotificationManager.IMPORTANCE_HIGH,
                notificationId = tripNotificationId(item.id),
                smallIconRes = R.mipmap.ic_launcher,
                contentTitle = context.getString(R.string.notification_live_title, item.trainNumber),
                contentText = tripContent(item),
                ongoing = true,
                requestPromoted = true,
                autoCancel = false,
                priority = NotificationCompat.PRIORITY_HIGH,
                category = Notification.CATEGORY_REMINDER,
                whenTime = item.departureTime,
                usesChronometer = true,
                chronometerCountDown = true,
                shortCriticalText = context.getString(R.string.notification_short_critical),
                contentIntent = tripViewIntent(item),
                deleteIntent = tripDismissedIntent(item.id),
                actions = listOf(
                    LiveUpdateAction(
                        SharedR.drawable.ic_info,
                        context.getString(R.string.notification_action_view),
                        tripViewIntent(item)
                    ),
                    LiveUpdateAction(
                        SharedR.drawable.ic_smartspacer,
                        context.getString(R.string.notification_action_mark_used),
                        tripMarkUsedIntent(item.id)
                    )
                ),
                visibility = android.app.Notification.VISIBILITY_PRIVATE,
                publicVersionTitle = context.getString(R.string.notification_public_title),
                publicVersionText = context.getString(R.string.notification_public_content)
            ),
            allowPromoted = allowPromoted
        )
    }

    fun cancelTrip(tripId: Int) {
        controller.cancel(tripNotificationId(tripId))
    }

    private fun tripContent(item: TravelInfoItem): String {
        val time = timeFormat.format(Date(item.departureTime))
        return buildString {
            append(item.departureStation)
            if (!item.arrivalStation.isNullOrBlank()) append(" → ").append(item.arrivalStation)
            append(" · ").append(time)
            if (!item.seat.isNullOrBlank()) append(" · ").append(item.seat)
        }
    }

    // ------------------------------------------------------------------ pending intents

    private fun pendingIntentFlags(): Int = PendingIntent.FLAG_UPDATE_CURRENT or
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    private fun shareOpContentIntent(opId: String): PendingIntent {
        val intent = Intent(context, TravelShareParseActivity::class.java).apply {
            action = TravelShareParseActivity.ACTION_REVIEW_OP
            putExtra(TravelShareParseActivity.EXTRA_OP_ID, opId)
            putExtra(TravelShareParseActivity.EXTRA_OP_ACTION, TravelShareParseActivity.ACTION_REVIEW_OP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return PendingIntent.getActivity(
            context,
            NotificationIds.forEntity("travel_share_op_content", opIdToRequestCode(opId)),
            intent,
            pendingIntentFlags()
        )
    }

    private fun shareOpReviewIntent(opId: String): PendingIntent {
        val intent = Intent(context, TravelShareParseActivity::class.java).apply {
            action = TravelShareParseActivity.ACTION_REVIEW_OP
            putExtra(TravelShareParseActivity.EXTRA_OP_ID, opId)
            putExtra(TravelShareParseActivity.EXTRA_OP_ACTION, TravelShareParseActivity.ACTION_REVIEW_OP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return PendingIntent.getActivity(
            context,
            NotificationIds.forEntity("travel_share_op_review", opIdToRequestCode(opId)),
            intent,
            pendingIntentFlags()
        )
    }

    private fun shareOpCancelIntent(opId: String): PendingIntent {
        val intent = Intent(context, TravelShareParseActivity::class.java).apply {
            action = TravelShareParseActivity.ACTION_CANCEL_OP
            putExtra(TravelShareParseActivity.EXTRA_OP_ID, opId)
            putExtra(TravelShareParseActivity.EXTRA_OP_ACTION, TravelShareParseActivity.ACTION_CANCEL_OP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return PendingIntent.getActivity(
            context,
            NotificationIds.forEntity("travel_share_op_cancel", opIdToRequestCode(opId)),
            intent,
            pendingIntentFlags()
        )
    }

    private fun tripViewIntent(item: TravelInfoItem): PendingIntent {
        val intent = Intent(context, TravelActionActivity::class.java).apply {
            putExtra("trainNumber", item.trainNumber)
            putExtra("departureStation", item.departureStation)
            putExtra("arrivalStation", item.arrivalStation ?: "")
            putExtra("seat", item.seat ?: "")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return PendingIntent.getActivity(
            context,
            NotificationIds.forEntity("travel_trip_view", item.id.toLong()),
            intent,
            pendingIntentFlags()
        )
    }

    private fun tripMarkUsedIntent(tripId: Int): PendingIntent {
        val intent = Intent(context, TravelActionReceiver::class.java).apply {
            action = TravelActionReceiver.ACTION_MARK_USED
            putExtra(TravelActionReceiver.EXTRA_TRIP_ID, tripId)
        }
        return PendingIntent.getBroadcast(
            context,
            NotificationIds.forEntity("travel_trip_mark_used", tripId.toLong()),
            intent,
            pendingIntentFlags()
        )
    }

    private fun tripDismissedIntent(tripId: Int): PendingIntent {
        val intent = Intent(context, TravelActionReceiver::class.java).apply {
            action = TravelActionReceiver.ACTION_DISMISSED
            putExtra(TravelActionReceiver.EXTRA_TRIP_ID, tripId)
        }
        return PendingIntent.getBroadcast(
            context,
            NotificationIds.forEntity("travel_trip_dismissed", tripId.toLong()),
            intent,
            pendingIntentFlags()
        )
    }
}

/** Whether the trip's departure is within the "imminent departure" Live Update window. */
fun TravelInfoItem.isWithinDepartureWindow(now: Long): Boolean {
    val windowStart = departureTime - TravelNotificationController.DEPARTURE_WINDOW_MS
    return departureTime >= now && windowStart <= now
}
