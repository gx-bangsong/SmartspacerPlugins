package com.kieronquinn.app.smartspacer.plugin.food.ui.activities

import androidx.annotation.NavigationRes
import com.kieronquinn.app.smartspacer.plugin.food.R
import com.kieronquinn.app.smartspacer.plugin.food.permissions.FoodPermissions
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.PermissionOnboardingSettingsActivity

class SettingsActivity : PermissionOnboardingSettingsActivity() {

    override val permissionConfig: PluginPermissionConfig = FoodPermissions.config


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
