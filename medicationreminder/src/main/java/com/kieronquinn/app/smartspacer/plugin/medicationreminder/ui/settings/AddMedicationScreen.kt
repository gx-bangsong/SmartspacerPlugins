package com.kieronquinn.app.smartspacer.plugin.medicationreminder.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.utils.formatDate
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.utils.formatTime
import org.koin.androidx.compose.getViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    viewModel: AddMedicationViewModel = getViewModel(),
    onSaveClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.startDate)
        DatePickerDialog(
            onDismissRequest = { viewModel.onEvent(AddMedicationFormEvent.ShowStartDatePicker(false)) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onEvent(AddMedicationFormEvent.StartDateChanged(it))
                    }
                    viewModel.onEvent(AddMedicationFormEvent.ShowStartDatePicker(false))
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(AddMedicationFormEvent.ShowStartDatePicker(false)) }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (uiState.showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.endDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { viewModel.onEvent(AddMedicationFormEvent.ShowEndDatePicker(false)) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onEvent(AddMedicationFormEvent.EndDateChanged(it))
                    }
                    viewModel.onEvent(AddMedicationFormEvent.ShowEndDatePicker(false))
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(AddMedicationFormEvent.ShowEndDatePicker(false)) }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

     if (uiState.showTimePickerForSpecificDays) {
        val timePickerState = rememberTimePickerState(initialHour = uiState.specificDaysTime.hour, initialMinute = uiState.specificDaysTime.minute)
        TimePickerDialog(
            onDismissRequest = { viewModel.onEvent(AddMedicationFormEvent.ShowTimePickerForSpecificDays(false)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(AddMedicationFormEvent.TimeForSpecificDaysChanged(LocalTime.of(timePickerState.hour, timePickerState.minute)))
                }) { Text("OK") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Medication") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                TextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onEvent(AddMedicationFormEvent.NameChanged(it)) },
                    label = { Text("Medication Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                TextField(
                    value = uiState.dosage,
                    onValueChange = { viewModel.onEvent(AddMedicationFormEvent.DosageChanged(it)) },
                    label = { Text("Dosage (e.g., 1 pill)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.onEvent(AddMedicationFormEvent.ShowStartDatePicker(true)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Start: ${uiState.startDate.formatDate()}")
                    }
                    OutlinedButton(
                        onClick = { viewModel.onEvent(AddMedicationFormEvent.ShowEndDatePicker(true)) },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isEndDateUnlimited
                    ) {
                        Text(uiState.endDate?.formatDate() ?: "End Date")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uiState.isEndDateUnlimited,
                        onCheckedChange = { viewModel.onEvent(AddMedicationFormEvent.EndDateUnlimitedToggled(it)) }
                    )
                    Text("No end date")
                }
            }
            item {
                ScheduleSelector(uiState, viewModel::onEvent)
            }
            item {
                Button(
                    onClick = {
                        viewModel.saveMedication()
                        onSaveClicked()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.name.isNotBlank()
                ) {
                    Text("Save Medication")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleSelector(
    uiState: AddMedicationFormState,
    onEvent: (AddMedicationFormEvent) -> Unit
) {
    Column {
        Text("Schedule", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        val scheduleTypes = ScheduleType.values()
        scheduleTypes.forEach { scheduleType ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(AddMedicationFormEvent.ScheduleTypeChanged(scheduleType)) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = uiState.scheduleType == scheduleType,
                    onClick = { onEvent(AddMedicationFormEvent.ScheduleTypeChanged(scheduleType)) }
                )
                Spacer(Modifier.width(8.dp))
                Text(scheduleType.name.replace('_', ' ').lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
            }
        }

        Spacer(Modifier.height(16.dp))

        when (uiState.scheduleType) {
            ScheduleType.EVERY_X_HOURS -> {
                TextField(
                    value = uiState.everyXHours.toString(),
                    onValueChange = { onEvent(AddMedicationFormEvent.EveryXHoursChanged(it.toIntOrNull() ?: 0)) },
                    label = { Text("Interval in hours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            ScheduleType.SPECIFIC_TIMES -> {
                Text("Add specific times (e.g., 9:00 AM, 5:00 PM)")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.specificTimes.forEach { time ->
                        InputChip(
                            selected = false,
                            onClick = { /* Not used */ },
                            label = { Text(time.formatTime()) },
                            trailingIcon = {
                                IconButton(onClick = { onEvent(AddMedicationFormEvent.SpecificTimeRemoved(time)) }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Remove time")
                                }
                            }
                        )
                    }
                    Button(onClick = { onEvent(AddMedicationFormEvent.SpecificTimeAdded(LocalTime.now())) }) {
                        Text("Add Time")
                    }
                }
            }
            ScheduleType.EVERY_X_DAYS -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = uiState.everyXDays.toString(),
                        onValueChange = { onEvent(AddMedicationFormEvent.EveryXDaysChanged(it.toIntOrNull() ?: 1)) },
                        label = { Text("Interval in days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = { onEvent(AddMedicationFormEvent.ShowTimePickerForSpecificDays(true)) }) {
                        Text(uiState.specificDaysTime.formatTime())
                    }
                }
            }
            ScheduleType.SPECIFIC_DAYS_OF_WEEK -> {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DayOfWeek.values().forEach { day ->
                        val isSelected = uiState.specificDaysOfWeek.contains(day)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onEvent(AddMedicationFormEvent.DayOfWeekToggled(day)) },
                            label = { Text(day.name.substring(0, 3)) }
                        )
                    }
                }
                OutlinedButton(
                    onClick = { onEvent(AddMedicationFormEvent.ShowTimePickerForSpecificDays(true)) },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("At time: ${uiState.specificDaysTime.formatTime()}")
                }
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Time") },
        text = content,
        confirmButton = confirmButton
    )
}