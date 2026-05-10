package com.kieronquinn.app.smartspacer.plugin.qweather.repositories

import com.kieronquinn.app.smartspacer.plugin.qweather.R
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository

class NavGraphRepositoryImpl : NavGraphRepository {

    override fun getNavGraph(className: String): NavGraphRepository.NavGraphMapping? {
        return when (className) {
            ".ui.activities.SettingsActivity" -> NavGraphMapping.Settings
            else -> null
        }
    }

    sealed class NavGraphMapping(override val className: String, override val graph: Int) : NavGraphRepository.NavGraphMapping {
        object Settings : NavGraphMapping(".ui.activities.SettingsActivity", R.navigation.nav_graph_configuration)
    }

}
