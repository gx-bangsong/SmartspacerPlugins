package com.kieronquinn.app.smartspacer.plugin.waterreminder.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.getViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterReminderSettingsScreen(
    viewModel: WaterReminderSettingsViewModel = getViewModel()
) {
    val dailyGoal by viewModel.dailyGoalMl.collectAsState(initial = 2000)
    val cupSize by viewModel.cupSizeMl.collectAsState(initial = 250)
    val startHour by viewModel.activeHourStart.collectAsState(initial = 8)
    val endHour by viewModel.activeHourEnd.collectAsState(initial = 22)
    val spacerStyle by viewModel.spacerStyle.collectAsState(initial = 0)
    val progressCups by viewModel.currentProgressCups.collectAsState(initial = 0)

    var dailyGoalText by remember(dailyGoal) { mutableStateOf(dailyGoal.toString()) }
    var cupSizeText by remember(cupSize) { mutableStateOf(cupSize.toString()) }

    val goalCups = if (cupSize > 0) (dailyGoal + cupSize - 1) / cupSize else 0

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Water Reminder Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Hydration Goal", style = MaterialTheme.typography.titleLarge)
            TextField(
                value = dailyGoalText,
                onValueChange = {
                    dailyGoalText = it
                    it.toIntOrNull()?.let { value -> viewModel.setDailyGoal(value) }
                },
                label = { Text("Daily Goal (ml)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = cupSizeText,
                onValueChange = {
                    cupSizeText = it
                    it.toIntOrNull()?.let { value -> viewModel.setCupSize(value) }
                },
                label = { Text("Cup/Bottle Size (ml)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Manual Progress", style = MaterialTheme.typography.titleLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { viewModel.adjustProgress(-1) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease progress")
                }
                Text(
                    text = "$progressCups / $goalCups cups",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(120.dp)
                )
                IconButton(onClick = { viewModel.adjustProgress(1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Increase progress")
                }
            }

            Text("Active Hours", style = MaterialTheme.typography.titleLarge)
            val timeFormatter = DateTimeFormatter.ofPattern("h a")
            Text("From: ${LocalTime.of(startHour, 0).format(timeFormatter)}")
            Slider(
                value = startHour.toFloat(),
                onValueChange = { viewModel.setActiveHours(it.roundToInt(), endHour) },
                valueRange = 0f..23f,
                steps = 22
            )
            Text("To: ${LocalTime.of(endHour, 0).format(timeFormatter)}")
            Slider(
                value = endHour.toFloat(),
                onValueChange = { viewModel.setActiveHours(startHour, it.roundToInt()) },
                valueRange = 0f..23f,
                steps = 22
            )

            Text("Spacer Display Style", style = MaterialTheme.typography.titleLarge)
            SpacerStyleDropdown(
                selectedStyle = spacerStyle,
                onStyleSelected = { viewModel.setSpacerStyle(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpacerStyleDropdown(selectedStyle: Int, onStyleSelected: (Int) -> Unit) {
    val options = listOf("Progress: 2 / 8 cups", "Next reminder time", "Combined view")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = options.getOrElse(selectedStyle) { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Style") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, text ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onStyleSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}