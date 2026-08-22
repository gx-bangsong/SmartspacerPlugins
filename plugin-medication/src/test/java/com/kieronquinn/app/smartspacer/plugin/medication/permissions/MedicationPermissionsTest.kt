package com.kieronquinn.app.smartspacer.plugin.medication.permissions

import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PluginCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MedicationPermissionsTest {

    @Test
    fun `medication plugin only declares notifications and exact alarms`() {
        assertEquals(
            listOf(PluginCapability.NOTIFICATIONS, PluginCapability.EXACT_ALARMS),
            MedicationPermissions.config.capabilities
        )
        assertFalse(MedicationPermissions.config.capabilities.contains(PluginCapability.SMS_RECEIVE))
        assertFalse(MedicationPermissions.config.capabilities.contains(PluginCapability.SMS_READ))
        assertFalse(MedicationPermissions.config.capabilities.contains(PluginCapability.PROMOTED_LIVE_UPDATES))
    }
}
