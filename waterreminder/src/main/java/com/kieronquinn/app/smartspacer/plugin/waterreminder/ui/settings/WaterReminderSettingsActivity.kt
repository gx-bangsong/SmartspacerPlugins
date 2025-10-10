package com.kieronquinn.app.smartspacer.plugin.waterreminder.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kieronquinn.app.smartspacer.plugin.waterreminder.ui.theme.WaterReminderTheme

class WaterReminderSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaterReminderTheme {
                WaterReminderSettingsScreen()
            }
        }
    }
}