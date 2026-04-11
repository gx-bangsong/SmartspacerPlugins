package com.kieronquinn.app.smartspacer.plugin.parcel.ui.activities

import androidx.annotation.NavigationRes
import com.kieronquinn.app.smartspacer.plugin.parcel.R
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity

class SettingsActivity : BaseConfigurationActivity() {

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
