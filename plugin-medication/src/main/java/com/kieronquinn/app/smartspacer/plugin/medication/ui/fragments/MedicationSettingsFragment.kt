package com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.databinding.FragmentMedicationSettingsBinding
import com.kieronquinn.app.smartspacer.plugin.medication.ui.adapters.MedicationAdapter
import com.kieronquinn.app.smartspacer.plugin.medication.work.MedicationWorker
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
        // 显式隐藏加载 UI
        binding.settingsBaseLoading.visibility = View.GONE
        setupMedicationList()
        setupFab()
        // 确保定期任务已启动
        MedicationWorker.enqueuePeriodic(requireContext())
    }

    private fun setupMedicationList() {
        lifecycleScope.launch {
            medicationDao.getAll().collect { medications ->
                binding.settingsBaseLoading.visibility = View.GONE
                val medicationAdapter = MedicationAdapter(medications) { medication ->
                    lifecycleScope.launch {
                        medicationDao.delete(medication)
                        // 数据变更时触发立即刷新
                        MedicationWorker.enqueueImmediate(requireContext())
                    }
                }
                recyclerView.adapter = object : com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView.Adapter<MedicationAdapter.ViewHolder>(recyclerView) {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationAdapter.ViewHolder {
                        return medicationAdapter.onCreateViewHolder(parent, viewType)
                    }

                    override fun onBindViewHolder(holder: MedicationAdapter.ViewHolder, position: Int) {
                        medicationAdapter.onBindViewHolder(holder, position)
                    }

                    override fun getItemCount(): Int = medicationAdapter.itemCount
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
                    // 添加新药后立即刷新
                    MedicationWorker.enqueueImmediate(requireContext())
                }
            }
            addMedicationFragment.show(childFragmentManager, "AddMedicationFragment")
        }
    }
}
