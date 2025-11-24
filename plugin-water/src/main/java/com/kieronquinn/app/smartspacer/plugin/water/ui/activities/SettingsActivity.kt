package com.kieronquinn.app.smartspacer.plugin.water.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings.WaterSettingsScreen
import com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings.WaterSettingsViewModel
import com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings.WaterSettingsViewModelImpl
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : FragmentActivity() {

    private val viewModel: WaterSettingsViewModel by viewModel<WaterSettingsViewModelImpl>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaterSettingsScreen(viewModel)
        }
    }

}
