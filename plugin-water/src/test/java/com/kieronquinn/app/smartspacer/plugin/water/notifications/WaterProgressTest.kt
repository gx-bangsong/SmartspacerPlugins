package com.kieronquinn.app.smartspacer.plugin.water.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WaterProgressTest {

    @Test
    fun `capsule is the ml fraction`() {
        val progress = WaterProgress(drankMl = 750, goalMl = 2000, cupMl = 250)
        assertEquals("750/2000", progress.capsule)
        assertEquals(37, progress.percent)
        assertFalse(progress.reached)
    }

    @Test
    fun `goal reached cancels the live update`() {
        val progress = WaterProgress(drankMl = 2000, goalMl = 2000, cupMl = 250)
        assertTrue(progress.reached)
        assertEquals(100, progress.percent)
        assertEquals("2000/2000", progress.capsule)
    }
}
