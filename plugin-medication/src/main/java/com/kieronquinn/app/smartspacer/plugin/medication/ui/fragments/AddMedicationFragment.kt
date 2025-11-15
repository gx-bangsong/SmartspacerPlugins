package com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.medication.data.Medication
import com.kieronquinn.app.smartspacer.plugin.medication.data.ScheduleType
import com.kieronquinn.app.smartspacer.plugin.medication.databinding.FragmentAddMedicationBinding
import java.util.Calendar

class AddMedicationFragment : DialogFragment() {

    private var _binding: FragmentAddMedicationBinding? = null
    private val binding get() = _binding!!

    private var listener: ((Medication) -> Unit)? = null
    private val reminderTimes = mutableListOf<String>()
    private val gson = Gson()

    fun setOnMedicationAddedListener(listener: (Medication) -> Unit) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentAddMedicationBinding.inflate(LayoutInflater.from(context))

        binding.buttonAddTime.setOnClickListener {
            showTimePicker()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Medication")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val medication = createMedicationFromInput()
                medication?.let {
                    listener?.invoke(it)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                reminderTimes.add(time)
                addReminderTimeView(time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun addReminderTimeView(time: String) {
        val textView = TextView(requireContext()).apply {
            text = time
            textSize = 16f
        }
        binding.containerReminderTimes.addView(textView)
    }

    private fun createMedicationFromInput(): Medication? {
        val name = binding.editTextMedicationName.text.toString()
        val dosage = binding.editTextDosage.text.toString()
        val startDateStr = binding.editTextStartDate.text.toString()
        val endDateStr = binding.editTextEndDate.text.toString()

        if (name.isBlank() || reminderTimes.isEmpty() || startDateStr.isBlank()) {
            return null
        }

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val startDate = dateFormat.parse(startDateStr)?.time ?: return null
        val endDate = if (endDateStr.isNotBlank()) dateFormat.parse(endDateStr)?.time else null

        val nextDoseTs = calculateNextDose(startDate)

        return Medication(
            name = name,
            dosage = dosage,
            startDate = startDate,
            endDate = endDate,
            isUnlimited = endDate == null,
            scheduleType = ScheduleType.SPECIFIC_TIMES.name,
            intervalHours = null,
            intervalDays = null,
            timesOfDay = gson.toJson(reminderTimes),
            weekdays = null,
            nextDoseTs = nextDoseTs
        )
    }

    private fun calculateNextDose(startDate: Long): Long {
        val now = Calendar.getInstance()
        val startCal = Calendar.getInstance().apply { timeInMillis = startDate }

        // Find the next upcoming dose time today
        for (time in reminderTimes.sorted()) {
            val (hour, minute) = time.split(":").map { it.toInt() }
            val doseTime = (startCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            if (doseTime.after(now)) {
                return doseTime.timeInMillis
            }
        }

        // If all doses for today have passed, schedule for the first dose tomorrow
        val (hour, minute) = reminderTimes.sorted().first().split(":").map { it.toInt() }
        return (startCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }.timeInMillis
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
