package com.kieronquinn.app.smartspacer.plugin.qweather.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.work.QWeatherVisibilityWorker
import com.kieronquinn.app.smartspacer.plugin.qweather.work.QWeatherWorker
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val settingsRepository by inject<SettingsRepository>()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        SmartspacerComplicationProvider.notifyChange(context, QWeatherComplication::class.java)
        QWeatherWorker.enqueuePeriodic(context)
        QWeatherVisibilityWorker.scheduleNext(context, settingsRepository)
    }
}
