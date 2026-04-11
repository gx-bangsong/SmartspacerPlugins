package com.kieronquinn.app.smartspacer.plugin.food.repositories

import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.food.ui.activities.SettingsActivity

class NavGraphRepositoryImpl: NavGraphRepository {

    override fun getNavGraph(className: String): NavGraphRepository.NavGraphMapping? {
        return SettingsActivity.NavGraphMapping.values().firstOrNull {
            it.className == className
        }
    }

}
