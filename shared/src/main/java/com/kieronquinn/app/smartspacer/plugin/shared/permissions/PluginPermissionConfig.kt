package com.kieronquinn.app.smartspacer.plugin.shared.permissions

/**
 * Per-plugin declaration of the capabilities that plugin's APK actually needs, plus the
 * onboarding version. Bumping [onboardingVersion] makes already-onboarded users see the wizard
 * once more (incremental onboarding).
 */
data class PluginPermissionConfig(
    val capabilities: List<PluginCapability>,
    val onboardingVersion: Int = PermissionOnboardingCoordinator.CURRENT_VERSION
) {
    init {
        require(capabilities == capabilities.distinct()) {
            "Duplicate capabilities are not allowed: $capabilities"
        }
    }
}
