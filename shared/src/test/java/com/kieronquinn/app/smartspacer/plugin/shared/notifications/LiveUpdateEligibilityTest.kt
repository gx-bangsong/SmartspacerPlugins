package com.kieronquinn.app.smartspacer.plugin.shared.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveUpdateEligibilityTest {

    // ---------------------------------------------------------------- platform support

    @Test
    fun `platform support requires API 36 or higher`() {
        assertFalse(LiveUpdateEligibility.isPlatformSupported(29))
        assertFalse(LiveUpdateEligibility.isPlatformSupported(33))
        assertFalse(LiveUpdateEligibility.isPlatformSupported(35))
        assertTrue(LiveUpdateEligibility.isPlatformSupported(36))
        assertTrue(LiveUpdateEligibility.isPlatformSupported(37))
    }

    // ---------------------------------------------------------------- evaluation matrix

    @Test
    fun `eligible when everything is in place`() {
        assertEquals(
            LiveUpdateEligibility.Result.ELIGIBLE,
            LiveUpdateEligibility.evaluate(
                platformSupported = true,
                manifestPermissionGranted = true,
                notificationsAllowed = true,
                canPostPromoted = true
            )
        )
    }

    @Test
    fun `old Android falls back to normal notifications`() {
        assertEquals(
            LiveUpdateEligibility.Result.NOT_SUPPORTED,
            LiveUpdateEligibility.evaluate(
                platformSupported = false,
                manifestPermissionGranted = true,
                notificationsAllowed = true,
                canPostPromoted = true
            )
        )
    }

    @Test
    fun `missing manifest permission blocks promotion`() {
        assertEquals(
            LiveUpdateEligibility.Result.PERMISSION_MISSING,
            LiveUpdateEligibility.evaluate(
                platformSupported = true,
                manifestPermissionGranted = false,
                notificationsAllowed = true,
                canPostPromoted = true
            )
        )
    }

    @Test
    fun `user or OEM disabled promoted notifications blocks promotion`() {
        // canPostPromotedNotifications() == false covers: user switch off, OEM not implemented,
        // feature flag disabled on the initial Android 16.0 release.
        assertEquals(
            LiveUpdateEligibility.Result.DISABLED,
            LiveUpdateEligibility.evaluate(
                platformSupported = true,
                manifestPermissionGranted = true,
                notificationsAllowed = true,
                canPostPromoted = false
            )
        )
    }

    @Test
    fun `denied POST_NOTIFICATIONS blocks everything`() {
        assertEquals(
            LiveUpdateEligibility.Result.POST_NOTIFICATIONS_DENIED,
            LiveUpdateEligibility.evaluate(
                platformSupported = true,
                manifestPermissionGranted = true,
                notificationsAllowed = false,
                canPostPromoted = true
            )
        )
    }

    @Test
    fun `sdk 36 alone is never enough - the runtime user toggle still gates it`() {
        // The exact scenario the docs warn about: SDK_INT >= 36 must not be treated as full
        // support; canPostPromotedNotifications() is the authoritative runtime check.
        assertEquals(
            LiveUpdateEligibility.Result.DISABLED,
            LiveUpdateEligibility.evaluate(
                platformSupported = true,
                manifestPermissionGranted = true,
                notificationsAllowed = true,
                canPostPromoted = false
            )
        )
    }

    // ---------------------------------------------------------------- 36.1 minor SDK detection

    // Official encoding (AOSP Build.parseFullVersion): SDK_INT_FULL = major * 100000 + minor,
    // so 36.0 -> 3600000 and 36.1 -> 3600001.
    private val baklavaFull = 3_600_000 // Build.VERSION_CODES_FULL.BAKLAVA = 36 * 100000

    @Test
    fun `36_1 device is detected as at-least-Baklava-QPR1`() {
        assertTrue(LiveUpdateEligibility.isAtLeastMinorSdk(36, 3_600_001, baklavaFull))
    }

    @Test
    fun `36_0 device is not detected as QPR1 even though SDK_INT is 36`() {
        // The exact trap: SDK_INT == 36 on both 36.0 and 36.1; the minor check disambiguates.
        assertFalse(LiveUpdateEligibility.isAtLeastMinorSdk(36, 3_600_000, baklavaFull))
        // Unset/absent sdk_full property (flag disabled) also reports not-QPR1:
        assertFalse(LiveUpdateEligibility.isAtLeastMinorSdk(36, 0, baklavaFull))
    }

    @Test
    fun `devices below API 36 are never QPR1`() {
        assertFalse(LiveUpdateEligibility.isAtLeastMinorSdk(35, 99_000_000, baklavaFull))
        assertFalse(LiveUpdateEligibility.isAtLeastMinorSdk(29, 99_000_000, baklavaFull))
    }

    @Test
    fun `android 17 and later are also QPR1 or newer`() {
        assertTrue(LiveUpdateEligibility.isAtLeastMinorSdk(37, 3_700_000, baklavaFull))
    }

    // ---------------------------------------------------------------- promotion shape

    @Test
    fun `promotable shape requires ongoing and title and a proper channel`() {
        val base = LiveUpdateSpec(
            channelId = "c",
            channelNameRes = 0,
            notificationId = 1,
            smallIconRes = 0,
            contentTitle = "title",
            ongoing = true,
            requestPromoted = true
        )
        assertTrue(base.isPromotableShape)

        assertFalse(base.copy(ongoing = false).isPromotableShape)
        assertFalse(base.copy(requestPromoted = false).isPromotableShape)
        assertFalse(base.copy(contentTitle = "").isPromotableShape)
        assertFalse(
            base.copy(channelImportance = android.app.NotificationManager.IMPORTANCE_MIN)
                .isPromotableShape
        )
    }
}
