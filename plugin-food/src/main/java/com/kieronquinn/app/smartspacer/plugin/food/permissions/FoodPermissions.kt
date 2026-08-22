package com.kieronquinn.app.smartspacer.plugin.food.permissions

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginCapability
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig

object FoodPermissions {
    val config = PluginPermissionConfig(
        capabilities = listOf(
            PluginCapability.NOTIFICATIONS,
            PluginCapability.EXACT_ALARMS
        )
    )
}
