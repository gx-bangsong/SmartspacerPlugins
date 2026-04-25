package com.kieronquinn.app.smartspacer.plugin.water.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.*
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.water.repositories.DisplayMode
import com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings.WaterSettingsViewModel
import com.kieronquinn.app.shared.databinding.FragmentSettingsBaseBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

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
        setupState()
    }

    private fun setupState() {
        whenResumed {
            viewModel.uiState.collect { state ->
                val items = mutableListOf<BaseSettingsItem>()

                items.add(Slider(
                    state.dailyGoalMl.toFloat(), 500f, 5000f, 100f,
                    "Daily Goal", "${state.dailyGoalMl}ml",
                    ContextCompat.getDrawable(requireContext(), com.kieronquinn.app.shared.R.drawable.ic_smartspacer)
                ) { viewModel.onDailyGoalChanged(it) })

                items.add(Slider(
                    state.cupSizeMl.toFloat(), 100f, 1000f, 50f,
                    "Cup Size", "${state.cupSizeMl}ml",
                    ContextCompat.getDrawable(requireContext(), com.kieronquinn.app.shared.R.drawable.ic_smartspacer)
                ) { viewModel.onCupSizeChanged(it) })

                items.add(SwitchSetting(
                    state.resetAtActiveStart,
                    "Automatic Progress Reset",
                    "Reset daily progress at start time",
                    null
                ) { viewModel.onResetAtActiveStartChanged(it) })

                items.add(SwitchSetting(
                    state.smartAdjust,
                    "Smart Adjustments",
                    "Adjust next reminder based on progress",
                    null
                ) { viewModel.onSmartAdjustChanged(it) })

                items.add(Dropdown(
                    "Display Mode", state.displayMode.name,
                    null, state.displayMode,
                    { viewModel.onDisplayModeChanged(it) },
                    DisplayMode.values().toList()
                ) { it.ordinal })

                items.add(Setting(
                    "Save Changes", "Apply new settings and reschedule",
                    ContextCompat.getDrawable(requireContext(), com.kieronquinn.app.shared.R.drawable.ic_smartspacer)
                ) { viewModel.saveChanges(requireContext()) })

                adapter.update(items)
                binding.settingsBaseLoading.visibility = View.GONE
            }
        }
    }
}
