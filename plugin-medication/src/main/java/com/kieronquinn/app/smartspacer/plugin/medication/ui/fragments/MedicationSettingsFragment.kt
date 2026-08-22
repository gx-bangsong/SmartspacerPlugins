package com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.kieronquinn.app.shared.R as SharedR
import com.kieronquinn.app.smartspacer.plugin.medication.R
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.databinding.FragmentMedicationSettingsBinding
import com.kieronquinn.app.smartspacer.plugin.medication.permissions.MedicationPermissions
import com.kieronquinn.app.smartspacer.plugin.medication.repositories.MedicationScheduler
import com.kieronquinn.app.smartspacer.plugin.medication.ui.adapters.MedicationAdapter
import com.kieronquinn.app.smartspacer.plugin.medication.work.MedicationWorker
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationIds
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationPermissionHelper
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PermissionOnboardingLauncher
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PermissionOnboardingSettings
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MedicationSettingsFragment : BaseFragment<FragmentMedicationSettingsBinding>(FragmentMedicationSettingsBinding::inflate) {

    private val medicationDao by inject<MedicationDao>()
    private val medicationScheduler by inject<MedicationScheduler>()

    override val adapter by lazy {
        object : BaseSettingsAdapter(recyclerView, emptyList()) {}
    }

    override val recyclerView: LifecycleAwareRecyclerView
        get() = binding.settingsBaseRecyclerView

    override val loadingView: LinearProgressIndicator
        get() = binding.settingsBaseLoadingProgress

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        updateNotificationPermissionRow()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 显式隐藏加载 UI
        binding.settingsBaseLoading.visibility = View.GONE
        binding.permissionOnboardingRow.setOnClickListener {
            PermissionOnboardingLauncher.launch(requireContext(), MedicationPermissions.config)
        }
        binding.notificationPermissionRow.setOnClickListener {
            if (NotificationPermissionHelper.hasNotificationPermission(requireContext())) {
                NotificationPermissionHelper.openNotificationSettings(requireContext())
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                NotificationPermissionHelper.openNotificationSettings(requireContext())
            }
        }
        updateNotificationPermissionRow()
        updatePermissionOnboardingRow()
        setupMedicationList()
        setupFab()
        // 确保定期任务已启动
        MedicationWorker.enqueuePeriodic(requireContext())
    }

    override fun onResume() {
        super.onResume()
        updateNotificationPermissionRow()
        updatePermissionOnboardingRow()
    }

    private fun updatePermissionOnboardingRow() {
        binding.permissionOnboardingRow.text = getString(SharedR.string.permission_onboarding_settings_entry) +
            "\n" + PermissionOnboardingSettings.subtitle(requireContext(), MedicationPermissions.config)
    }

    private fun updateNotificationPermissionRow() {
        binding.notificationPermissionRow.text = if (NotificationPermissionHelper.hasNotificationPermission(requireContext())) {
            getString(R.string.notification_permission_granted)
        } else {
            getString(R.string.notification_permission_denied)
        }
    }

    private fun setupMedicationList() {
        lifecycleScope.launch {
            medicationDao.getAll().collect { medications ->
                binding.settingsBaseLoading.visibility = View.GONE
                val medicationAdapter = MedicationAdapter(medications) { medication ->
                    lifecycleScope.launch {
                        medicationDao.delete(medication)
                        medicationScheduler.cancelAlarm(medication.id)
                        // 删除药物时同步取消对应提醒通知
                        NotificationManagerCompat.from(requireContext()).cancel(
                            NotificationIds.forEntity(NotificationIds.NAMESPACE_MEDICATION, medication.id.toLong())
                        )
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
                    medicationScheduler.rescheduleAll()
                    // 添加新药后立即刷新
                    MedicationWorker.enqueueImmediate(requireContext())
                }
            }
            addMedicationFragment.show(childFragmentManager, "AddMedicationFragment")
        }
    }
}
