package com.kieronquinn.app.smartspacer.plugin.foodreminder.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItem
import org.koin.androidx.compose.getViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FoodReminderSettingsScreen(viewModel: FoodReminderSettingsViewModel = getViewModel()) {
    val foodItems by viewModel.foodItems.collectAsState()
    var foodName by remember { mutableStateOf("") }
    var storageMethod by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    val leadTimeDays by viewModel.reminderLeadTimeDays.collectAsState(initial = 1)

    val onAddFoodItem = { expiryDate: Long ->
        if (foodName.isNotBlank()) {
            viewModel.addFoodItem(foodName, storageMethod, expiryDate)
            foodName = ""
            storageMethod = ""
            selectedDate = null
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    selectedDate = datePickerState.selectedDateMillis
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Add New Food Item", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            TextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("Food Name") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            TextField(
                value = storageMethod,
                onValueChange = { storageMethod = it },
                label = { Text("Storage (e.g., Fridge)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("Set Expiry", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date", modifier = Modifier.padding(end = 8.dp))
                    Text(
                        selectedDate?.let {
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "Select Date"
                    )
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { selectedDate?.let(onAddFoodItem) }, enabled = foodName.isNotBlank() && selectedDate != null) {
                    Text("Add")
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("Quick Add (Expires in...)", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAddButton("3 days", 3, Calendar.DAY_OF_YEAR, onAddFoodItem)
                QuickAddButton("3 months", 3, Calendar.MONTH, onAddFoodItem)
                QuickAddButton("12 months", 12, Calendar.MONTH, onAddFoodItem)
                QuickAddButton("24 months", 24, Calendar.MONTH, onAddFoodItem)
                QuickAddButton("36 months", 36, Calendar.MONTH, onAddFoodItem)
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            val leadTimeText = when (leadTimeDays) {
                0 -> "on the day of expiry"
                1 -> "1 day before expiry"
                else -> "$leadTimeDays days before expiry"
            }
            Text(
                "Remind me $leadTimeText",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = leadTimeDays.toFloat(),
                onValueChange = { viewModel.setReminderLeadTimeDays(it.roundToInt()) },
                valueRange = 0f..7f,
                steps = 6,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text(
                "Tracked Food Items",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        items(foodItems) { item ->
            FoodItemCard(item = item, onDelete = { viewModel.deleteFoodItem(item) })
        }
    }
}

@Composable
fun QuickAddButton(
    text: String,
    amount: Int,
    calendarField: Int,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            val calendar = Calendar.getInstance()
            calendar.add(calendarField, amount)
            onClick(calendar.timeInMillis)
        },
        modifier = modifier
    ) {
        Text(text)
    }
}

@Composable
fun FoodItemCard(item: FoodItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text("Stored: ${item.storageMethod}", style = MaterialTheme.typography.bodySmall)
                val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(item.expiryDate))
                val daysUntil = TimeUnit.MILLISECONDS.toDays(item.expiryDate - System.currentTimeMillis())
                val expiryText = when {
                    daysUntil < 0 -> "Expired"
                    daysUntil == 0L -> "Expires today"
                    daysUntil == 1L -> "Expires tomorrow"
                    else -> "Expires in $daysUntil days ($formattedDate)"
                }
                Text(expiryText, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Item")
            }
        }
    }
}