package com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.smartspacer.plugin.medication.R
import com.kieronquinn.app.smartspacer.plugin.medication.data.Medication
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistory
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistoryDao
import com.kieronquinn.app.smartspacer.plugin.medication.data.ScheduleType
import com.kieronquinn.app.smartspacer.plugin.medication.databinding.FragmentRecordDoseBinding
import com.kieronquinn.app.smartspacer.plugin.medication.providers.MedicationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import java.util.Calendar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RecordDoseFragment : AppCompatDialogFragment() {

    private val medicationDao by inject<MedicationDao>()
    private val doseHistoryDao by inject<DoseHistoryDao>()
    private val gson = Gson()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val medicationId = requireArguments().getInt("medicationId", -1)
        if (medicationId == -1) {
            dismiss()
        }

        val binding = FragmentRecordDoseBinding.inflate(LayoutInflater.from(context))
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()

        lifecycleScope.launch {
            val medication = medicationDao.getById(medicationId)
            if (medication == null) {
                dismiss()
                return@launch
            }
            binding.textViewDoseInfo.text = "Take ${medication.name}"

            binding.buttonTaken.setOnClickListener {
                handleDose(medication, DoseHistory.Status.TAKEN)
            }

            binding.buttonSkip.setOnClickListener {
                handleDose(medication, DoseHistory.Status.SKIPPED)
            }
        }

        return dialog
    }

    private fun handleDose(medication: Medication, status: DoseHistory.Status) {
        lifecycleScope.launch {
            val dose = DoseHistory(
                timestamp = System.currentTimeMillis(),
                medicationId = medication.id,
                status = status
            )
            doseHistoryDao.insert(dose)

            val nextDoseTs = calculateNextDose(medication)
            val updatedMedication = medication.copy(nextDoseTs = nextDoseTs)
            medicationDao.update(updatedMedication)

            SmartspacerTargetProvider.notifyChange(requireContext(), MedicationProvider::class.java)
            if (status == DoseHistory.Status.TAKEN) {
                Toast.makeText(requireContext(), R.string.medication_record_success, Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
    }

    private fun calculateNextDose(medication: Medication): Long {
        val currentDoseTime = Calendar.getInstance().apply { timeInMillis = medication.nextDoseTs }

        return when (medication.scheduleType) {
            ScheduleType.SPECIFIC_TIMES -> {
                val times = gson.fromJson(medication.timesOfDay, Array<String>::class.java).toList()
                for (time in times.sorted()) {
                    val (hour, minute) = time.split(":").map { it.toInt() }
                    val doseTime = (currentDoseTime.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (doseTime.after(currentDoseTime)) {
                        return doseTime.timeInMillis
                    }
                }
                val (hour, minute) = times.sorted().first().split(":").map { it.toInt() }
                (currentDoseTime.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            ScheduleType.EVERY_X_HOURS -> {
                medication.nextDoseTs + (medication.intervalHours ?: 1) * 60 * 60 * 1000L
            }
            ScheduleType.EVERY_X_DAYS -> {
                medication.nextDoseTs + (medication.intervalDays ?: 1) * 24 * 60 * 60 * 1000L
            }
            ScheduleType.SPECIFIC_WEEKDAYS -> {
                val (hour, minute) = medication.timesOfDay!!.split(":").map { it.toInt() }
                val doseTime = (currentDoseTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                for (i in 1..7) {
                    val checkTime = (doseTime.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) }
                    val dayOfWeek = checkTime.get(Calendar.DAY_OF_WEEK)
                    if ((medication.weekdays!! and (1 shl dayOfWeek)) != 0) {
                        return checkTime.timeInMillis
                    }
                }
                0L
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.finish()
    }
}
