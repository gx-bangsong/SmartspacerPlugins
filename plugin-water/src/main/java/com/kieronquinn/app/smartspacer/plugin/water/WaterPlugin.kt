package com.kieronquinn.app.smartspacer.plugin.water

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.water.scheduling.DailyScheduleWorker
import com.kieronquinn.app.smartspacer.plugin.water.scheduling.WaterScheduler
import com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings.WaterSettingsViewModel
import com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings.WaterSettingsViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

class WaterPlugin: SmartspacerPlugin() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "water_reminders"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
        //Start the daily scheduler
        val workManager = WorkManager.getInstance(this)
        val request = OneTimeWorkRequestBuilder<DailyScheduleWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES) //Start in 1 minute for first run
            .build()
        workManager.enqueueUniqueWork("DailyScheduleWorker", ExistingWorkPolicy.KEEP, request)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Water Reminders"
            val descriptionText = "Notifications to remind you to drink water"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun getModule(context: Context) = module {
        single<WaterDataRepository> { WaterDataRepositoryImpl(get()) }
        single { WaterScheduler() }
        viewModel { WaterSettingsViewModelImpl(get()) as WaterSettingsViewModel }
    }

}