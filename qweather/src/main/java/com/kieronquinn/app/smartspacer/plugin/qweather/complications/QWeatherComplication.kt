package com.kieronquinn.app.smartspacer.plugin.qweather.complications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit
import android.graphics.drawable.Icon as AndroidIcon

class QWeatherComplication : SmartspacerComplicationProvider() {

    private val settingsRepository by inject<SettingsRepository>()
    private val qWeatherRepository by inject<QWeatherRepository>()
    private val alarmManager by lazy { provideContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val apiKey = runBlocking { settingsRepository.apiKey.get() }
        val locationId = runBlocking { settingsRepository.locationId.get() }

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
            ComplicationTemplate.Basic(
                id = "qweather_${daily.type}",
                icon = Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_launcher_foreground)),
                primaryText = Text(primaryText),
                secondaryText = Text(secondaryText)
            ).create()
        }
    }

    private fun getSetupAction(secondaryText: String = "Tap to configure"): SmartspaceAction {
        return ComplicationTemplate.Basic(
            id = "qweather_setup",
            icon = Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_launcher_foreground)),
            primaryText = Text("Set up QWeather"),
            secondaryText = Text(secondaryText),
            tapAction = TapAction(intent = Intent(provideContext(), SettingsActivity::class.java))
        ).create()
    }

    override fun onProviderAdded(smartspacerId: String) {
        super.onProviderAdded(smartspacerId)
        scheduleUpdates(smartspacerId)
    }

    override fun onProviderRemoved(smartspacerId: String) {
        super.onProviderRemoved(smartspacerId)
        cancelUpdates(smartspacerId)
    }

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
            label = getString(R.string.complication_qweather_label),
            description = getString(R.string.complication_qweather_description),
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_launcher_foreground),
            configActivity = Intent(provideContext(), SettingsActivity::class.java)
        )
    }
}