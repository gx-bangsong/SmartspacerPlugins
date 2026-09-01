package com.kieronquinn.app.smartspacer.plugin.medication.ui.activities

import androidx.annotation.NavigationRes
import com.kieronquinn.app.smartspacer.plugin.medication.R
import com.kieronquinn.app.smartspacer.plugin.medication.permissions.MedicationPermissions
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.PermissionOnboardingSettingsActivity

class SettingsActivity : PermissionOnboardingSettingsActivity() {

    override val permissionConfig: PluginPermissionConfig = MedicationPermissions.config


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
