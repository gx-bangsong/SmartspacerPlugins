package com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.databinding.FragmentMedicationSettingsBinding
import com.kieronquinn.app.smartspacer.plugin.medication.ui.adapters.MedicationAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MedicationSettingsFragment : BaseFragment<FragmentMedicationSettingsBinding>(FragmentMedicationSettingsBinding::inflate) {

    private val medicationDao by inject<MedicationDao>()

    override val adapter by lazy {
        object : BaseSettingsAdapter(recyclerView, emptyList()) {}
    }

    override val recyclerView: LifecycleAwareRecyclerView
        get() = binding.settingsBaseRecyclerView

    override val loadingView: LinearProgressIndicator
        get() = binding.settingsBaseLoadingProgress

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMedicationList()
        setupFab()
    }

    private fun setupMedicationList() {
        lifecycleScope.launch {
            medicationDao.getAll().collect { medications ->
                binding.settingsBaseLoading.isVisible = false
                recyclerView.adapter = object : com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView.Adapter<MedicationAdapter.ViewHolder>(recyclerView) {
                    private val innerAdapter = MedicationAdapter(medications) { medication ->
                        lifecycleScope.launch {
                            medicationDao.delete(medication)
                            com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider.notifyChange(requireContext(), com.kieronquinn.app.smartspacer.plugin.medication.providers.MedicationProvider::class.java)
                        }
                    }

                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationAdapter.ViewHolder {
                        return innerAdapter.onCreateViewHolder(parent, viewType)
                    }

                    override fun onBindViewHolder(holder: MedicationAdapter.ViewHolder, position: Int) {
                        innerAdapter.onBindViewHolder(holder, position)
                    }

                    override fun getItemCount(): Int = innerAdapter.itemCount
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val addMedicationFragment = AddMedicationFragment()
            addMedicationFragment.setOnMedicationAddedListener { medication ->
                lifecycleScope.launch {
                    medicationDao.insert(medication)
                    com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider.notifyChange(requireContext(), com.kieronquinn.app.smartspacer.plugin.medication.providers.MedicationProvider::class.java)
                }
            }
            addMedicationFragment.show(childFragmentManager, "AddMedicationFragment")
        }
    }
}
