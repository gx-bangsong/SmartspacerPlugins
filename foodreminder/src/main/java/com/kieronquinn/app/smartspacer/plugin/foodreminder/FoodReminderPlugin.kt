package com.kieronquinn.app.smartspacer.plugin.foodreminder

import android.content.Context
import android.content.ComponentName
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.kieronquinn.smartspacer.sdk.SmartspacerPlugin
import com.kieronquinn.smartspacer.sdk.SmartspacerProvider
import com.kieronquinn.smartspacer.sdk.feature.sublist.SubListItem
import com.kieronquinn.smartspacer.sdk.feature.sublist.FEATURE_SUB_LIST
import java.util.concurrent.TimeUnit

class FoodReminderPlugin: com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin() {
    override fun getModule(context: Context): Module {
        return foodReminderModule
    }

    override fun onCreate() {
        super.onCreate()
        val workRequest = PeriodicWorkRequestBuilder<FoodReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }
}