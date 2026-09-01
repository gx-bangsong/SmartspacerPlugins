package com.kieronquinn.app.smartspacer.plugin.travel.permissions

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginCapability
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig

object TravelPermissions {
    val config = PluginPermissionConfig(
        capabilities = listOf(
            PluginCapability.NOTIFICATIONS,
            PluginCapability.SMS_RECEIVE,
            PluginCapability.SMS_READ,
            PluginCapability.EXACT_ALARMS,
            PluginCapability.PROMOTED_LIVE_UPDATES
        )
    )
}
