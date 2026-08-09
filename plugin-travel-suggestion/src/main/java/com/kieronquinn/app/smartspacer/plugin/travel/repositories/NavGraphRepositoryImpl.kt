package com.kieronquinn.app.smartspacer.plugin.travel.repositories

import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.travel.ui.activities.SettingsActivity

class NavGraphRepositoryImpl: NavGraphRepository {

    override fun getNavGraph(className: String): NavGraphRepository.NavGraphMapping? {
        return SettingsActivity.NavGraphMapping.values().firstOrNull {
            it.className == className
        }
    }

}
