package com.kieronquinn.app.smartspacer.plugin.shared.ui.activities

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PermissionOnboardingLauncher
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig

/**
 * [BaseConfigurationActivity] that auto-launches the permission wizard the first time the
 * plugin's SettingsActivity is opened for a given onboarding version.
 */
abstract class PermissionOnboardingSettingsActivity : BaseConfigurationActivity() {

    protected abstract val permissionConfig: PluginPermissionConfig

    override fun onResume() {
        super.onResume()
        PermissionOnboardingLauncher.maybeLaunch(this, permissionConfig)
    }
}
