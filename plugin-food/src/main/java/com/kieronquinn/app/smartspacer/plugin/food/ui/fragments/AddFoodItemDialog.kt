package com.kieronquinn.app.smartspacer.plugin.food.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItem
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddFoodItemDialog(
    onDismiss: () -> Unit,
    onSave: (FoodItem) -> Unit
) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var storageMethod by rememberSaveable { mutableStateOf("") }
    var shelfLifeDays by rememberSaveable { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Add Food Item") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            val shelfLife = shelfLifeDays.toLongOrNull()
                            if (name.isNotBlank() && storageMethod.isNotBlank() && shelfLife != null) {
                                val expiryDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(shelfLife)
                                val foodItem = FoodItem(
                                    name = name,
                                    storageMethod = storageMethod,
                                    expiryDate = expiryDate,
                                    reminderOffsetDays = 1,
                                    notes = null
                                )
                                onSave(foodItem)
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
                    label = { Text("Food Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = storageMethod,
                    onValueChange = { storageMethod = it },
                    label = { Text("Storage Method (e.g., refrigerated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = shelfLifeDays,
                    onValueChange = { shelfLifeDays = it },
                    label = { Text("Shelf Life (in days)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Text("Shelf Life Shortcuts", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "3 Days" to "3",
                        "3 Months" to "90",
                        "1 Year" to "365",
                        "2 Years" to "730",
                        "3 Years" to "1095"
                    ).forEach { (label, days) ->
                        SuggestionChip(onClick = { shelfLifeDays = days }, label = { Text(label) })
                    }
                }

                Text("Quick Shortcuts (Storage: Frozen)", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Frozen Food (12m)" to "365",
                        "Meat (3m)" to "90",
                        "Seafood (1m)" to "30",
                        "Processed (3m)" to "90"
                    ).forEach { (label, days) ->
                        SuggestionChip(
                            onClick = {
                                storageMethod = "Frozen"
                                shelfLifeDays = days
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    }
}
