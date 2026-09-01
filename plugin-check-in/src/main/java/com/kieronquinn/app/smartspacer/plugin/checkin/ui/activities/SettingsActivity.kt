package com.kieronquinn.app.smartspacer.plugin.checkin.ui.activities

import androidx.annotation.NavigationRes
import com.kieronquinn.app.smartspacer.plugin.checkin.R
import com.kieronquinn.app.smartspacer.plugin.checkin.permissions.CheckInPermissions
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.PermissionOnboardingSettingsActivity

class SettingsActivity : PermissionOnboardingSettingsActivity() {

    override val permissionConfig: PluginPermissionConfig = CheckInPermissions.config


    enum class NavGraphMapping(
        override val className: String,
        @NavigationRes override val graph: Int
    ): NavGraphRepository.NavGraphMapping {
        SETTINGS(
            ".ui.activities.SettingsActivity",
            R.navigation.nav_graph_configuration
        )
    }

}
