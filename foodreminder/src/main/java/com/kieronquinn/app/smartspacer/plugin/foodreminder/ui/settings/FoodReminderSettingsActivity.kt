package com.kieronquinn.app.smartspacer.plugin.foodreminder.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.kieronquinn.app.smartspacer.plugin.foodreminder.ui.theme.FoodReminderTheme

class FoodReminderSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodReminderTheme {
                FoodReminderSettingsScreen()
            }
        }
    }
}