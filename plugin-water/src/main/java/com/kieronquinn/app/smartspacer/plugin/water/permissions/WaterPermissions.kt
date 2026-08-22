package com.kieronquinn.app.smartspacer.plugin.water.permissions

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginCapability
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig

object WaterPermissions {
    val config = PluginPermissionConfig(
        capabilities = listOf(
            PluginCapability.NOTIFICATIONS,
            PluginCapability.EXACT_ALARMS
        )
    )
}
