package com.kieronquinn.app.smartspacer.plugin.water.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings.WaterSettingsScreen
import com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings.WaterSettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : ComponentActivity() {

    private val viewModel: WaterSettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaterSettingsScreen(viewModel)
        }
    }

}