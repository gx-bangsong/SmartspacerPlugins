package com.kieronquinn.app.smartspacer.plugin.shared.permissions

/**
 * Capabilities a plugin may require. Each APK must pass only the capabilities it actually uses —
 * SMS must not be requested by plugins that never parse SMS, exact alarms must not be requested
 * by plugins that never schedule time-sensitive reminders, and promoted Live Updates must not be
 * requested just to fill the onboarding wizard.
 */
enum class PluginCapability {
    NOTIFICATIONS,
    SMS_RECEIVE,
    SMS_READ,
    EXACT_ALARMS,
    PROMOTED_LIVE_UPDATES
}
