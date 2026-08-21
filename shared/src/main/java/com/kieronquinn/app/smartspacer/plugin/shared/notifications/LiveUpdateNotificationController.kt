package com.kieronquinn.app.smartspacer.plugin.shared.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

/**
 * Central notification controller for this repository's plugins.
 *
 * It renders [LiveUpdateSpec]s either as promoted ongoing notifications (Live Updates) using the
 * platform [Notification.Builder] API 36+ surface, or as regular [NotificationCompat]
 * notifications on older devices / when promotion is unavailable. This keeps a single code path
 * for every plugin while still satisfying every "must" of the promoted-ongoing requirements:
 *
 *  - `POST_PROMOTED_NOTIFICATIONS` manifest permission is a prerequisite checked by
 *    [LiveUpdateEligibility.isPromotedSupported] (the permission itself is declared per app).
 *  - `setRequestPromotedOngoing(true)` + `setOngoing(true)` + non-empty contentTitle.
 *  - Standard styles only ([Notification.ProgressStyle] for progress, plain/BigText otherwise).
 *  - Channel with importance > [NotificationManager.IMPORTANCE_MIN].
 *  - No custom content view, no colorized, not a group summary, [NotificationCompat.VISIBILITY_PRIVATE]
 *    with a redacted public version for lock-screen privacy.
 *
 * [LiveUpdateEligibility.hasPromotableCharacteristics] is consulted before requesting promotion so
 * that on Android 16.0 (where the 36.1/QPR opt-in flag is disabled) we simply fall back to the
 * normal ongoing notification instead of posting a dead "promoted" request.
 */
class LiveUpdateNotificationController(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Posts (or in-place updates, when [LiveUpdateSpec.notificationId] matches an existing
     * notification) the given spec.
     *
     * @param allowPromoted when false the spec is always rendered as a regular notification,
     *   regardless of eligibility (used for explicit fallbacks).
     */
    fun post(spec: LiveUpdateSpec, allowPromoted: Boolean = true): Notification {
        ensureChannel(spec)

        val promoted = allowPromoted && spec.requestPromoted && spec.isPromotableShape &&
            LiveUpdateEligibility.isAtLeastBaklavaQpr1() &&
            LiveUpdateEligibility.isPromotedSupported(context)

        val notification = if (promoted) {
            buildPlatformPromoted(spec)
        } else {
            buildCompat(spec)
        }

        // If the framework reports the notification cannot be promoted (e.g. initial Android 16.0
        // release, OEM implementation, or user toggles), still post it — as a normal notification.
        if (promoted && !LiveUpdateEligibility.hasPromotableCharacteristics(notification)) {
            notificationManager.notify(spec.notificationId, buildCompat(spec))
        } else {
            notificationManager.notify(spec.notificationId, notification)
        }
        return notification
    }

    fun cancel(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    private fun ensureChannel(spec: LiveUpdateSpec) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                spec.channelId,
                context.getString(spec.channelNameRes),
                spec.channelImportance
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Platform builder used on Android 16 QPR1 (minor SDK 36.1)+ for promoted Live Updates. This
     * is the same API surface the official `platform-samples/user-interface/live-updates` sample
     * uses (`Notification.ProgressStyle`, `setRequestPromotedOngoing`, `setShortCriticalText`,
     * ...). The caller gates this path with
     * [LiveUpdateEligibility.isAtLeastBaklavaQpr1], because `setRequestPromotedOngoing` and
     * `ProgressStyle#setProgressIndeterminate` do not exist on the initial Android 16.0 release.
     */
    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun buildPlatformPromoted(spec: LiveUpdateSpec): Notification {
        val builder = Notification.Builder(context, spec.channelId).apply {
            setSmallIcon(spec.smallIconRes)
            setContentTitle(spec.contentTitle)
            spec.contentText?.let { setContentText(it) }
            spec.subText?.let { setSubText(it) }
            setOngoing(true)
            setOnlyAlertOnce(true)
            setRequestPromotedOngoing(true)
            setAutoCancel(spec.autoCancel)
            setPriority(spec.priority)
            spec.category?.let { setCategory(it) }
            spec.whenTime?.let { setWhen(it) }
            if (spec.usesChronometer) {
                setUsesChronometer(true)
                setChronometerCountDown(spec.chronometerCountDown)
            }
            spec.shortCriticalText?.let { setShortCriticalText(it.toString()) }
            if (spec.progressIndeterminate) {
                setStyle(Notification.ProgressStyle().setProgressIndeterminate(true))
            }
            spec.contentIntent?.let { setContentIntent(it) }
            spec.deleteIntent?.let { setDeleteIntent(it) }
            spec.actions.forEach { action ->
                val platformAction = Notification.Action.Builder(
                    Icon.createWithResource(context, action.iconRes),
                    action.title,
                    action.pendingIntent
                ).build()
                addAction(platformAction)
            }
            setVisibility(spec.visibility)
            spec.publicVersionTitle?.let { title ->
                val publicVersion = Notification.Builder(context, spec.channelId)
                    .setSmallIcon(spec.smallIconRes)
                    .setContentTitle(title)
                    .setContentText(spec.publicVersionText ?: "")
                    .build()
                setPublicVersion(publicVersion)
            }
        }
        return builder.build()
    }

    /**
     * Regular notification used on all devices, and the fallback whenever promotion is not
     * available or the notification doesn't meet the promotable shape.
     */
    private fun buildCompat(spec: LiveUpdateSpec): Notification {
        val builder = NotificationCompat.Builder(context, spec.channelId).apply {
            setSmallIcon(spec.smallIconRes)
            setContentTitle(spec.contentTitle)
            spec.contentText?.let { setContentText(it) }
            spec.subText?.let { setSubText(it) }
            setOngoing(spec.ongoing)
            // Same entity is always updated with the same notification ID, so never re-alert.
            setOnlyAlertOnce(true)
            setAutoCancel(spec.autoCancel)
            setPriority(spec.priority)
            spec.category?.let { setCategory(it) }
            spec.whenTime?.let { setWhen(it) }
            if (spec.usesChronometer) {
                setUsesChronometer(true)
                setChronometerCountDown(spec.chronometerCountDown)
            }
            if (spec.progressIndeterminate) {
                // Standard indeterminate progress on pre-36 devices (classic ProgressBar style).
                setProgress(0, 0, true)
            }
            spec.contentIntent?.let { setContentIntent(it) }
            spec.deleteIntent?.let { setDeleteIntent(it) }
            spec.actions.forEach { action ->
                addAction(action.iconRes, action.title, action.pendingIntent)
            }
            setVisibility(spec.visibility)
            spec.publicVersionTitle?.let { title ->
                val publicVersion = NotificationCompat.Builder(context, spec.channelId)
                    .setSmallIcon(spec.smallIconRes)
                    .setContentTitle(title)
                    .setContentText(spec.publicVersionText ?: "")
                    .build()
                setPublicVersion(publicVersion)
            }
        }
        return builder.build()
    }
}
