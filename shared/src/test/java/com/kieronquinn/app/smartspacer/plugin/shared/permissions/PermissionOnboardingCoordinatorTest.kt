package com.kieronquinn.app.smartspacer.plugin.shared.permissions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionOnboardingCoordinatorTest {

    private fun coordinator(
        shown: Int = 0,
        version: Int = 1,
        capabilities: List<PluginCapability> = listOf(PluginCapability.NOTIFICATIONS)
    ): PermissionOnboardingCoordinator {
        return PermissionOnboardingCoordinator(
            InMemoryOnboardingVersionStore(shown),
            PluginPermissionConfig(capabilities, version)
        )
    }

    @Test
    fun `first launch auto-shows when no version has been stored`() {
        assertTrue(coordinator(shown = 0, version = 1).shouldAutoShow())
    }

    @Test
    fun `skip or complete does not auto-show again`() {
        val store = InMemoryOnboardingVersionStore(0)
        val coordinator = PermissionOnboardingCoordinator(
            store,
            PluginPermissionConfig(listOf(PluginCapability.NOTIFICATIONS), 1)
        )
        assertTrue(coordinator.shouldAutoShow())
        coordinator.markShown()
        assertEquals(1, store.shownVersion)
        assertFalse(coordinator.shouldAutoShow())
        assertFalse(coordinator.shouldAutoShow())
    }

    @Test
    fun `settings can re-run the wizard after it has been shown`() {
        val coordinator = coordinator(shown = 1, version = 1)
        assertFalse(coordinator.shouldAutoShow())
        assertTrue(coordinator.canRerunFromSettings())
    }

    @Test
    fun `bumping onboarding version shows an incremental wizard once`() {
        val store = InMemoryOnboardingVersionStore(1)
        val v2 = PermissionOnboardingCoordinator(
            store,
            PluginPermissionConfig(listOf(PluginCapability.NOTIFICATIONS, PluginCapability.EXACT_ALARMS), 2)
        )
        assertTrue(v2.shouldAutoShow())
        v2.markShown()
        assertFalse(v2.shouldAutoShow())
        assertEquals(2, store.shownVersion)
    }

    @Test
    fun `shown version is independent of live permission grants`() {
        val coordinator = coordinator(shown = 1, version = 1)
        assertFalse(coordinator.shouldAutoShow())
    }

    @Test
    fun `only the capabilities passed to the coordinator are visible`() {
        val travel = coordinator(
            capabilities = listOf(
                PluginCapability.NOTIFICATIONS,
                PluginCapability.SMS_RECEIVE,
                PluginCapability.SMS_READ,
                PluginCapability.EXACT_ALARMS,
                PluginCapability.PROMOTED_LIVE_UPDATES
            )
        )
        val parcel = coordinator(
            capabilities = listOf(
                PluginCapability.NOTIFICATIONS,
                PluginCapability.SMS_RECEIVE,
                PluginCapability.SMS_READ
            )
        )
        val water = coordinator(
            capabilities = listOf(PluginCapability.NOTIFICATIONS, PluginCapability.EXACT_ALARMS)
        )
        assertFalse(travel.capabilities.contains(PluginCapability.SMS_RECEIVE).not())
        assertFalse(parcel.capabilities.contains(PluginCapability.EXACT_ALARMS))
        assertFalse(parcel.capabilities.contains(PluginCapability.PROMOTED_LIVE_UPDATES))
        assertFalse(water.capabilities.contains(PluginCapability.SMS_RECEIVE))
        assertFalse(water.capabilities.contains(PluginCapability.SMS_READ))
        assertFalse(water.capabilities.contains(PluginCapability.PROMOTED_LIVE_UPDATES))
        assertEquals(
            listOf(PluginCapability.NOTIFICATIONS, PluginCapability.EXACT_ALARMS),
            water.capabilities
        )
    }
}
