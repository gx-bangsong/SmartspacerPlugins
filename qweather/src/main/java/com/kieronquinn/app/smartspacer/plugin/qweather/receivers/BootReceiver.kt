package com.kieronquinn.app.smartspacer.plugin.qweather.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        // Re-schedule alarms for all active complications
        val complication = QWeatherComplication()
        val activeComplications = SmartspacerComplicationProvider.getActiveComplications(context, complication.getComponentName(context))
        activeComplications.forEach {
            complication.onProviderAdded(it)
        }
    }
}