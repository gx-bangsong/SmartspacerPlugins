package com.kieronquinn.app.smartspacer.plugin.water.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.*
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PermissionOnboardingLauncher
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PermissionOnboardingSettings
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.water.permissions.WaterPermissions
import com.kieronquinn.app.smartspacer.plugin.water.repositories.DisplayMode
import com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings.WaterSettingsViewModel
import com.kieronquinn.app.smartspacer.plugin.water.work.WaterWorker
import com.kieronquinn.app.shared.databinding.FragmentSettingsBaseBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class WaterSettingsFragment : BaseFragment<FragmentSettingsBaseBinding>(FragmentSettingsBaseBinding::inflate) {

    private val viewModel: WaterSettingsViewModel by viewModel<WaterSettingsViewModel>()

    override val adapter by lazy {
        object : BaseSettingsAdapter(recyclerView, emptyList()) {}
    }

    override val recyclerView: LifecycleAwareRecyclerView
        get() = binding.settingsBaseRecyclerView

    override val loadingView: LinearProgressIndicator
        get() = binding.settingsBaseLoadingProgress

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsBaseLoading.visibility = View.GONE
        setupState()
        // 确保定期刷新任务已启动
        WaterWorker.enqueuePeriodic(requireContext())
    }

    private fun setupState() {
        whenResumed {
            viewModel.uiState.collect { state ->
                val items = mutableListOf<BaseSettingsItem>()

                items.add(Setting(
                    getString(SharedR.string.permission_onboarding_settings_entry),
                    PermissionOnboardingSettings.subtitle(requireContext(), WaterPermissions.config),
                    ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info)
                ) { PermissionOnboardingLauncher.launch(requireContext(), WaterPermissions.config) })

                items.add(Slider(
                    state.dailyGoalMl.toFloat(), 500f, 5000f, 100f,
                    getString(R.string.setting_daily_goal), "${state.dailyGoalMl}ml",
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_local_drink)
                ) { viewModel.onDailyGoalChanged(it) })

                items.add(Slider(
                    state.cupSizeMl.toFloat(), 100f, 1000f, 50f,
                    getString(R.string.setting_cup_size), "${state.cupSizeMl}ml",
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_local_drink)
                ) { viewModel.onCupSizeChanged(it) })

                items.add(SwitchSetting(
                    state.resetAtActiveStart,
                    getString(R.string.setting_auto_reset),
                    getString(R.string.setting_auto_reset_desc),
                    null
                ) { viewModel.onResetAtActiveStartChanged(it) })

                items.add(SwitchSetting(
                    state.smartAdjust,
                    getString(R.string.setting_smart_adjust),
                    getString(R.string.setting_smart_adjust_desc),
                    null
                ) { viewModel.onSmartAdjustChanged(it) })

                items.add(Dropdown(
                    getString(R.string.setting_display_mode),
                    getString(displayModeLabel(state.displayMode)),
                    null, state.displayMode,
                    { viewModel.onDisplayModeChanged(it) },
                    DisplayMode.values().toList()
                ) {
                    displayModeLabel(it)
                })

                items.add(Setting(
                    getString(R.string.setting_save),
                    getString(R.string.setting_save_desc),
                    ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info)
                ) { viewModel.saveChanges(requireContext()) })

                adapter.update(items)
                binding.settingsBaseLoading.visibility = View.GONE
            }
        }
    }

    private fun displayModeLabel(mode: DisplayMode): Int = when (mode) {
        DisplayMode.PROGRESS -> R.string.display_mode_progress
        DisplayMode.REMINDER -> R.string.display_mode_reminder
        DisplayMode.DYNAMIC -> R.string.display_mode_dynamic
    }
}
