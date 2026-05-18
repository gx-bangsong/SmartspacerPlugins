package com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kieronquinn.app.smartspacer.plugin.medication.data.Medication
import com.kieronquinn.app.smartspacer.plugin.medication.data.ScheduleType
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onSave: (Medication) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var name by rememberSaveable { mutableStateOf("") }
    var dosage by rememberSaveable { mutableStateOf("") }
    var scheduleType by rememberSaveable { mutableStateOf(ScheduleType.SPECIFIC_TIMES) }
    var startDate by rememberSaveable { mutableStateOf(dateFormat.format(Date())) }
    var endDate by rememberSaveable { mutableStateOf("") }

    val reminderTimes = remember { mutableStateListOf<String>() }
    var intervalValue by rememberSaveable { mutableStateOf("") }
    var selectedStartTime by rememberSaveable { mutableStateOf("") }
    var selectedWeekdays by rememberSaveable { mutableStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Add Medication") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            val medication = validateAndCreateMedication(
                                name, dosage, startDate, endDate, scheduleType,
                                reminderTimes, intervalValue, selectedStartTime, selectedWeekdays,
                                dateFormat
                            )
                            if (medication != null) {
                                onSave(medication)
                            } else {
                                android.widget.Toast.makeText(context, "Please fill in all required fields", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Save")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medication Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage (e.g. 1 pill)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Schedule Type", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ScheduleType.entries.forEachIndexed { index, type ->
                        val label = when (type) {
                            ScheduleType.SPECIFIC_TIMES -> "Times"
                            ScheduleType.EVERY_X_HOURS -> "Hours"
                            ScheduleType.EVERY_X_DAYS -> "Days"
                            ScheduleType.SPECIFIC_WEEKDAYS -> "Week"
                        }
                        SegmentedButton(
                            selected = scheduleType == type,
                            onClick = { scheduleType = type },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = ScheduleType.entries.size)
                        ) {
                            Text(label)
                        }
                    }
                }

                when (scheduleType) {
                    ScheduleType.SPECIFIC_TIMES -> SpecificTimesConfig(reminderTimes)
                    ScheduleType.EVERY_X_HOURS -> IntervalConfig(
                        label = "Every X hours",
                        value = intervalValue,
                        onValueChange = { intervalValue = it },
                        startTime = selectedStartTime,
                        onStartTimeClick = { showTimePicker(context) { selectedStartTime = it } }
                    )
                    ScheduleType.EVERY_X_DAYS -> IntervalConfig(
                        label = "Every X days",
                        value = intervalValue,
                        onValueChange = { intervalValue = it },
                        startTime = selectedStartTime,
                        onStartTimeClick = { showTimePicker(context) { selectedStartTime = it } }
                    )
                    ScheduleType.SPECIFIC_WEEKDAYS -> WeekdaysConfig(
                        selectedWeekdays = selectedWeekdays,
                        onWeekdaysChange = { selectedWeekdays = it },
                        startTime = selectedStartTime,
                        onStartTimeClick = { showTimePicker(context) { selectedStartTime = it } }
                    )
                }

                OutlinedTextField(
                    value = startDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start Date") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            showDatePicker(context, calendar) { startDate = dateFormat.format(it) }
                        }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                        }
                    }
                )

                OutlinedTextField(
                    value = endDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End Date (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            showDatePicker(context, calendar) { endDate = dateFormat.format(it) }
                        }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpecificTimesConfig(reminderTimes: MutableList<String>) {
    val context = LocalContext.current
    Text("Reminder Times", style = MaterialTheme.typography.titleMedium)
    reminderTimes.forEach { time ->
        ListItem(
            headlineContent = { Text(time) },
            trailingContent = {
                IconButton(onClick = { reminderTimes.remove(time) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        )
    }
    FilledTonalButton(
        onClick = { showTimePicker(context) { if (!reminderTimes.contains(it)) reminderTimes.add(it) } },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Add Time")
    }
    Text("Shortcuts", style = MaterialTheme.typography.titleSmall)
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            "1 time/day" to listOf("08:00"),
            "2 times/day" to listOf("08:00", "20:00"),
            "3 times/day" to listOf("08:00", "14:00", "20:00"),
            "4 times/day" to listOf("08:00", "12:00", "16:00", "20:00")
        ).forEach { (label, times) ->
            SuggestionChip(onClick = {
                reminderTimes.clear()
                reminderTimes.addAll(times)
            }, label = { Text(label) })
        }
    }
}

@Composable
fun IntervalConfig(label: String, value: String, onValueChange: (String) -> Unit, startTime: String, onStartTimeClick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
    )
    OutlinedTextField(
        value = startTime,
        onValueChange = {},
        readOnly = true,
        label = { Text("Start Time") },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = { IconButton(onClick = onStartTimeClick) { Icon(Icons.Default.AccessTime, contentDescription = "Select Time") } }
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeekdaysConfig(selectedWeekdays: Int, onWeekdaysChange: (Int) -> Unit, startTime: String, onStartTimeClick: () -> Unit) {
    val weekdays = listOf("Mon" to Calendar.MONDAY, "Tue" to Calendar.TUESDAY, "Wed" to Calendar.WEDNESDAY, "Thu" to Calendar.THURSDAY, "Fri" to Calendar.FRIDAY, "Sat" to Calendar.SATURDAY, "Sun" to Calendar.SUNDAY)
    Text("Select Weekdays", style = MaterialTheme.typography.titleMedium)
    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        weekdays.forEach { (name, day) ->
            FilterChip(
                selected = (selectedWeekdays and (1 shl day)) != 0,
                onClick = {
                    val newValue = if ((selectedWeekdays and (1 shl day)) != 0) selectedWeekdays and (1 shl day).inv() else selectedWeekdays or (1 shl day)
                    onWeekdaysChange(newValue)
                },
                label = { Text(name) }
            )
        }
    }
    OutlinedTextField(
        value = startTime,
        onValueChange = {},
        readOnly = true,
        label = { Text("Time") },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = { IconButton(onClick = onStartTimeClick) { Icon(Icons.Default.AccessTime, contentDescription = "Select Time") } }
    )
}

private fun showTimePicker(context: android.content.Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    TimePickerDialog(context, { _, hour, minute -> onTimeSelected(String.format("%02d:%02d", hour, minute)) }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
}

private fun showDatePicker(context: android.content.Context, calendar: Calendar, onDateSelected: (Date) -> Unit) {
    DatePickerDialog(context, { _, year, month, day ->
        calendar.set(year, month, day)
        onDateSelected(calendar.time)
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
}

private fun validateAndCreateMedication(name: String, dosage: String, startDateStr: String, endDateStr: String, scheduleType: ScheduleType, reminderTimes: List<String>, intervalValue: String, selectedStartTime: String, selectedWeekdays: Int, dateFormat: SimpleDateFormat): Medication? {
    if (name.isBlank() || startDateStr.isBlank()) return null
    val startDate = try { dateFormat.parse(startDateStr)?.time ?: return null } catch (e: Exception) { return null }
    val endDate = if (endDateStr.isNotBlank()) try { dateFormat.parse(endDateStr)?.time } catch (e: Exception) { null } else null
    val intervalHours = if (scheduleType == ScheduleType.EVERY_X_HOURS) intervalValue.toIntOrNull() else null
    val intervalDays = if (scheduleType == ScheduleType.EVERY_X_DAYS) intervalValue.toIntOrNull() else null
    when (scheduleType) {
        ScheduleType.SPECIFIC_TIMES -> if (reminderTimes.isEmpty()) return null
        ScheduleType.EVERY_X_HOURS -> if (intervalHours == null || selectedStartTime.isBlank()) return null
        ScheduleType.EVERY_X_DAYS -> if (intervalDays == null || selectedStartTime.isBlank()) return null
        ScheduleType.SPECIFIC_WEEKDAYS -> if (selectedWeekdays == 0 || selectedStartTime.isBlank()) return null
    }
    val medication = Medication(
        name = name, dosage = dosage, startDate = startDate, endDate = endDate, isUnlimited = endDate == null, scheduleType = scheduleType, intervalHours = intervalHours, intervalDays = intervalDays,
        timesOfDay = if (scheduleType == ScheduleType.SPECIFIC_TIMES) com.google.gson.Gson().toJson(reminderTimes) else selectedStartTime,
        weekdays = if (scheduleType == ScheduleType.SPECIFIC_WEEKDAYS) selectedWeekdays else null, nextDoseTs = 0L
    )
    return medication.copy(nextDoseTs = MedicationUtils.calculateNextDose(medication))
}
