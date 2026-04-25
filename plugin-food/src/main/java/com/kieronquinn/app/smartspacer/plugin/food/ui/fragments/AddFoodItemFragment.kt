package com.kieronquinn.app.smartspacer.plugin.food.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItem
import com.kieronquinn.app.smartspacer.plugin.food.databinding.FragmentAddFoodItemBinding
import java.util.concurrent.TimeUnit

class AddFoodItemFragment : DialogFragment() {

    private var _binding: FragmentAddFoodItemBinding? = null
    private val binding get() = _binding!!

    private var listener: ((FoodItem) -> Unit)? = null

    fun setOnFoodItemAddedListener(listener: (FoodItem) -> Unit) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentAddFoodItemBinding.inflate(LayoutInflater.from(context))

        setupQuickFillButtons()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Food Item")
            .setView(binding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val foodItem = createFoodItemFromInput()
                if (foodItem != null) {
                    listener?.invoke(foodItem)
                    dialog.dismiss()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Please fill in all required fields", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        return dialog
    }

    private fun setupQuickFillButtons() {
        binding.buttonShelfLife3Days.setOnClickListener {
            binding.editTextShelfLife.setText("3")
        }
        binding.buttonShelfLife3Months.setOnClickListener {
            binding.editTextShelfLife.setText("90")
        }
        binding.buttonShelfLife12Months.setOnClickListener {
            binding.editTextShelfLife.setText("365")
        }
        binding.buttonShelfLife24Months.setOnClickListener {
            binding.editTextShelfLife.setText("730")
        }
        binding.buttonShelfLife36Months.setOnClickListener {
            binding.editTextShelfLife.setText("1095")
        }

        binding.buttonShortcutFrozenFood.setOnClickListener {
            binding.editTextStorageMethod.setText("Frozen")
            binding.editTextShelfLife.setText("365")
        }
        binding.buttonShortcutMeat.setOnClickListener {
            binding.editTextStorageMethod.setText("Frozen")
            binding.editTextShelfLife.setText("90")
        }
        binding.buttonShortcutSeafood.setOnClickListener {
            binding.editTextStorageMethod.setText("Frozen")
            binding.editTextShelfLife.setText("30")
        }
        binding.buttonShortcutProcessed.setOnClickListener {
            binding.editTextStorageMethod.setText("Frozen")
            binding.editTextShelfLife.setText("90")
        }
    }

    private fun createFoodItemFromInput(): FoodItem? {
        val name = binding.editTextFoodName.text.toString()
        val storageMethod = binding.editTextStorageMethod.text.toString()
        val shelfLifeDays = binding.editTextShelfLife.text.toString().toLongOrNull()

        if (name.isBlank() || storageMethod.isBlank() || shelfLifeDays == null) {
            return null
        }

        val expiryDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(shelfLifeDays)

        return FoodItem(
            name = name,
            storageMethod = storageMethod,
            expiryDate = expiryDate,
            reminderOffsetDays = 1, // Default to 1 day reminder
            notes = null
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
