package com.kieronquinn.app.smartspacer.plugin.qweather.complications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon as AndroidIcon
import android.os.Build
import androidx.annotation.RequiresApi
import com.kieronquinn.app.smartspacer.plugin.qweather.R
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.QWeatherRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.receivers.UpdateReceiver
import com.kieronquinn.app.smartspacer.plugin.qweather.ui.activities.SettingsActivity
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
class QWeatherComplication : SmartspacerComplicationProvider() {

    private val settingsRepository by inject<SettingsRepository>()
    private val qWeatherRepository by inject<QWeatherRepository>()
    private val alarmManager by lazy { provideContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
          val apiKey = settingsRepository.apiKey.getBlocking()
        val locationId = settingsRepository.locationId.getBlocking()

        if (apiKey.isBlank() || locationId.isBlank()) {
            return listOf(getSetupAction())
        }

        val weatherData = runBlocking { qWeatherRepository.weatherData.first() }
        val previousWeatherData = runBlocking { qWeatherRepository.previousWeatherData.first() }

        if (weatherData == null) {
            return listOf(getSetupAction("Loading weather data..."))
        }

        return weatherData.daily.map { daily ->
            val previousDaily = previousWeatherData?.daily?.find { it.type == daily.type }
            val (primaryText, secondaryText) = com.kieronquinn.app.smartspacer.plugin.qweather.utils.AdviceGenerator.generateAdvice(daily, previousDaily)
            
            // Use the builder pattern for SmartspaceAction
            SmartspaceAction.Builder(smartspacerId, provideContext().packageName)
                .setPrimaryText(Text(primaryText))
                .setSubtitle(Text(secondaryText))
                .setIcon(Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_launcher_foreground)))
                // TapAction is now set directly on the builder
                .setTapAction(TapAction(intent = Intent())) // The TapAction can be set to a specific intent if needed
                .build()
        }
    }

    private fun getSetupAction(secondaryText: String = "Tap to configure"): SmartspaceAction {
        // Use the builder pattern for the setup action
        return SmartspaceAction.Builder("qweather_setup", provideContext().packageName)
            .setPrimaryText(Text("Set up QWeather"))
            .setSubtitle(Text(secondaryText))
            .setIcon(Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_launcher_foreground)))
            .setTapAction(TapAction(intent = Intent(provideContext(), SettingsActivity::class.java)))
            .build()
    }

    // onProviderAdded and onProviderRemoved have been removed as they are no longer part of the Smartspacer SDK.
    // Logic for scheduling updates should be handled elsewhere, such as in the BootReceiver or the plugin's main entry point.

    private fun scheduleUpdates(smartspacerId: String) {
        val intent = Intent(provideContext(), UpdateReceiver::class.java).apply {
            putExtra(UpdateReceiver.EXTRA_SMARTSPACER_ID, smartspacerId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            provideContext(),
            smartspacerId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            0,
            TimeUnit.HOURS.toMillis(1),
            pendingIntent
        )
    }

    private fun cancelUpdates(smartspacerId: String) {
        val intent = Intent(provideContext(), UpdateReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            provideContext(),
            smartspacerId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = provideContext().getString(R.string.complication_qweather_label),
            description = provideContext().getString(R.string.complication_qweather_description),
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_launcher_foreground),
            configActivity = Intent(provideContext(), SettingsActivity::class.java)
        )
    }
}
