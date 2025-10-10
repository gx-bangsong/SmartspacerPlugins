package com.kieronquinn.app.smartspacer.plugin.qweather.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        
        // This is a simplified approach. The more robust solution would be for the main app
        // to manage boot updates. For now, we simply notify a generic change.
        val componentName = ComponentName(context, QWeatherComplication::class.java)
        SmartspacerComplicationProvider.notifyChange(context, componentName)
    }
}
