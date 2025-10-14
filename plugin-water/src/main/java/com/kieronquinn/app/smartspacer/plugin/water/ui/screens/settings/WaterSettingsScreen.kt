package com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kieronquinn.app.smartspacer.plugin.water.repositories.DisplayMode

@Composable
fun WaterSettingsScreen(viewModel: WaterSettingsViewModel) {
    val dailyGoalMl by viewModel.dailyGoalMl.collectAsState()
    val cupMl by viewModel.cupMl.collectAsState()
    val activeStartMinutes by viewModel.activeStartMinutes.collectAsState()
    val activeEndMinutes by viewModel.activeEndMinutes.collectAsState()
    val displayMode by viewModel.displayMode.collectAsState()
    val resetAtActiveStart by viewModel.resetAtActiveStart.collectAsState()
    val smartAdjust by viewModel.smartAdjust.collectAsState()
    val snoozeMinutes by viewModel.snoozeMinutes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Water Reminder Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Daily Goal
        OutlinedTextField(
            value = dailyGoalMl.toString(),
            onValueChange = { viewModel.setDailyGoalMl(it.toIntOrNull() ?: 0) },
            label = { Text("Daily Goal (ml)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Cup Size
        OutlinedTextField(
            value = cupMl.toString(),
            onValueChange = { viewModel.setCupMl(it.toIntOrNull() ?: 0) },
            label = { Text("Cup Size (ml)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Active Hours
        // Note: Time pickers would require a more complex implementation, using text fields for now
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedTextField(
                value = minutesToTime(activeStartMinutes),
                onValueChange = { viewModel.setActiveStartMinutes(timeToMinutes(it)) },
                label = { Text("Active Hours Start") }
            )
            OutlinedTextField(
                value = minutesToTime(activeEndMinutes),
                onValueChange = { viewModel.setActiveEndMinutes(timeToMinutes(it)) },
                label = { Text("Active Hours End") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Display Mode
        Text("Display Mode", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = displayMode == DisplayMode.PROGRESS, onClick = { viewModel.setDisplayMode(DisplayMode.PROGRESS) })
            Text("Progress")
            RadioButton(selected = displayMode == DisplayMode.REMINDER, onClick = { viewModel.setDisplayMode(DisplayMode.REMINDER) })
            Text("Reminder")
            RadioButton(selected = displayMode == DisplayMode.DYNAMIC, onClick = { viewModel.setDisplayMode(DisplayMode.DYNAMIC) })
            Text("Dynamic")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Toggles
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = resetAtActiveStart, onCheckedChange = { viewModel.setResetAtActiveStart(it) })
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset at start of active hours")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = smartAdjust, onCheckedChange = { viewModel.setSmartAdjust(it) })
            Spacer(modifier = Modifier.width(8.dp))
            Text("Smart-adjust schedule")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Snooze Minutes
        OutlinedTextField(
            value = snoozeMinutes.toString(),
            onValuechange = { viewModel.setSnoozeMinutes(it.toIntOrNull() ?: 0) },
            label = { Text("Snooze Duration (minutes)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Developer / Debug", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.simulateReminder() }) {
            Text("Simulate Reminder Now")
        }
        Button(onClick = { viewModel.clearTodaySchedule() }) {
            Text("Clear Today's Schedule")
        }
    }
}

private fun minutesToTime(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return String.format("%02d:%02d", hours, mins)
}

private fun timeToMinutes(time: String): Int {
    return try {
        val parts = time.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        hours * 60 + minutes
    } catch (e: Exception) {
        0
    }
}