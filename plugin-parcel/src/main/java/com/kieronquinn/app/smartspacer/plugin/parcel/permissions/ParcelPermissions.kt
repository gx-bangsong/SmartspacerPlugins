package com.kieronquinn.app.smartspacer.plugin.parcel.permissions

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginCapability
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginPermissionConfig

object ParcelPermissions {
    val config = PluginPermissionConfig(
        capabilities = listOf(
            PluginCapability.NOTIFICATIONS,
            PluginCapability.SMS_RECEIVE,
            PluginCapability.SMS_READ
        )
    )
}
