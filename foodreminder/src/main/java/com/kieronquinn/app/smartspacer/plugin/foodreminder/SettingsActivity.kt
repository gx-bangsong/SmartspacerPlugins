package com.kieronquinn.app.smartspacer.plugin.foodreminder

import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.kieronquinn.app.smartspacer.plugin.foodreminder.R
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : BaseSettingsFragment() {
        override val androidXMl: Int = R.xml.preferences

        private val repository by inject<com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItemRepository>()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            findPreference<Preference>("add_food_item")?.setOnPreferenceClickListener {
                showAddEditFoodItemDialog()
                true
            }

            lifecycleScope.launch {
                repository.allFoodItems.collect { foodItems ->
                    val category = findPreference<PreferenceCategory>("food_items_category")
                    category?.removeAll()
                    // Add the "add" preference from the XML
                    findPreference<Preference>("add_food_item")?.let {
                        category?.addPreference(it)
                    }

                    foodItems.forEach { foodItem ->
                        val preference = Preference(requireContext()).apply {
                            title = foodItem.name
                            summary = "Expires on ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(foodItem.expiryDate))}"
                            setOnPreferenceClickListener {
                                showEditDeleteDialog(foodItem)
                                true
                            }
                        }
                        category?.addPreference(preference)
                    }
                }
            }
        }

        private fun showEditDeleteDialog(foodItem: com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItem) {
            AlertDialog.Builder(requireContext())
                .setTitle(foodItem.name)
                .setItems(arrayOf("Edit", "Delete")) { dialog, which ->
                    when (which) {
                        0 -> showAddEditFoodItemDialog(foodItem)
                        1 -> {
                            lifecycleScope.launch {
                                repository.deleteById(foodItem.id)
                            }
                        }
                    }
                }
                .show()
        }

        private fun showAddEditFoodItemDialog(foodItem: com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItem? = null) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_food_item, null)
            val foodName = dialogView.findViewById<EditText>(R.id.food_name)
            val storageMethod = dialogView.findViewById<EditText>(R.id.storage_method)
            val expiryDatePicker = dialogView.findViewById<DatePicker>(R.id.expiry_date_picker)

            foodItem?.let {
                foodName.setText(it.name)
                storageMethod.setText(it.storageMethod)
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = it.expiryDate
                expiryDatePicker.updateDate(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))
            }

            dialogView.findViewById<Button>(R.id.quick_add_3_days).setOnClickListener {
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 3)
                expiryDatePicker.updateDate(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))
            }

            dialogView.findViewById<Button>(R.id.quick_add_3_months).setOnClickListener {
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.MONTH, 3)
                expiryDatePicker.updateDate(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))
            }

            dialogView.findViewById<Button>(R.id.quick_add_12_months).setOnClickListener {
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.YEAR, 1)
                expiryDatePicker.updateDate(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))
            }

            AlertDialog.Builder(requireContext())
                .setTitle(if (foodItem == null) "Add Food Item" else "Edit Food Item")
                .setView(dialogView)
                .setPositiveButton(if (foodItem == null) "Add" else "Save") { _, _ ->
                    val name = foodName.text.toString()
                    val storage = storageMethod.text.toString()
                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(expiryDatePicker.year, expiryDatePicker.month, expiryDatePicker.dayOfMonth)
                    val expiryDate = calendar.timeInMillis

                    if (name.isNotBlank()) {
                        lifecycleScope.launch {
                            if (foodItem == null) {
                                repository.insert(com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItem(name = name, storageMethod = storage, expiryDate = expiryDate))
                            } else {
                                repository.update(foodItem.copy(name = name, storageMethod = storage, expiryDate = expiryDate))
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}