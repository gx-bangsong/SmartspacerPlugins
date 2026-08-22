package com.kieronquinn.app.smartspacer.plugin.shared.permissions

/**
 * Pure decision functions for permission onboarding. Unit-testable without a device or
 * instrumentation — callers inject the live system-API values.
 */
object PermissionStatusEvaluator {

    const val TIRAMISU = 33
    const val S = 31

    fun evaluateNotifications(sdkInt: Int, runtimeGranted: Boolean): CapabilitySnapshot {
        if (sdkInt < TIRAMISU) {
            return CapabilitySnapshot(
                PluginCapability.NOTIFICATIONS,
                CapabilityStatus.NOT_REQUIRED,
                CapabilityAction.OPEN_APP_NOTIFICATION_SETTINGS
            )
        }
        return if (runtimeGranted) {
            CapabilitySnapshot(
                PluginCapability.NOTIFICATIONS,
                CapabilityStatus.GRANTED,
                CapabilityAction.OPEN_APP_NOTIFICATION_SETTINGS
            )
        } else {
            CapabilitySnapshot(
                PluginCapability.NOTIFICATIONS,
                CapabilityStatus.DENIED,
                CapabilityAction.REQUEST_RUNTIME
            )
        }
    }

    fun evaluateSms(capability: PluginCapability, granted: Boolean): CapabilitySnapshot {
        require(
            capability == PluginCapability.SMS_RECEIVE || capability == PluginCapability.SMS_READ
        ) { "evaluateSms only accepts SMS capabilities, got $capability" }
        return if (granted) {
            CapabilitySnapshot(capability, CapabilityStatus.GRANTED, CapabilityAction.NONE)
        } else {
            CapabilitySnapshot(capability, CapabilityStatus.DENIED, CapabilityAction.REQUEST_RUNTIME)
        }
    }

    fun evaluateExactAlarms(sdkInt: Int, canScheduleExactAlarms: Boolean): CapabilitySnapshot {
        if (sdkInt < S) {
            return CapabilitySnapshot(
                PluginCapability.EXACT_ALARMS,
                CapabilityStatus.NOT_REQUIRED,
                CapabilityAction.NONE
            )
        }
        return if (canScheduleExactAlarms) {
            CapabilitySnapshot(
                PluginCapability.EXACT_ALARMS,
                CapabilityStatus.GRANTED,
                CapabilityAction.NONE
            )
        } else {
            CapabilitySnapshot(
                PluginCapability.EXACT_ALARMS,
                CapabilityStatus.DENIED,
                CapabilityAction.OPEN_EXACT_ALARM_SETTINGS
            )
        }
    }

    /**
     * Promoted Live Updates are a non-runtime manifest permission plus a user/OEM toggle.
     * Below Android 16 QPR1 (36.1) the capability is [CapabilityStatus.UNSUPPORTED] and the
     * wizard must not open an invalid settings page.
     */
    fun evaluatePromotedLiveUpdates(
        atLeastBaklavaQpr1: Boolean,
        manifestPermissionGranted: Boolean,
        canPostPromotedNotifications: Boolean
    ): CapabilitySnapshot {
        if (!atLeastBaklavaQpr1) {
            return CapabilitySnapshot(
                PluginCapability.PROMOTED_LIVE_UPDATES,
                CapabilityStatus.UNSUPPORTED,
                CapabilityAction.NONE
            )
        }
        if (!manifestPermissionGranted) {
            return CapabilitySnapshot(
                PluginCapability.PROMOTED_LIVE_UPDATES,
                CapabilityStatus.DENIED,
                CapabilityAction.NONE
            )
        }
        return if (canPostPromotedNotifications) {
            CapabilitySnapshot(
                PluginCapability.PROMOTED_LIVE_UPDATES,
                CapabilityStatus.GRANTED,
                CapabilityAction.OPEN_PROMOTED_SETTINGS
            )
        } else {
            CapabilitySnapshot(
                PluginCapability.PROMOTED_LIVE_UPDATES,
                CapabilityStatus.SETTINGS_DISABLED,
                CapabilityAction.OPEN_PROMOTED_SETTINGS
            )
        }
    }

    fun canOpenPromotedSettings(atLeastBaklavaQpr1: Boolean): Boolean = atLeastBaklavaQpr1

    fun missingCount(snapshots: List<CapabilitySnapshot>): Int =
        snapshots.count { it.needsUserAction }
}
