package com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.kieronquinn.app.smartspacer.plugin.water.repositories.DisplayMode

@Composable
fun WaterSettingsScreen(viewModel: WaterSettingsViewModel) {
    val uiState by viewModel.uiState.collectAsState(viewModel.initialState)
    val context = LocalContext.current as androidx.fragment.app.FragmentActivity

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Daily Goal: ${uiState.dailyGoalMl}ml")
        Slider(
            value = uiState.dailyGoalMl.toFloat(),
            onValueChange = { viewModel.onDailyGoalChanged(it) },
            valueRange = 500f..5000f,
            steps = 9
        )

        Text("Cup Size: ${uiState.cupSizeMl}ml")
        Slider(
            value = uiState.cupSizeMl.toFloat(),
            onValueChange = { viewModel.onCupSizeChanged(it) },
            valueRange = 100f..1000f,
            steps = 9
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Start Time: ${uiState.startTimeHour}:${uiState.startTimeMinute}")
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(uiState.startTimeHour)
                    .setMinute(uiState.startTimeMinute)
                    .build()
                    .apply {
                        addOnPositiveButtonClickListener {
                            viewModel.onStartTimeChanged(hour, minute)
                        }
                    }
                    .show(context.supportFragmentManager, "start_time_picker")
            }) {
                Text("Change")
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("End Time: ${uiState.endTimeHour}:${uiState.endTimeMinute}")
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(uiState.endTimeHour)
                    .setMinute(uiState.endTimeMinute)
                    .build()
                    .apply {
                        addOnPositiveButtonClickListener {
                            viewModel.onEndTimeChanged(hour, minute)
                        }
                    }
                    .show(context.supportFragmentManager, "end_time_picker")
            }) {
                Text("Change")
            }
        }

        var expanded by remember { mutableStateOf(false) }
        Text("Display Mode: ${uiState.displayMode}")
        Button(onClick = { expanded = true }) {
            Text("Change")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DisplayMode.values().forEach {
                DropdownMenuItem(onClick = {
                    viewModel.onDisplayModeChanged(it)
                    expanded = false
                }, text = { Text(it.name) })
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Reset at Active Start")
            Spacer(Modifier.weight(1f))
            Switch(
                checked = uiState.resetAtActiveStart,
                onCheckedChange = { viewModel.onResetAtActiveStartChanged(it) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Smart Adjust")
            Spacer(Modifier.weight(1f))
            Switch(
                checked = uiState.smartAdjust,
                onCheckedChange = { viewModel.onSmartAdjustChanged(it) }
            )
        }

        Text("Snooze: ${uiState.snoozeMinutes} minutes")
        Slider(
            value = uiState.snoozeMinutes.toFloat(),
            onValueChange = { viewModel.onSnoozeMinutesChanged(it) },
            valueRange = 5f..60f,
            steps = 11
        )

        Button(onClick = { viewModel.saveChanges(context) }) {
            Text("Save")
        }
    }
}
