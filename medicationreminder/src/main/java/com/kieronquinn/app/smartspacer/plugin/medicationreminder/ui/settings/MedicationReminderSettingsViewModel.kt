package com.kieronquinn.app.smartspacer.plugin.medicationreminder.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationRepository
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.Medication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicationReminderSettingsViewModel(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    val medications = medicationRepository.getMedications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun addMedication(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.addMedication(medication)
        }
    }

    fun deleteMedication(medicationId: Int) {
        viewModelScope.launch {
            medicationRepository.deleteMedication(medicationId)
        }
    }
}