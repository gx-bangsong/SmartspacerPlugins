package com.kieronquinn.app.smartspacer.plugin.parcel.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.smartspacer.plugin.parcel.R
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelItem
import com.kieronquinn.app.smartspacer.plugin.parcel.receivers.ParcelActionReceiver
import com.kieronquinn.app.smartspacer.plugin.parcel.ui.fragments.ParcelDetailFragment
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.LiveUpdateAction
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.LiveUpdateNotificationController
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.LiveUpdateSpec
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationIds
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.DialogLauncherActivity

/**
 * Parcel notifications.
 *
 * Per the official Live Update guidance, ordinary package tracking is NOT appropriate for
 * promoted ongoing notifications, so parcels use a high-visibility regular notification by
 * default. When the user explicitly enables the experimental "promoted live update" setting, the
 * same notification is instead rendered as an ongoing Live Update (with "已取件" and "停止实时显示"
 * actions); it uses the parcel's stable notification ID either way.
 */
class ParcelNotificationController(context: Context) {

    private val context = context.applicationContext
    private val controller = LiveUpdateNotificationController(context)

    companion object {
        const val CHANNEL_ID = "parcel_pickup"
    }

    fun postParcel(parcel: ParcelItem, promoted: Boolean) {
        val notificationId = parcelNotificationId(parcel.id)
        val capsule = ParcelLiveUpdateCapsule.text(parcel.pickupCode)
        val content = parcel.stationName ?: context.getString(R.string.app_name)
        val spec = LiveUpdateSpec(
            channelId = CHANNEL_ID,
            channelNameRes = R.string.notification_channel,
            channelImportance = NotificationManager.IMPORTANCE_HIGH,
            notificationId = notificationId,
            smallIconRes = R.drawable.ic_launcher_monochrome,
            // Live Update title + chip are the raw pickup code. Never prefix "取件码：".
            contentTitle = if (capsule.isNotEmpty()) capsule else parcel.pickupCode,
            contentText = content,
            shortCriticalText = capsule.takeIf { it.isNotEmpty() },
            progressIndeterminate = promoted,
            ongoing = promoted,
            requestPromoted = promoted,
            autoCancel = !promoted,
            priority = NotificationCompat.PRIORITY_HIGH,
            category = Notification.CATEGORY_STATUS,
            visibility = if (promoted) {
                android.app.Notification.VISIBILITY_PUBLIC
            } else {
                android.app.Notification.VISIBILITY_PRIVATE
            },
            contentIntent = parcelContentIntent(parcel.id),
            deleteIntent = parcelUnpinIntent(parcel.id),
            actions = listOf(
                LiveUpdateAction(
                    R.drawable.ic_launcher_foreground,
                    context.getString(R.string.action_mark_as_picked_up),
                    parcelPickedUpIntent(parcel.id)
                ),
                LiveUpdateAction(
                    R.drawable.ic_launcher_foreground,
                    context.getString(R.string.action_stop_live_update),
                    parcelUnpinIntent(parcel.id)
                )
            ),
            publicVersionTitle = if (promoted) null else context.getString(R.string.notification_public_title),
            publicVersionText = if (promoted) null else context.getString(R.string.notification_public_content)
        )
        controller.post(spec, allowPromoted = promoted)
    }

    fun cancelParcel(parcelId: Long) {
        controller.cancel(parcelNotificationId(parcelId))
    }

    fun parcelNotificationId(parcelId: Long): Int =
        NotificationIds.forEntity(NotificationIds.NAMESPACE_PARCEL, parcelId)

    private fun pendingIntentFlags(): Int = PendingIntent.FLAG_UPDATE_CURRENT or
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    private fun parcelContentIntent(parcelId: Long): PendingIntent {
        val intent = Intent(context, DialogLauncherActivity::class.java).apply {
            putExtra(DialogLauncherActivity.EXTRA_FRAGMENT_CLASS, ParcelDetailFragment::class.java.name)
            putExtra("parcelId", parcelId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return PendingIntent.getActivity(
            context,
            NotificationIds.forEntity("parcel_content", parcelId),
            intent,
            pendingIntentFlags()
        )
    }

    private fun parcelPickedUpIntent(parcelId: Long): PendingIntent {
        val intent = Intent(context, ParcelActionReceiver::class.java).apply {
            action = ParcelActionReceiver.ACTION_MARK_AS_PICKED_UP
            putExtra(ParcelActionReceiver.EXTRA_PARCEL_ID, parcelId)
        }
        return PendingIntent.getBroadcast(
            context,
            NotificationIds.forEntity("parcel_picked_up", parcelId),
            intent,
            pendingIntentFlags()
        )
    }

    private fun parcelUnpinIntent(parcelId: Long): PendingIntent {
        val intent = Intent(context, ParcelActionReceiver::class.java).apply {
            action = ParcelActionReceiver.ACTION_UNPIN
            putExtra(ParcelActionReceiver.EXTRA_PARCEL_ID, parcelId)
        }
        return PendingIntent.getBroadcast(
            context,
            NotificationIds.forEntity("parcel_unpin", parcelId),
            intent,
            pendingIntentFlags()
        )
    }
}
