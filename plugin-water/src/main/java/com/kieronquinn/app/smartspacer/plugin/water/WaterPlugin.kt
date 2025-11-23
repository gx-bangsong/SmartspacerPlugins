package com.kieronquinn.app.smartspacer.plugin.water

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepositoryImpl
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
        single { com.kieronquinn.app.smartspacer.plugin.water.data.WaterDatabase.getDatabase(get()).drinkHistoryDao() }
        single<WaterDataRepository> { WaterDataRepositoryImpl(context, get()) }
        single { WaterScheduler() }
        viewModel { WaterSettingsViewModelImpl(get()) }
        factory<WaterSettingsViewModel> { get<WaterSettingsViewModelImpl>() }
    }

}
