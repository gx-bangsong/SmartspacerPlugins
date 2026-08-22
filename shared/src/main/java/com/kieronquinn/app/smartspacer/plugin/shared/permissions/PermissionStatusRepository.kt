package com.kieronquinn.app.smartspacer.plugin.shared.permissions

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.LiveUpdateEligibility
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationPermissionHelper

/**
 * Live permission status. Always reads the current system APIs — never a cached "granted"
 * boolean from onboarding storage.
 */
class PermissionStatusRepository(private val context: Context) {

    fun snapshot(capability: PluginCapability): CapabilitySnapshot {
        return when (capability) {
            PluginCapability.NOTIFICATIONS -> PermissionStatusEvaluator.evaluateNotifications(
                sdkInt = Build.VERSION.SDK_INT,
                runtimeGranted = NotificationPermissionHelper.hasNotificationPermission(context)
            )
            PluginCapability.SMS_RECEIVE -> PermissionStatusEvaluator.evaluateSms(
                PluginCapability.SMS_RECEIVE,
                isGranted(Manifest.permission.RECEIVE_SMS)
            )
            PluginCapability.SMS_READ -> PermissionStatusEvaluator.evaluateSms(
                PluginCapability.SMS_READ,
                isGranted(Manifest.permission.READ_SMS)
            )
            PluginCapability.EXACT_ALARMS -> PermissionStatusEvaluator.evaluateExactAlarms(
                sdkInt = Build.VERSION.SDK_INT,
                canScheduleExactAlarms = canScheduleExactAlarms()
            )
            PluginCapability.PROMOTED_LIVE_UPDATES ->
                PermissionStatusEvaluator.evaluatePromotedLiveUpdates(
                    atLeastBaklavaQpr1 = LiveUpdateEligibility.isAtLeastBaklavaQpr1(),
                    manifestPermissionGranted =
                        LiveUpdateEligibility.hasPostPromotedManifestPermission(context),
                    canPostPromotedNotifications =
                        LiveUpdateEligibility.canPostPromotedNotifications(context)
                )
        }
    }

    fun snapshots(capabilities: List<PluginCapability>): List<CapabilitySnapshot> {
        return capabilities.map { snapshot(it) }
    }

    fun missingCount(capabilities: List<PluginCapability>): Int {
        return PermissionStatusEvaluator.missingCount(snapshots(capabilities))
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return false
        return alarmManager.canScheduleExactAlarms()
    }
}
