package com.kieronquinn.app.smartspacer.plugin.travel.permissions

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TravelPermissionsTest {

    @Test
    fun `travel plugin only declares the capabilities it actually uses`() {
        assertEquals(
            listOf(
                PluginCapability.NOTIFICATIONS,
                PluginCapability.SMS_RECEIVE,
                PluginCapability.SMS_READ,
                PluginCapability.EXACT_ALARMS,
                PluginCapability.PROMOTED_LIVE_UPDATES
            ),
            TravelPermissions.config.capabilities
        )
        assertTrue(TravelPermissions.config.capabilities.contains(PluginCapability.SMS_RECEIVE))
        assertTrue(TravelPermissions.config.capabilities.contains(PluginCapability.EXACT_ALARMS))
        assertTrue(TravelPermissions.config.capabilities.contains(PluginCapability.PROMOTED_LIVE_UPDATES))
    }
}
