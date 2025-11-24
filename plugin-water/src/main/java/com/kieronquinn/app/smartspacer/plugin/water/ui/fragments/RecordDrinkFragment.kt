package com.kieronquinn.app.smartspacer.plugin.water.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.water.data.DrinkHistory
import com.kieronquinn.app.smartspacer.plugin.water.data.DrinkHistoryDao
import com.kieronquinn.app.smartspacer.plugin.water.databinding.FragmentRecordDrinkBinding
import com.kieronquinn.app.smartspacer.plugin.water.providers.WaterProvider
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RecordDrinkFragment : DialogFragment() {

    private lateinit var binding: FragmentRecordDrinkBinding
    private val drinkHistoryDao by inject<DrinkHistoryDao>()
    private val waterDataRepository by inject<WaterDataRepository>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordDrinkBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val amount = requireArguments().getInt("amount", -1)
        if (amount == -1) {
            dismiss()
            return
        }

        binding.textViewDrinkInfo.text = "Drink ${amount}ml"

        binding.buttonTaken.setOnClickListener {
            lifecycleScope.launch {
                val drinkHistory = DrinkHistory(
                    timestamp = System.currentTimeMillis(),
                    amount = amount
                )
                drinkHistoryDao.insert(drinkHistory)
                SmartspacerTargetProvider.notifyChange(requireContext(), WaterProvider::class.java)
                Toast.makeText(requireContext(), R.string.water_record_success, Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }

        binding.buttonSkip.setOnClickListener {
            lifecycleScope.launch {
                // We don't need to do anything here, just close the dialog
                dismiss()
            }
        }
    }
}
