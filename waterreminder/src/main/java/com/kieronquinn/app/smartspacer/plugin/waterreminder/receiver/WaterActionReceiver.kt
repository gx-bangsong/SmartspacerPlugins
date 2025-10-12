package com.kieronquinn.app.smartspacer.plugin.waterreminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.waterreminder.data.WaterReminderSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WaterActionReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val ACTION_DRINK_WATER = "ACTION_DRINK_WATER"
    }

    private val settings: WaterReminderSettings by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_DRINK_WATER) {
            settings.currentIntake += settings.cupSize
        }
    }
}