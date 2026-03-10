package com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.kieronquinn.app.smartspacer.plugin.medication.R
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
    private var selectedStartTime: String? = null

    fun setOnMedicationAddedListener(listener: (Medication) -> Unit) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentAddMedicationBinding.inflate(LayoutInflater.from(context))

        setupScheduleTypeToggle()
        setupTimePickers()
        setupShortcutButtons()

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

    private fun setupScheduleTypeToggle() {
        binding.toggleGroupScheduleType.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            binding.layoutConfigSpecificTimes.visibility = if (checkedId == R.id.button_schedule_specific_times) android.view.View.VISIBLE else android.view.View.GONE
            binding.layoutConfigIntervalHours.visibility = if (checkedId == R.id.button_schedule_interval_hours) android.view.View.VISIBLE else android.view.View.GONE
            binding.layoutConfigIntervalDays.visibility = if (checkedId == R.id.button_schedule_interval_days) android.view.View.VISIBLE else android.view.View.GONE
            binding.layoutConfigWeekdays.visibility = if (checkedId == R.id.button_schedule_weekdays) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun setupTimePickers() {
        binding.buttonAddTime.setOnClickListener {
            showTimePicker { time ->
                reminderTimes.add(time)
                addReminderTimeView(time)
            }
        }
        binding.buttonSelectStartTimeHours.setOnClickListener {
            showTimePicker { time ->
                selectedStartTime = time
                binding.buttonSelectStartTimeHours.text = "Start Time: $time"
            }
        }
        binding.buttonSelectStartTimeDays.setOnClickListener {
            showTimePicker { time ->
                selectedStartTime = time
                binding.buttonSelectStartTimeDays.text = "Start Time: $time"
            }
        }
        binding.buttonSelectTimeWeekdays.setOnClickListener {
            showTimePicker { time ->
                selectedStartTime = time
                binding.buttonSelectTimeWeekdays.text = "Time: $time"
            }
        }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                onTimeSelected(String.format("%02d:%02d", hourOfDay, minute))
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

    private fun setupShortcutButtons() {
        binding.buttonShortcut1Time.setOnClickListener {
            clearTimes()
            addTime("08:00")
        }
        binding.buttonShortcut2Times.setOnClickListener {
            clearTimes()
            addTime("08:00")
            addTime("20:00")
        }
        binding.buttonShortcut3Times.setOnClickListener {
            clearTimes()
            addTime("08:00")
            addTime("14:00")
            addTime("20:00")
        }
        binding.buttonShortcut4Times.setOnClickListener {
            clearTimes()
            addTime("08:00")
            addTime("12:00")
            addTime("16:00")
            addTime("20:00")
        }
    }

    private fun clearTimes() {
        reminderTimes.clear()
        binding.containerReminderTimes.removeAllViews()
    }

    private fun addTime(time: String) {
        reminderTimes.add(time)
        addReminderTimeView(time)
    }

    private fun createMedicationFromInput(): Medication? {
        val name = binding.editTextMedicationName.text.toString()
        val dosage = binding.editTextDosage.text.toString()
        val startDateStr = binding.editTextStartDate.text.toString()
        val endDateStr = binding.editTextEndDate.text.toString()

        if (name.isBlank() || startDateStr.isBlank()) {
            return null
        }

        val scheduleType = when (binding.toggleGroupScheduleType.checkedButtonId) {
            R.id.button_schedule_interval_hours -> ScheduleType.EVERY_X_HOURS
            R.id.button_schedule_interval_days -> ScheduleType.EVERY_X_DAYS
            R.id.button_schedule_weekdays -> ScheduleType.SPECIFIC_WEEKDAYS
            else -> ScheduleType.SPECIFIC_TIMES
        }

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val startDate = dateFormat.parse(startDateStr)?.time ?: return null
        val endDate = if (endDateStr.isNotBlank()) dateFormat.parse(endDateStr)?.time else null

        val intervalHours = binding.editTextIntervalHours.text.toString().toIntOrNull()
        val intervalDays = binding.editTextIntervalDays.text.toString().toIntOrNull()

        val weekdays = if (scheduleType == ScheduleType.SPECIFIC_WEEKDAYS) {
            var mask = 0
            if (binding.chipMonday.isChecked) mask = mask or (1 shl Calendar.MONDAY)
            if (binding.chipTuesday.isChecked) mask = mask or (1 shl Calendar.TUESDAY)
            if (binding.chipWednesday.isChecked) mask = mask or (1 shl Calendar.WEDNESDAY)
            if (binding.chipThursday.isChecked) mask = mask or (1 shl Calendar.THURSDAY)
            if (binding.chipFriday.isChecked) mask = mask or (1 shl Calendar.FRIDAY)
            if (binding.chipSaturday.isChecked) mask = mask or (1 shl Calendar.SATURDAY)
            if (binding.chipSunday.isChecked) mask = mask or (1 shl Calendar.SUNDAY)
            mask
        } else null

        // Validation
        when (scheduleType) {
            ScheduleType.SPECIFIC_TIMES -> if (reminderTimes.isEmpty()) return null
            ScheduleType.EVERY_X_HOURS -> if (intervalHours == null || selectedStartTime == null) return null
            ScheduleType.EVERY_X_DAYS -> if (intervalDays == null || selectedStartTime == null) return null
            ScheduleType.SPECIFIC_WEEKDAYS -> if (weekdays == 0 || selectedStartTime == null) return null
        }

        val medication = Medication(
            name = name,
            dosage = dosage,
            startDate = startDate,
            endDate = endDate,
            isUnlimited = endDate == null,
            scheduleType = scheduleType,
            intervalHours = intervalHours,
            intervalDays = intervalDays,
            timesOfDay = if (scheduleType == ScheduleType.SPECIFIC_TIMES) gson.toJson(reminderTimes) else selectedStartTime,
            weekdays = weekdays,
            nextDoseTs = 0L // Will be calculated below
        )

        return medication.copy(nextDoseTs = calculateNextDose(medication))
    }

    private fun calculateNextDose(medication: Medication): Long {
        val now = Calendar.getInstance()
        val startCal = Calendar.getInstance().apply { timeInMillis = medication.startDate }

        return when (medication.scheduleType) {
            ScheduleType.SPECIFIC_TIMES -> {
                val times = gson.fromJson(medication.timesOfDay, Array<String>::class.java).toList()
                for (time in times.sorted()) {
                    val (hour, minute) = time.split(":").map { it.toInt() }
                    val doseTime = (now.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (doseTime.after(now) && !doseTime.before(startCal)) {
                        return doseTime.timeInMillis
                    }
                }
                val (hour, minute) = times.sorted().first().split(":").map { it.toInt() }
                (now.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            ScheduleType.EVERY_X_HOURS -> {
                val (hour, minute) = medication.timesOfDay!!.split(":").map { it.toInt() }
                val firstDose = (startCal.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (firstDose.after(now)) return firstDose.timeInMillis

                val diffMillis = now.timeInMillis - firstDose.timeInMillis
                val intervalMillis = (medication.intervalHours ?: 1) * 60 * 60 * 1000L
                val intervalsPassed = (diffMillis / intervalMillis) + 1
                firstDose.timeInMillis + (intervalsPassed * intervalMillis)
            }
            ScheduleType.EVERY_X_DAYS -> {
                val (hour, minute) = medication.timesOfDay!!.split(":").map { it.toInt() }
                val firstDose = (startCal.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (firstDose.after(now)) return firstDose.timeInMillis

                val diffMillis = now.timeInMillis - firstDose.timeInMillis
                val intervalMillis = (medication.intervalDays ?: 1) * 24 * 60 * 60 * 1000L
                val intervalsPassed = (diffMillis / intervalMillis) + 1
                firstDose.timeInMillis + (intervalsPassed * intervalMillis)
            }
            ScheduleType.SPECIFIC_WEEKDAYS -> {
                val (hour, minute) = medication.timesOfDay!!.split(":").map { it.toInt() }
                val doseTime = (now.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                for (i in 0..7) {
                    val checkTime = (doseTime.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) }
                    val dayOfWeek = checkTime.get(Calendar.DAY_OF_WEEK)
                    if ((medication.weekdays!! and (1 shl dayOfWeek)) != 0) {
                        if (checkTime.after(now) && !checkTime.before(startCal)) {
                            return checkTime.timeInMillis
                        }
                    }
                }
                0L
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
