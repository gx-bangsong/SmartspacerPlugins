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
import com.kieronquinn.app.smartspacer.plugin.qweather.ui.activities.SettingsActivity
import com.kieronquinn.app.smartspacer.plugin.qweather.utils.AdviceGenerator
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

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

        if (weatherData == null || weatherData.daily.isEmpty()) {
            return listOf(getSetupAction("Loading weather data..."))
        }

        val actions = mutableListOf<SmartspaceAction>()

        // Generate and add activity advice action
        val activityAdvice = AdviceGenerator.generateActivityAdvice(weatherData.daily)
        if (activityAdvice != null) {
            actions.add(
                SmartspaceAction(
                    id = "qweather_activity_summary",
                    title = "生活建议",
                    subtitle = activityAdvice,
                    icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_launcher_foreground),
                    pendingIntent = PendingIntent.getActivity(
                        provideContext(),
                        1, // Use different request code to avoid conflicts
                        Intent(provideContext(), SettingsActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        },
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            )
        }

        // Generate and add status advice action
        val statusAdvice = AdviceGenerator.generateStatusAdvice(weatherData.daily)
        if (statusAdvice != null) {
            actions.add(
                SmartspaceAction(
                    id = "qweather_status_summary",
                    title = "状态摘要",
                    subtitle = statusAdvice,
                    icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_launcher_foreground),
                    pendingIntent = PendingIntent.getActivity(
                        provideContext(),
                        2, // Use different request code
                        Intent(provideContext(), SettingsActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        },
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            )
        }

        // If no actions could be generated, show loading state
        if (actions.isEmpty()){
             return listOf(getSetupAction("Loading weather data..."))
        }

        return actions
    }

    private fun getSetupAction(secondaryText: String = "Tap to configure"): SmartspaceAction {
        return SmartspaceAction(
            id ="qweather_setup",
            title = "Set up QWeather",
            subtitle = secondaryText,
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_launcher_foreground),
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
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_launcher_foreground),
            configActivity = Intent(provideContext(), SettingsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
