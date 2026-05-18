package com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistory
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistoryDao
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationUtils
import com.kieronquinn.app.smartspacer.plugin.medication.databinding.FragmentRecordDoseBinding
import com.kieronquinn.app.smartspacer.plugin.medication.work.MedicationWorker
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RecordDoseFragment : BottomSheetDialogFragment() {

    private val medicationDao by inject<MedicationDao>()
    private val doseHistoryDao by inject<DoseHistoryDao>()

    private var _binding: FragmentRecordDoseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecordDoseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val medicationId = arguments?.getInt("medicationId", -1) ?: -1
        if(medicationId == -1) return

        lifecycleScope.launch {
            val medication = medicationDao.getById(medicationId) ?: return@launch
            binding.textViewDoseInfo.text = "${medication.name} ${medication.dosage ?: ""}"

            binding.buttonTaken.setOnClickListener {
                lifecycleScope.launch {
                    val history = DoseHistory(
                        medicationId = medication.id,
                        timestamp = System.currentTimeMillis(),
                        status = DoseHistory.Status.TAKEN
                    )
                    doseHistoryDao.insert(history)

                    // 计算并更新下一次服药时间
                    val updatedMedication = medication.copy(
                        nextDoseTs = MedicationUtils.calculateNextDose(medication)
                    )
                    medicationDao.update(updatedMedication)

                    // 记录服药后立即刷新 Smartspace
                    MedicationWorker.enqueueImmediate(requireContext())
                    dismiss()
                    activity?.finish()
                }
            }

            binding.buttonSkip.setOnClickListener {
                lifecycleScope.launch {
                    val history = DoseHistory(
                        medicationId = medication.id,
                        timestamp = System.currentTimeMillis(),
                        status = DoseHistory.Status.SKIPPED
                    )
                    doseHistoryDao.insert(history)

                    // 跳过时也更新下一次服药时间
                    val updatedMedication = medication.copy(
                        nextDoseTs = MedicationUtils.calculateNextDose(medication)
                    )
                    medicationDao.update(updatedMedication)

                    // 跳过服药后也立即刷新
                    MedicationWorker.enqueueImmediate(requireContext())
                    dismiss()
                    activity?.finish()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
