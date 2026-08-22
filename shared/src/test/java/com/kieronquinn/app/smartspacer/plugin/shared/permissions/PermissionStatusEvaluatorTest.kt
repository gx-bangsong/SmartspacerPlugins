package com.kieronquinn.app.smartspacer.plugin.shared.permissions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionStatusEvaluatorTest {

    @Test
    fun `notifications below Android 13 do not require a runtime grant`() {
        val snapshot = PermissionStatusEvaluator.evaluateNotifications(32, runtimeGranted = false)
        assertEquals(CapabilityStatus.NOT_REQUIRED, snapshot.status)
        assertEquals(CapabilityAction.OPEN_APP_NOTIFICATION_SETTINGS, snapshot.action)
        assertTrue(snapshot.isSatisfied)
        assertFalse(snapshot.needsUserAction)
    }

    @Test
    fun `notifications on Android 13 plus request runtime permission when denied`() {
        val denied = PermissionStatusEvaluator.evaluateNotifications(33, runtimeGranted = false)
        assertEquals(CapabilityStatus.DENIED, denied.status)
        assertEquals(CapabilityAction.REQUEST_RUNTIME, denied.action)
        assertTrue(denied.needsUserAction)

        val granted = PermissionStatusEvaluator.evaluateNotifications(33, runtimeGranted = true)
        assertEquals(CapabilityStatus.GRANTED, granted.status)
        assertEquals(CapabilityAction.OPEN_APP_NOTIFICATION_SETTINGS, granted.action)
    }

    @Test
    fun `sms denial is a runtime request and does not look like a special permission`() {
        val denied = PermissionStatusEvaluator.evaluateSms(PluginCapability.SMS_RECEIVE, granted = false)
        assertEquals(CapabilityAction.REQUEST_RUNTIME, denied.action)
        val granted = PermissionStatusEvaluator.evaluateSms(PluginCapability.SMS_READ, granted = true)
        assertEquals(CapabilityStatus.GRANTED, granted.status)
        assertEquals(CapabilityAction.NONE, granted.action)
    }

    @Test
    fun `exact alarms never use requestPermissions`() {
        val denied = PermissionStatusEvaluator.evaluateExactAlarms(31, canScheduleExactAlarms = false)
        assertEquals(CapabilityStatus.DENIED, denied.status)
        assertEquals(CapabilityAction.OPEN_EXACT_ALARM_SETTINGS, denied.action)
        assertFalse(denied.action == CapabilityAction.REQUEST_RUNTIME)

        val granted = PermissionStatusEvaluator.evaluateExactAlarms(31, canScheduleExactAlarms = true)
        assertEquals(CapabilityStatus.GRANTED, granted.status)
        assertEquals(CapabilityAction.NONE, granted.action)

        val legacy = PermissionStatusEvaluator.evaluateExactAlarms(30, canScheduleExactAlarms = false)
        assertEquals(CapabilityStatus.NOT_REQUIRED, legacy.status)
    }

    @Test
    fun `live updates below 36_1 are unsupported and must not open settings`() {
        val snapshot = PermissionStatusEvaluator.evaluatePromotedLiveUpdates(
            atLeastBaklavaQpr1 = false,
            manifestPermissionGranted = true,
            canPostPromotedNotifications = true
        )
        assertEquals(CapabilityStatus.UNSUPPORTED, snapshot.status)
        assertEquals(CapabilityAction.NONE, snapshot.action)
        assertFalse(PermissionStatusEvaluator.canOpenPromotedSettings(false))
    }

    @Test
    fun `canPostPromotedNotifications false can enter settings`() {
        val snapshot = PermissionStatusEvaluator.evaluatePromotedLiveUpdates(
            atLeastBaklavaQpr1 = true,
            manifestPermissionGranted = true,
            canPostPromotedNotifications = false
        )
        assertEquals(CapabilityStatus.SETTINGS_DISABLED, snapshot.status)
        assertEquals(CapabilityAction.OPEN_PROMOTED_SETTINGS, snapshot.action)
        assertTrue(PermissionStatusEvaluator.canOpenPromotedSettings(true))
        assertTrue(snapshot.needsUserAction)
    }

    @Test
    fun `promoted eligible when QPR1 and toggle on`() {
        val snapshot = PermissionStatusEvaluator.evaluatePromotedLiveUpdates(
            atLeastBaklavaQpr1 = true,
            manifestPermissionGranted = true,
            canPostPromotedNotifications = true
        )
        assertEquals(CapabilityStatus.GRANTED, snapshot.status)
    }
}
