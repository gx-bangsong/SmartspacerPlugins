package com.kieronquinn.app.smartspacer.plugin.shared.permissions

/**
 * Live status of a [PluginCapability]. This is always derived from system APIs at evaluation
 * time — it is never persisted. "Wizard already shown" is stored separately as an onboarding
 * version integer.
 */
enum class CapabilityStatus {
    /** Runtime / special permission is granted, or the capability is not required on this SDK. */
    GRANTED,

    /** User (or the platform) has not granted the capability. */
    DENIED,

    /** No runtime grant is required on this platform version (e.g. POST_NOTIFICATIONS < 33). */
    NOT_REQUIRED,

    /** The platform does not support the capability (e.g. Live Updates below Android 16 QPR1). */
    UNSUPPORTED,

    /** Capability exists but the user/OEM toggle is off (promoted notifications). */
    SETTINGS_DISABLED
}

enum class CapabilityAction {
    NONE,
    REQUEST_RUNTIME,
    OPEN_APP_NOTIFICATION_SETTINGS,
    OPEN_EXACT_ALARM_SETTINGS,
    OPEN_PROMOTED_SETTINGS
}

data class CapabilitySnapshot(
    val capability: PluginCapability,
    val status: CapabilityStatus,
    val action: CapabilityAction
) {
    /** True when the settings page should highlight this capability as missing. */
    val needsUserAction: Boolean
        get() = status == CapabilityStatus.DENIED

    /** True when the UI should treat the capability as satisfied. */
    val isSatisfied: Boolean
        get() = status == CapabilityStatus.GRANTED || status == CapabilityStatus.NOT_REQUIRED
}
