package com.kieronquinn.app.smartspacer.plugin.parcel

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun `test Koin is started when application is created`() {
        // Robolectric handles application creation, which calls attachBaseContext and onCreate
        // Check if Koin GlobalContext has been started
        val koin = GlobalContext.getOrNull()
        assertNotNull("Koin should be started by ParcelPlugin", koin)
    }
}
