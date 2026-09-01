package com.kieronquinn.app.smartspacer.plugin.food.permissions

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FoodPermissionsTest {

    @Test
    fun `food plugin only declares notifications and exact alarms`() {
        assertEquals(
            listOf(PluginCapability.NOTIFICATIONS, PluginCapability.EXACT_ALARMS),
            FoodPermissions.config.capabilities
        )
        assertFalse(FoodPermissions.config.capabilities.contains(PluginCapability.SMS_RECEIVE))
        assertFalse(FoodPermissions.config.capabilities.contains(PluginCapability.SMS_READ))
        assertFalse(FoodPermissions.config.capabilities.contains(PluginCapability.PROMOTED_LIVE_UPDATES))
    }
}
