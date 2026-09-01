package com.kieronquinn.app.smartspacer.plugin.shared.permissions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PluginFeatureGatesTest {

    @Test
    fun `notification denial does not block database or Smartspacer target`() {
        assertTrue(PluginFeatureGates.canPersistAndShowSmartspacerTarget())
        assertFalse(PluginFeatureGates.canPostNotification(notificationsGranted = false))
        assertTrue(PluginFeatureGates.canPostNotification(notificationsGranted = true))
        assertTrue(PluginFeatureGates.canUseShareOrManualPaste())
    }

    @Test
    fun `sms denial disables auto parse but share and manual stay available`() {
        assertFalse(
            PluginFeatureGates.canAutoParseSms(smsReceiveGranted = false, smsReadGranted = false)
        )
        assertFalse(
            PluginFeatureGates.canAutoParseSms(smsReceiveGranted = true, smsReadGranted = false)
        )
        assertTrue(
            PluginFeatureGates.canAutoParseSms(smsReceiveGranted = true, smsReadGranted = true)
        )
        assertTrue(PluginFeatureGates.canUseShareOrManualPaste())
    }

    @Test
    fun `promoted unavailable falls back to a normal notification`() {
        assertTrue(
            PluginFeatureGates.shouldFallBackToNormalNotification(
                atLeastBaklavaQpr1 = false,
                canPostPromotedNotifications = false
            )
        )
        assertTrue(
            PluginFeatureGates.shouldFallBackToNormalNotification(
                atLeastBaklavaQpr1 = true,
                canPostPromotedNotifications = false
            )
        )
        assertFalse(
            PluginFeatureGates.shouldFallBackToNormalNotification(
                atLeastBaklavaQpr1 = true,
                canPostPromotedNotifications = true
            )
        )
        assertFalse(
            PluginFeatureGates.canPostPromotedLiveUpdate(
                atLeastBaklavaQpr1 = true,
                canPostPromotedNotifications = false,
                notificationsGranted = true
            )
        )
    }
}
