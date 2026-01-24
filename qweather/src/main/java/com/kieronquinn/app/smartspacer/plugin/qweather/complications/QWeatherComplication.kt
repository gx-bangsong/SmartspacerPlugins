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
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.getBlocking
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
        val locationName = settingsRepository.locationName.getBlocking()
        val cityLookupFailed = settingsRepository.cityLookupFailed.getBlocking()

        if (apiKey.isBlank() || locationName.isBlank()) {
            return listOf(getSetupAction())
        }

        if (cityLookupFailed) {
            return listOf(getSetupAction("City not found. Tap to re-enter."))
        }

        val weatherData = runBlocking { qWeatherRepository.weatherData.first() }
        val previousWeatherData = runBlocking { qWeatherRepository.previousWeatherData.first() }

        if (weatherData == null) {
            return listOf(getSetupAction("Loading weather data..."))
        }

        return weatherData.daily.map { daily ->
            val previousDaily = previousWeatherData?.daily?.find { it.type == daily.type }
            val (primaryText, secondaryText) = com.kieronquinn.app.smartspacer.plugin.qweather.utils.AdviceGenerator.generateAdvice(daily, previousDaily)

            // 确保 SmartspaceAction.Builder 被正确导入
            SmartspaceAction(
                id = "qweather_${daily.type}",
                title = primaryText,
                subtitle = secondaryText,
                icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_cloud),
                pendingIntent = PendingIntent.getActivity(
                    provideContext(),
                    0,
                    Intent(),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
    }

    private fun getSetupAction(secondaryText: String = "Tap to configure"): SmartspaceAction {
        // 确保 SmartspaceAction.Builder 被正确导入
        return SmartspaceAction(
            id ="qweather_setup",
            title = "Set up QWeather",
            subtitle = secondaryText,
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_cloud),
            pendingIntent = PendingIntent.getActivity(
                provideContext(),
                0,
                Intent(provideContext(), SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = provideContext().getString(R.string.complication_qweather_label),
            description = provideContext().getString(R.string.complication_qweather_description),
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_cloud),
            configActivity = Intent(provideContext(), SettingsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
