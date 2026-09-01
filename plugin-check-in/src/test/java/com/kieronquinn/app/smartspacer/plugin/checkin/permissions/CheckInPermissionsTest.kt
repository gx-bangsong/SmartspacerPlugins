package com.kieronquinn.app.smartspacer.plugin.checkin.permissions

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CheckInPermissionsTest {

    @Test
    fun `check-in plugin only declares notifications and exact alarms`() {
        assertEquals(
            listOf(PluginCapability.NOTIFICATIONS, PluginCapability.EXACT_ALARMS),
            CheckInPermissions.config.capabilities
        )
        assertFalse(CheckInPermissions.config.capabilities.contains(PluginCapability.SMS_RECEIVE))
        assertFalse(CheckInPermissions.config.capabilities.contains(PluginCapability.SMS_READ))
        assertFalse(CheckInPermissions.config.capabilities.contains(PluginCapability.PROMOTED_LIVE_UPDATES))
    }
}
