package com.kieronquinn.app.smartspacer.plugin.shared.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * Central notification controller for this repository's plugins.
 *
 * It renders [LiveUpdateSpec]s either as promoted ongoing notifications (Live Updates) or as
 * regular [NotificationCompat] notifications. Promotion is requested through the androidx compat
 * API surface (`NotificationCompat.Builder#setRequestPromotedOngoing` and
 * `NotificationCompat.ProgressStyle`, added in androidx.core 1.17.0) so the code compiles
 * against compileSdk 36 without needing the Android 16 QPR1 (36.1) minor SDK; the compat calls
 * are only *invoked* when the device actually runs 36.1+ (see
 * [LiveUpdateEligibility.isAtLeastBaklavaQpr1]), because the platform's opt-in request API and
 * the progress-style methods do not exist on the initial Android 16.0 release.
 *
 * Every requirement of the promoted-ongoing checklist is satisfied:
 *  - `POST_PROMOTED_NOTIFICATIONS` manifest permission (declared per app, checked by
 *    [LiveUpdateEligibility.isPromotedSupported]);
 *  - `setRequestPromotedOngoing(true)` + `setOngoing(true)` + non-empty contentTitle;
 *  - standard styles only ([NotificationCompat.ProgressStyle] for progress, plain otherwise);
 *  - channel with importance > [NotificationManager.IMPORTANCE_MIN];
 *  - no custom content view, no colorized, not a group summary;
 *  - [NotificationCompat.VISIBILITY_PRIVATE] with a redacted public version for lock screens.
 *
 * [LiveUpdateEligibility.hasPromotableCharacteristics] is consulted before posting so that on
 * Android 16.0 (where the opt-in flag is disabled) we fall back to the normal notification.
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
            LiveUpdateEligibility.isPlatformSupported(Build.VERSION.SDK_INT) &&
            LiveUpdateEligibility.isPromotedSupported(context)

        val notification = buildCompat(spec, promoted)

        // Always post the builder we actually requested. Rebuilding with promoted=false
        // strips setRequestPromotedOngoing and shortCriticalText, which removes the
        // status-bar capsule entirely. If the framework still cannot promote, it simply
        // renders this as a normal ongoing notification — same as the old fallback,
        // without losing the chip request.
        notificationManager.notify(spec.notificationId, notification)
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
     * Builds the notification with [NotificationCompat]. When [promoted] is true the builder
     * additionally requests promoted treatment (`setRequestPromotedOngoing`), applies the
     * progress-centric [NotificationCompat.ProgressStyle] and sets the status-chip critical text.
     * The caller guarantees [promoted] is only true on Android 16 QPR1 (36.1)+ devices.
     */
    private fun buildCompat(spec: LiveUpdateSpec, promoted: Boolean): Notification {
        val builder = NotificationCompat.Builder(context, spec.channelId).apply {
            setSmallIcon(spec.smallIconRes)
            setContentTitle(spec.contentTitle)
            spec.contentText?.let { setContentText(it) }
            spec.subText?.let { setSubText(it) }
            setOngoing(spec.ongoing || promoted)
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
            if (promoted) {
                // Compat APIs are safe on API 36.0 (they set extras / no-op). Always use
                // indeterminate ProgressStyle — setProgress(0) with no segments is an invalid
                // bar and OEMs drop the Live Update chip entirely.
                setRequestPromotedOngoing(true)
                setStyle(NotificationCompat.ProgressStyle().setProgressIndeterminate(true))
                spec.shortCriticalText
                    ?.toString()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { setShortCriticalText(it) }
            } else if (spec.progressIndeterminate) {
                // Standard indeterminate progress on pre-36.1 devices (classic ProgressBar style).
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
