package com.kieronquinn.app.smartspacer.plugin.medicationreminder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.R
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

        private val repository by inject<com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.MedicationRepository>()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            findPreference<Preference>("add_medication")?.setOnPreferenceClickListener {
                startActivity(android.content.Intent(requireContext(), AddEditMedicationActivity::class.java))
                true
            }

            lifecycleScope.launch {
                repository.allMedications.collect { medications ->
                    val category = findPreference<PreferenceCategory>("medications_category")
                    category?.removeAll()
                    // Add the "add" preference from the XML
                    findPreference<Preference>("add_medication")?.let {
                        category?.addPreference(it)
                    }

                    medications.forEach { medication ->
                        val preference = Preference(requireContext()).apply {
                            title = medication.name
                            summary = medication.dosage
                            setOnPreferenceClickListener {
                                showEditDeleteDialog(medication)
                                true
                            }
                        }
                        category?.addPreference(preference)
                    }
                }
            }
        }

        private fun showEditDeleteDialog(medication: com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.Medication) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(medication.name)
                .setItems(arrayOf("Edit", "Delete")) { dialog, which ->
                    when (which) {
                        0 -> {
                            val intent = android.content.Intent(requireContext(), AddEditMedicationActivity::class.java)
                            intent.putExtra("medication_id", medication.id)
                            startActivity(intent)
                        }
                        1 -> {
                            lifecycleScope.launch {
                                repository.deleteById(medication.id)
                            }
                        }
                    }
                }
                .show()
        }
    }
}