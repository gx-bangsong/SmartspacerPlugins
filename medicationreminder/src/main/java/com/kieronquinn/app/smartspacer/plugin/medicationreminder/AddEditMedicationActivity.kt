package com.kieronquinn.app.smartspacer.plugin.medicationreminder

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.R
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.Medication
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationRepository
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.Schedule
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.ScheduleType
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AddEditMedicationActivity : AppCompatActivity() {

    private val repository: MedicationRepository by inject()

    private lateinit var intervalDetailsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_medication)

        val medicationId = intent.getIntExtra("medication_id", -1)
        if (medicationId != -1) {
            lifecycleScope.launch {
                val medication = repository.allMedications.first().find { it.id == medicationId }
                medication?.let { populateUi(it) }
            }
        }

        val medicationName = findViewById<EditText>(R.id.medication_name)
        val medicationDosage = findViewById<EditText>(R.id.medication_dosage)
        val startDatePicker = findViewById<DatePicker>(R.id.start_date_picker)
        val endDateUnlimited = findViewById<CheckBox>(R.id.end_date_unlimited)
        val endDatePicker = findViewById<DatePicker>(R.id.end_date_picker)
        val intervalTypeSpinner = findViewById<Spinner>(R.id.interval_type_spinner)
        intervalDetailsContainer = findViewById(R.id.interval_details_container)
        val saveButton = findViewById<Button>(R.id.save_button)

        endDateUnlimited.setOnCheckedChangeListener { _, isChecked ->
            endDatePicker.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        val intervalTypes = ScheduleType.values().map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, intervalTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        intervalTypeSpinner.adapter = adapter

        intervalTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateIntervalDetails(ScheduleType.values()[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        saveButton.setOnClickListener {
            val name = medicationName.text.toString()
            val dosage = medicationDosage.text.toString()
            val startDate = getTimestampFromDatePicker(startDatePicker)
            val endDate = if (endDateUnlimited.isChecked) null else getTimestampFromDatePicker(endDatePicker)
            val schedule = createScheduleFromUi()

            if (name.isNotBlank() && schedule != null) {
                lifecycleScope.launch {
                    val medicationId = intent.getIntExtra("medication_id", -1)
                    if (medicationId == -1) {
                        repository.insert(Medication(name = name, dosage = dosage, startDate = startDate, endDate = endDate, schedule = schedule))
                    } else {
                        repository.update(Medication(id = medicationId, name = name, dosage = dosage, startDate = startDate, endDate = endDate, schedule = schedule))
                    }
                    finish()
                }
            }
        }
    }

    private fun populateUi(medication: Medication) {
        findViewById<EditText>(R.id.medication_name).setText(medication.name)
        findViewById<EditText>(R.id.medication_dosage).setText(medication.dosage)

        val startDatePicker = findViewById<DatePicker>(R.id.start_date_picker)
        val startCalendar = java.util.Calendar.getInstance()
        startCalendar.timeInMillis = medication.startDate
        startDatePicker.updateDate(startCalendar.get(java.util.Calendar.YEAR), startCalendar.get(java.util.Calendar.MONTH), startCalendar.get(java.util.Calendar.DAY_OF_MONTH))

        val endDateUnlimited = findViewById<CheckBox>(R.id.end_date_unlimited)
        val endDatePicker = findViewById<DatePicker>(R.id.end_date_picker)
        if (medication.endDate == null) {
            endDateUnlimited.isChecked = true
        } else {
            endDateUnlimited.isChecked = false
            val endCalendar = java.util.Calendar.getInstance()
            endCalendar.timeInMillis = medication.endDate
            endDatePicker.updateDate(endCalendar.get(java.util.Calendar.YEAR), endCalendar.get(java.util.Calendar.MONTH), endCalendar.get(java.util.Calendar.DAY_OF_MONTH))
        }

        val intervalTypeSpinner = findViewById<Spinner>(R.id.interval_type_spinner)
        intervalTypeSpinner.setSelection(medication.schedule.type.ordinal)
        updateIntervalDetails(medication.schedule.type, medication.schedule)
    }

    private fun updateIntervalDetails(scheduleType: ScheduleType, schedule: Schedule? = null) {
        intervalDetailsContainer.removeAllViews()
        when (scheduleType) {
            ScheduleType.EVERY_X_HOURS -> {
                val editText = EditText(this)
                editText.hint = "Hours"
                editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                schedule?.interval?.let { editText.setText(it.toString()) }
                intervalDetailsContainer.addView(editText)
            }
            ScheduleType.SPECIFIC_TIMES -> {
                val addTimeButton = Button(this)
                addTimeButton.text = "Add Time"
                addTimeButton.setOnClickListener {
                    val timePicker = TimePicker(this)
                    intervalDetailsContainer.addView(timePicker)
                }
                intervalDetailsContainer.addView(addTimeButton)
                schedule?.times?.forEach { time ->
                    val (hour, minute) = time.split(":").map { it.toInt() }
                    val timePicker = TimePicker(this)
                    timePicker.hour = hour
                    timePicker.minute = minute
                    intervalDetailsContainer.addView(timePicker)
                }
            }
            ScheduleType.EVERY_X_DAYS -> {
                val editText = EditText(this)
                editText.hint = "Days"
                editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                schedule?.interval?.let { editText.setText(it.toString()) }
                intervalDetailsContainer.addView(editText)
            }
            ScheduleType.SPECIFIC_DAYS_OF_WEEK -> {
                val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                days.forEachIndexed { index, day ->
                    val checkBox = CheckBox(this)
                    checkBox.text = day
                    checkBox.tag = index + 1
                    schedule?.daysOfWeek?.let {
                        if (it.contains(index + 1)) {
                            checkBox.isChecked = true
                        }
                    }
                    intervalDetailsContainer.addView(checkBox)
                }
            }
        }
    }

    private fun createScheduleFromUi(): Schedule? {
        val intervalType = ScheduleType.values()[findViewById<Spinner>(R.id.interval_type_spinner).selectedItemPosition]
        return when (intervalType) {
            ScheduleType.EVERY_X_HOURS -> {
                val hours = (intervalDetailsContainer.getChildAt(0) as EditText).text.toString().toIntOrNull()
                if (hours != null) Schedule(type = intervalType, interval = hours, times = null, daysOfWeek = null) else null
            }
            ScheduleType.SPECIFIC_TIMES -> {
                val times = mutableListOf<String>()
                for (i in 0 until intervalDetailsContainer.childCount) {
                    val child = intervalDetailsContainer.getChildAt(i)
                    if (child is TimePicker) {
                        times.add("${child.hour}:${child.minute}")
                    }
                }
                Schedule(type = intervalType, times = times, interval = null, daysOfWeek = null)
            }
            ScheduleType.EVERY_X_DAYS -> {
                val days = (intervalDetailsContainer.getChildAt(0) as EditText).text.toString().toIntOrNull()
                if (days != null) Schedule(type = intervalType, interval = days, times = null, daysOfWeek = null) else null
            }
            ScheduleType.SPECIFIC_DAYS_OF_WEEK -> {
                val daysOfWeek = mutableListOf<Int>()
                for (i in 0 until intervalDetailsContainer.childCount) {
                    val checkBox = intervalDetailsContainer.getChildAt(i) as CheckBox
                    if (checkBox.isChecked) {
                        daysOfWeek.add(checkBox.tag as Int)
                    }
                }
                Schedule(type = intervalType, daysOfWeek = daysOfWeek, interval = null, times = null)
            }
        }
    }

    private fun getTimestampFromDatePicker(datePicker: DatePicker): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
        return calendar.timeInMillis
    }
}