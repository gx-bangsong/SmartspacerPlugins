package com.kieronquinn.app.smartspacer.plugin.shared.notifications

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/**
 * Notification permission helpers shared by all plugins.
 *
 * `POST_NOTIFICATIONS` is a runtime permission on Android 13+ and must be requested from the
 * plugin's settings UI (never from a BroadcastReceiver). `POST_PROMOTED_NOTIFICATIONS` is a
 * non-runtime permission that simply needs to be declared in the manifest; on Android 16+ the
 * user can additionally disable promoted notifications per app from system settings.
 */
object NotificationPermissionHelper {

    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    /**
     * `android.permission.POST_PROMOTED_NOTIFICATIONS` — the non-runtime permission introduced
     * with Android 16 QPR1 (minor SDK 36.1). The Manifest.permission constant is absent from the
     * compileSdk 36 stubs, so the literal string is used.
     */
    const val POST_PROMOTED_NOTIFICATIONS = "android.permission.POST_PROMOTED_NOTIFICATIONS"

    fun hasPostPromotedPermission(context: Context): Boolean {
        return context.checkSelfPermission(POST_PROMOTED_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    /** Opens the per-app notification settings screen (works on all API levels). */
    fun openNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // No settings activity available — nothing else we can do.
        }
    }

    /**
     * Opens the per-app "promoted notifications / live updates" settings screen on Android 16
     * QPR1 (36.1) and newer. No-op below 36.1 — the settings page does not exist there.
     */
    fun openPromotedSettings(context: Context) {
        if (!LiveUpdateEligibility.isAtLeastBaklavaQpr1()) return
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            openNotificationSettings(context)
        }
    }
}
