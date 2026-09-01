package com.kieronquinn.app.smartspacer.plugin.parcel.permissions

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ParcelPermissionsTest {

    @Test
    fun `parcel plugin only declares notifications and sms`() {
        assertEquals(
            listOf(
                PluginCapability.NOTIFICATIONS,
                PluginCapability.SMS_RECEIVE,
                PluginCapability.SMS_READ
            ),
            ParcelPermissions.config.capabilities
        )
        assertFalse(ParcelPermissions.config.capabilities.contains(PluginCapability.EXACT_ALARMS))
        assertFalse(ParcelPermissions.config.capabilities.contains(PluginCapability.PROMOTED_LIVE_UPDATES))
    }
}
