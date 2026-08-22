package com.kieronquinn.app.smartspacer.plugin.checkin.permissions

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginCapability
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig

object CheckInPermissions {
    val config = PluginPermissionConfig(
        capabilities = listOf(
            PluginCapability.NOTIFICATIONS,
            PluginCapability.EXACT_ALARMS
        )
    )
}
