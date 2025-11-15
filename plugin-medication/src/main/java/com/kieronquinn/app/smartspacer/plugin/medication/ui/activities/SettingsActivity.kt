package com.kieronquinn.app.smartspacer.plugin.medication.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.databinding.ActivitySettingsBinding
import com.kieronquinn.app.smartspacer.plugin.medication.ui.adapters.MedicationAdapter
import com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments.AddMedicationFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val medicationDao by inject<MedicationDao>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        lifecycleScope.launch {
            medicationDao.getAll().collect { medications ->
                binding.recyclerView.adapter = MedicationAdapter(medications)
            }
        }

        binding.fabAdd.setOnClickListener {
            val addMedicationFragment = AddMedicationFragment()
            addMedicationFragment.setOnMedicationAddedListener { medication ->
                lifecycleScope.launch {
                    medicationDao.insert(medication)
                }
            }
            addMedicationFragment.show(supportFragmentManager, "AddMedicationFragment")
        }
    }

}
