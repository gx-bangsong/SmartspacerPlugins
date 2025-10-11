package com.kieronquinn.app.smartspacer.plugin.medicationreminder.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.Medication
import org.koin.androidx.compose.getViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MedicationReminderSettingsScreen(
    viewModel: MedicationReminderSettingsViewModel = getViewModel(),
    onAddMedicationClicked: () -> Unit
) {
    val medications by viewModel.medications.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = { onAddMedicationClicked() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Medication")
        }

        Text(
            "Tracked Medications",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )

        LazyColumn {
            items(medications) { medication ->
                MedicationItemCard(
                    medication = medication,
                    onDelete = { viewModel.deleteMedication(medication.id) }
                )
            }
        }
    }
}

@Composable
fun MedicationItemCard(medication: Medication, onDelete: () -> Unit) {
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
                Text(medication.name, style = MaterialTheme.typography.titleMedium)
                medication.dosage?.let {
                    Text("Dosage: $it", style = MaterialTheme.typography.bodySmall)
                }
                val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(medication.startDate))
                Text("Started: $formattedDate", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Medication")
            }
        }
    }
}