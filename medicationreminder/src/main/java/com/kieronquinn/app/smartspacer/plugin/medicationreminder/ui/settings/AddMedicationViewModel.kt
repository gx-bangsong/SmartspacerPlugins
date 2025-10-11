package com.kieronquinn.app.smartspacer.plugin.medicationreminder.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationRepository
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.Medication
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.Schedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

enum class ScheduleType {
    EVERY_X_HOURS,
    SPECIFIC_TIMES,
    EVERY_X_DAYS,
    SPECIFIC_DAYS_OF_WEEK
}

data class AddMedicationFormState(
    val name: String = "",
    val dosage: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val isEndDateUnlimited: Boolean = true,
    val scheduleType: ScheduleType = ScheduleType.SPECIFIC_TIMES,
    // State for each schedule type
    val everyXHours: Int = 6,
    val specificTimes: List<LocalTime> = listOf(LocalTime.of(9, 0)),
    val everyXDays: Int = 1,
    val specificDaysTime: LocalTime = LocalTime.of(9, 0),
    val specificDaysOfWeek: Set<DayOfWeek> = setOf(DayOfWeek.MONDAY),
    // UI state
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val showTimePickerForSpecificDays: Boolean = false
)

class AddMedicationViewModel(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationFormState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: AddMedicationFormEvent) {
        when (event) {
            is AddMedicationFormEvent.NameChanged -> _uiState.update { it.copy(name = event.name) }
            is AddMedicationFormEvent.DosageChanged -> _uiState.update { it.copy(dosage = event.dosage) }
            is AddMedicationFormEvent.ScheduleTypeChanged -> _uiState.update { it.copy(scheduleType = event.type) }
            is AddMedicationFormEvent.EveryXHoursChanged -> _uiState.update { it.copy(everyXHours = event.hours) }
            is AddMedicationFormEvent.EveryXDaysChanged -> _uiState.update { it.copy(everyXDays = event.days) }
            is AddMedicationFormEvent.TimeForSpecificDaysChanged -> _uiState.update { it.copy(specificDaysTime = event.time, showTimePickerForSpecificDays = false) }
            is AddMedicationFormEvent.DayOfWeekToggled -> {
                _uiState.update { currentState ->
                    val newDays = currentState.specificDaysOfWeek.toMutableSet()
                    if (newDays.contains(event.day)) {
                        newDays.remove(event.day)
                    } else {
                        newDays.add(event.day)
                    }
                    if (newDays.isEmpty()) newDays.add(event.day)
                    currentState.copy(specificDaysOfWeek = newDays)
                }
            }
            is AddMedicationFormEvent.SpecificTimeAdded -> _uiState.update { it.copy(specificTimes = (it.specificTimes + event.time).sorted()) }
            is AddMedicationFormEvent.SpecificTimeRemoved -> _uiState.update { it.copy(specificTimes = it.specificTimes.filter { time -> time != event.time }) }
            is AddMedicationFormEvent.StartDateChanged -> _uiState.update { it.copy(startDate = event.date, showStartDatePicker = false) }
            is AddMedicationFormEvent.EndDateChanged -> _uiState.update { it.copy(endDate = event.date, showEndDatePicker = false) }
            is AddMedicationFormEvent.EndDateUnlimitedToggled -> _uiState.update { it.copy(isEndDateUnlimited = event.isUnlimited) }
            is AddMedicationFormEvent.ShowStartDatePicker -> _uiState.update { it.copy(showStartDatePicker = event.show) }
            is AddMedicationFormEvent.ShowEndDatePicker -> _uiState.update { it.copy(showEndDatePicker = event.show) }
            is AddMedicationFormEvent.ShowTimePickerForSpecificDays -> _uiState.update { it.copy(showTimePickerForSpecificDays = event.show) }
        }
    }

    fun saveMedication() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val schedule = when (currentState.scheduleType) {
                ScheduleType.EVERY_X_HOURS -> Schedule.EveryXHours(currentState.everyXHours)
                ScheduleType.SPECIFIC_TIMES -> Schedule.SpecificTimes(currentState.specificTimes)
                ScheduleType.EVERY_X_DAYS -> Schedule.EveryXDays(currentState.everyXDays, currentState.specificDaysTime)
                ScheduleType.SPECIFIC_DAYS_OF_WEEK -> Schedule.SpecificDaysOfWeek(currentState.specificDaysOfWeek, currentState.specificDaysTime)
            }
            val newMedication = Medication(
                name = currentState.name,
                dosage = currentState.dosage.ifBlank { null },
                startDate = currentState.startDate,
                endDate = if (currentState.isEndDateUnlimited) null else currentState.endDate,
                schedule = schedule
            )
            if(newMedication.name.isNotBlank()) {
                medicationRepository.addMedication(newMedication)
            }
        }
    }
}

sealed interface AddMedicationFormEvent {
    data class NameChanged(val name: String) : AddMedicationFormEvent
    data class DosageChanged(val dosage: String) : AddMedicationFormEvent
    data class ScheduleTypeChanged(val type: ScheduleType) : AddMedicationFormEvent
    data class EveryXHoursChanged(val hours: Int) : AddMedicationFormEvent
    data class EveryXDaysChanged(val days: Int) : AddMedicationFormEvent
    data class TimeForSpecificDaysChanged(val time: LocalTime) : AddMedicationFormEvent
    data class DayOfWeekToggled(val day: DayOfWeek) : AddMedicationFormEvent
    data class SpecificTimeAdded(val time: LocalTime) : AddMedicationFormEvent
    data class SpecificTimeRemoved(val time: LocalTime) : AddMedicationFormEvent
    data class StartDateChanged(val date: Long) : AddMedicationFormEvent
    data class EndDateChanged(val date: Long) : AddMedicationFormEvent
    data class EndDateUnlimitedToggled(val isUnlimited: Boolean) : AddMedicationFormEvent
    data class ShowStartDatePicker(val show: Boolean) : AddMedicationFormEvent
    data class ShowEndDatePicker(val show: Boolean) : AddMedicationFormEvent
    data class ShowTimePickerForSpecificDays(val show: Boolean) : AddMedicationFormEvent
}