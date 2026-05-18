package com.kieronquinn.app.smartspacer.plugin.qweather.complications

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
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

@RequiresApi(Build.VERSION_CODES.O)
class QWeatherComplication : SmartspacerComplicationProvider() {

    private val settingsRepository by inject<SettingsRepository>()
    private val qWeatherRepository by inject<QWeatherRepository>()

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
            ?: return listOf(getSetupAction("Loading weather data..."))

        val useEmoji = settingsRepository.useEmoji.getBlocking()

        val actions = mutableListOf<SmartspaceAction>()

        AdviceGenerator.generateActivityAdvice(weatherData.daily, useEmoji).forEachIndexed { index, advice ->
            actions.add(createAction("qweather_activity_advice_$index", advice))
        }

        AdviceGenerator.generateStatusAdvice(weatherData.daily, useEmoji).forEachIndexed { index, advice ->
            actions.add(createAction("qweather_status_advice_$index", advice))
        }

        return actions
    }

    private fun createAction(id: String, text: String): SmartspaceAction {
        return ComplicationTemplate.Basic(
            id = id,
            content = Text(text),
            icon = Icon(AndroidIcon.createWithResource(provideContext(), R.mipmap.ic_launcher), shouldTint = false),
            onClick = TapAction(
                intent = Intent(provideContext(), SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        ).create()
    }

    private fun getSetupAction(text: String = "Set up QWeather"): SmartspaceAction {
        return ComplicationTemplate.Basic(
            id ="qweather_setup",
            content = Text(text),
            icon = Icon(AndroidIcon.createWithResource(provideContext(), R.mipmap.ic_launcher), shouldTint = false),
            onClick = TapAction(
                intent = Intent(provideContext(), SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        ).create()
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = provideContext().getString(R.string.complication_qweather_label),
            description = provideContext().getString(R.string.complication_qweather_description),
            icon = AndroidIcon.createWithResource(provideContext(), R.mipmap.ic_launcher),
            configActivity = Intent(provideContext(), SettingsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
