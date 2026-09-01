package com.kieronquinn.app.smartspacer.plugin.water.permissions

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WaterPermissionsTest {

    @Test
    fun `water plugin only declares notifications and exact alarms`() {
        assertEquals(
            listOf(PluginCapability.NOTIFICATIONS, PluginCapability.EXACT_ALARMS),
            WaterPermissions.config.capabilities
        )
        assertFalse(WaterPermissions.config.capabilities.contains(PluginCapability.SMS_RECEIVE))
        assertFalse(WaterPermissions.config.capabilities.contains(PluginCapability.SMS_READ))
        assertFalse(WaterPermissions.config.capabilities.contains(PluginCapability.PROMOTED_LIVE_UPDATES))
    }
}
