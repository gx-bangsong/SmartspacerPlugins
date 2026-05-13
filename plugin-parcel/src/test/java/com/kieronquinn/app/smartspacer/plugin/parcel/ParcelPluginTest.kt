package com.kieronquinn.app.smartspacer.plugin.parcel

import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = ParcelPlugin::class)
class ParcelPluginTest {

    @Test
    fun testKoinIsStarted() {
        val context = RuntimeEnvironment.getApplication()
        try {
            WorkManagerTestInitHelper.initializeTestWorkManager(context)
        } catch (e: Exception) {}

        val koin = GlobalContext.getOrNull()
        assertNotNull("Koin should be started", koin)
    }
}
