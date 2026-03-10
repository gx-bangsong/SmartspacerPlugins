package com.kieronquinn.app.smartspacer.plugin.water.ui.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.water.data.DrinkHistory
import com.kieronquinn.app.smartspacer.plugin.water.data.DrinkHistoryDao
import com.kieronquinn.app.smartspacer.plugin.water.databinding.FragmentRecordDrinkBinding
import com.kieronquinn.app.smartspacer.plugin.water.providers.WaterProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RecordDrinkFragment : AppCompatDialogFragment() {

    private val drinkHistoryDao by inject<DrinkHistoryDao>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val amount = arguments?.getInt("amount", -1) ?: -1
        if (amount == -1) {
            dismiss()
        }

        val binding = FragmentRecordDrinkBinding.inflate(LayoutInflater.from(context))
        binding.textViewDrinkInfo.text = "Drink ${amount}ml"

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()

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
            dismiss()
        }

        return dialog
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
