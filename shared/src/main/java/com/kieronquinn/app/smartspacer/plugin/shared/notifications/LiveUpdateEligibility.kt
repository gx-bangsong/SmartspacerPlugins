package com.kieronquinn.app.smartspacer.plugin.shared.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Runtime eligibility checks for Android "promoted ongoing" (Live Update) notifications.
 *
 * Android 16 (API 36, BAKLAVA) introduced the promoted-ongoing notification surface, and the
 * Android 16 QPR1 (minor SDK 36.1) release added the explicit opt-in request API
 * (`Notification.Builder#setRequestPromotedOngoing`) and the progress-centric
 * `Notification.ProgressStyle#setProgress*` methods. Because the framework feature is
 * feature-flagged and OEM dependent, `SDK_INT >= 36` alone must NEVER be treated as "promoted
 * Live Updates work here". The authoritative runtime checks are:
 *
 *  - [isAtLeastBaklavaQpr1]: the device runs Android 16 QPR1 (36.1) or newer. Mirrors the
 *    official androidx `BuildCompat.isAtLeastB_1()` check, which compares
 *    `Build.VERSION.SDK_INT_FULL > Build.VERSION_CODES_FULL.BAKLAVA` (both introduced with the
 *    major/minor versioning scheme in Android 16). Reading `SDK_INT_FULL` is only safe on
 *    API 36+ devices, hence the short-circuit on [isPlatformSupported].
 *  - [NotificationManager.canPostPromotedNotifications] (API 36): returns whether this app is
 *    currently allowed to post promoted notifications (considers the manifest permission, the
 *    user's per-app switch and the framework feature flag / OEM implementation).
 *  - [Notification.hasPromotableCharacteristics] (API 36): validates the notification itself.
 *    On the initial 36.0 release this method used the colourised/legacy promotion spec, while
 *    36.1/QPR requires the explicit request extra. Calling it after building is the only
 *    reliable way to know whether the notification will actually be promoted.
 *
 * All checks in this class are safe to call on any API level; they simply report "not eligible"
 * below the required platform, so callers fall back to a normal notification.
 */
object LiveUpdateEligibility {

    /** The minimum platform version exposing the promoted-ongoing APIs. */
    const val PLATFORM_MIN_SDK = Build.VERSION_CODES.BAKLAVA // 36

    /**
     * The minor SDK level (Android 16 QPR1) that added the opt-in promoted-ongoing request
     * (`setRequestPromotedOngoing`) and the progress-centric ProgressStyle methods.
     */
    const val PLATFORM_MIN_MINOR_SDK = 1

    enum class Result {
        /** Device + app + user all allow promoted notifications. */
        ELIGIBLE,

        /** Device below Android 16 (API 36). */
        NOT_SUPPORTED,

        /** The non-runtime manifest permission is missing. */
        PERMISSION_MISSING,

        /** The feature exists but is disabled for this app on this device. */
        DISABLED,

        /** The app is not allowed to post normal notifications (POST_NOTIFICATIONS). */
        POST_NOTIFICATIONS_DENIED
    }

    /**
     * Pure decision function (unit-testable without a device).
     *
     * @param platformSupported  `Build.VERSION.SDK_INT >= 36`
     * @param manifestPermissionGranted `android.permission.POST_PROMOTED_NOTIFICATIONS` granted
     * @param notificationsAllowed whether the app may post notifications at all
     * @param canPostPromoted    value of [NotificationManager.canPostPromotedNotifications]
     */
    fun evaluate(
        platformSupported: Boolean,
        manifestPermissionGranted: Boolean,
        notificationsAllowed: Boolean,
        canPostPromoted: Boolean
    ): Result {
        if (!notificationsAllowed) return Result.POST_NOTIFICATIONS_DENIED
        if (!platformSupported) return Result.NOT_SUPPORTED
        if (!manifestPermissionGranted) return Result.PERMISSION_MISSING
        if (!canPostPromoted) return Result.DISABLED
        return Result.ELIGIBLE
    }

    fun isPlatformSupported(sdkInt: Int): Boolean = sdkInt >= PLATFORM_MIN_SDK

    /**
     * Runtime detection of Android 16 QPR1 (minor SDK 36.1) or newer.
     *
     * This mirrors the official androidx `BuildCompat.isAtLeastB_1()` implementation. The full
     * SDK version (`ro.build.version.sdk_full`) is a runtime value, so on a 36.0 device
     * `SDK_INT_FULL` is either absent/0 (flag off) or equals the Baklava base, both of which
     * yield `false` here; only a 36.1+ device reports a value greater than the Baklava base.
     * The comparison is only evaluated after the `SDK_INT >= 36` short-circuit, because the
     * `SDK_INT_FULL` field does not exist below API 36.
     */
    /**
     * Pure decision function for the 36.1+ check (unit-testable on the JVM).
     *
     * @param sdkInt        `Build.VERSION.SDK_INT` (major API level, 36 on all Android 16 devices)
     * @param sdkIntFull    `Build.VERSION.SDK_INT_FULL` (runtime full version, encoding
     *                      `major * 100000 + minor`, e.g. 3600000 on 36.0 and 3600001 on 36.1)
     * @param baklavaFull   `Build.VERSION_CODES_FULL.BAKLAVA` (compile-time constant, 3600000)
     */
    fun isAtLeastMinorSdk(sdkInt: Int, sdkIntFull: Int, baklavaFull: Int): Boolean =
        sdkInt >= PLATFORM_MIN_SDK && sdkIntFull > baklavaFull

    fun isAtLeastBaklavaQpr1(): Boolean {
        if (!isPlatformSupported(Build.VERSION.SDK_INT)) return false
        return try {
            @Suppress("FlaggedApi") // Flagged in the SDK; runtime-gated by the version check above (same approach as androidx BuildCompat).
            isAtLeastMinorSdk(Build.VERSION.SDK_INT, Build.VERSION.SDK_INT_FULL, Build.VERSION_CODES_FULL.BAKLAVA)
        } catch (e: Throwable) {
            false
        }
    }

    fun hasPostPromotedManifestPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.POST_PROMOTED_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    /**
     * Full runtime check. Returns true only when the current device/app/user combination can
     * actually post a promoted ongoing notification.
     */
    fun isPromotedSupported(context: Context): Boolean {
        return evaluate(
            platformSupported = isPlatformSupported(Build.VERSION.SDK_INT),
            manifestPermissionGranted = hasPostPromotedManifestPermission(context),
            notificationsAllowed = NotificationPermissionHelper.areNotificationsEnabled(context),
            canPostPromoted = canPostPromotedNotifications(context)
        ) == Result.ELIGIBLE
    }

    /**
     * Wraps [NotificationManager.canPostPromotedNotifications] with API gating. Returns false
     * below API 36 or when the service is unavailable.
     */
    fun canPostPromotedNotifications(context: Context): Boolean {
        if (!isPlatformSupported(Build.VERSION.SDK_INT)) return false
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return false
        return try {
            @Suppress("FlaggedApi") // Flagged in the SDK; runtime-gated by the version check above.
            manager.canPostPromotedNotifications()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Wraps [Notification.hasPromotableCharacteristics] with API gating. On the initial 36.0
     * release this uses the legacy (colourised) promotion spec, so apps targeting the 36.1
     * opt-in behaviour may see `false` there; that is expected and callers must simply fall back
     * to the normal notification.
     */
    fun hasPromotableCharacteristics(notification: Notification?): Boolean {
        if (notification == null || !isPlatformSupported(Build.VERSION.SDK_INT)) return false
        return try {
            @Suppress("FlaggedApi") // Flagged in the SDK; runtime-gated by the version check above.
            notification.hasPromotableCharacteristics()
        } catch (e: Exception) {
            false
        }
    }
}
