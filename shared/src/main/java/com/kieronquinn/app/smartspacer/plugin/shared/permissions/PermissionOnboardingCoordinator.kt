package com.kieronquinn.app.smartspacer.plugin.shared.permissions

/**
 * Decides whether the first-launch permission wizard should auto-show, and which capabilities
 * that plugin's APK actually presents. Auto-show is driven only by the stored onboarding
 * version — never by whether permissions are currently granted — so refusing or skipping does
 * not cause the wizard to harass the user on every subsequent launch.
 */
class PermissionOnboardingCoordinator(
    private val store: OnboardingVersionStore,
    private val config: PluginPermissionConfig
) {

    val capabilities: List<PluginCapability> = config.capabilities

    val currentVersion: Int = config.onboardingVersion

    fun shouldAutoShow(): Boolean = store.shownVersion < currentVersion

    /**
     * Marks the current onboarding version as presented. Called when the wizard is launched
     * (not when every permission is granted) so skip / deny / back never re-trigger auto-show.
     */
    fun markShown() {
        store.shownVersion = currentVersion
    }

    /**
     * Settings-page "re-run permission wizard" does not change the stored version; it just
     * launches the UI. Exposed as a named method so tests document the contract.
     */
    fun canRerunFromSettings(): Boolean = true

    companion object {
        const val CURRENT_VERSION = 1
    }
}
