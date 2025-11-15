package com.kieronquinn.app.smartspacer.plugin.medication.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.databinding.ActivityRecordDoseBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Calendar

class RecordDoseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordDoseBinding
    private val medicationDao by inject<MedicationDao>()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDoseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val medicationId = intent.getIntExtra("medication_id", -1)
        if (medicationId == -1) {
            finish()
            return
        }

        lifecycleScope.launch {
            val medication = medicationDao.getById(medicationId) ?: return@launch
            binding.textViewMedicationName.text = medication.name

            binding.buttonTaken.setOnClickListener {
                lifecycleScope.launch {
                    val nextDoseTs = calculateNextDose(medication.startDate, gson.fromJson(medication.timesOfDay, Array<String>::class.java).toList())
                    medicationDao.update(medication.copy(nextDoseTs = nextDoseTs))
                    finish()
                }
            }

            binding.buttonSkip.setOnClickListener {
                lifecycleScope.launch {
                    val nextDoseTs = calculateNextDose(medication.startDate, gson.fromJson(medication.timesOfDay, Array<String>::class.java).toList())
                    medicationDao.update(medication.copy(nextDoseTs = nextDoseTs))
                    finish()
                }
            }
        }
    }

    private fun calculateNextDose(startDate: Long, reminderTimes: List<String>): Long {
        val now = Calendar.getInstance()
        val startCal = Calendar.getInstance().apply { timeInMillis = startDate }

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

        val (hour, minute) = reminderTimes.sorted().first().split(":").map { it.toInt() }
        return (startCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }.timeInMillis
    }
}
