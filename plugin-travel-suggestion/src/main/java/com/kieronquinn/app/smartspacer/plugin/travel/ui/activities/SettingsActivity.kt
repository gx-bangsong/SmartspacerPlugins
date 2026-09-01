package com.kieronquinn.app.smartspacer.plugin.travel.ui.activities

import androidx.annotation.NavigationRes
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.PermissionOnboardingSettingsActivity
import com.kieronquinn.app.smartspacer.plugin.travel.R
import com.kieronquinn.app.smartspacer.plugin.travel.permissions.TravelPermissions

class SettingsActivity : PermissionOnboardingSettingsActivity() {

    override val permissionConfig: PluginPermissionConfig = TravelPermissions.config


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
