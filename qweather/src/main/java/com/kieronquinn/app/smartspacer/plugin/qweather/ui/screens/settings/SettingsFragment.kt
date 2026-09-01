package com.kieronquinn.app.smartspacer.plugin.qweather.ui.screens.settings

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kieronquinn.app.smartspacer.plugin.qweather.R
import com.kieronquinn.app.smartspacer.plugin.qweather.utils.AdvicePaging
import com.kieronquinn.app.smartspacer.plugin.qweather.utils.LifestyleDisplayWindow
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR
import com.kieronquinn.app.smartspacer.plugin.qweather.R as QWeatherR

class SettingsFragment : BaseSettingsFragment() {

    private val viewModel by viewModel<SettingsViewModel>()

    override val adapter by lazy {
        object : BaseSettingsAdapter(recyclerView, emptyList()) {}
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: SettingsViewModel.State) = with(binding) {
        when (state) {
            is SettingsViewModel.State.Loading -> {
                settingsBaseLoading.isVisible = true
                settingsBaseRecyclerView.isVisible = false
            }
            is SettingsViewModel.State.Loaded -> {
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), settingsBaseRecyclerView)
            }
        }
    }

    private fun SettingsViewModel.State.Loaded.loadItems(): List<BaseSettingsItem> {
        val durationOptions = listOf(15, 30, 45, 60, 90, 120)
        val items = mutableListOf<BaseSettingsItem>(
            Setting(
                getString(R.string.settings_api_key_title),
                apiKey.ifEmpty { getString(R.string.settings_api_key_summary) },
                ContextCompat.getDrawable(requireContext(), QWeatherR.drawable.ic_key),
                onClick = { showInputDialog(requireContext(), getString(R.string.settings_api_key_title), apiKey) { viewModel.onApiKeyChanged(it) } }
            ),
            Setting(
                getString(R.string.settings_api_host_title),
                apiHost.ifEmpty { getString(R.string.settings_api_host_summary) },
                ContextCompat.getDrawable(requireContext(), QWeatherR.drawable.ic_web),
                onClick = { showInputDialog(requireContext(), getString(R.string.settings_api_host_title), apiHost) { viewModel.onApiHostChanged(it) } }
            ),
            Setting(
                getString(R.string.settings_location_name_title),
                locationName.ifEmpty { getString(R.string.settings_location_name_summary) },
                ContextCompat.getDrawable(requireContext(), QWeatherR.drawable.ic_cloud),
                onClick = { showInputDialog(requireContext(), getString(R.string.settings_location_name_title), locationName) { viewModel.onLocationNameChanged(it) } }
            ),
            Setting(
                getString(R.string.settings_select_indices_title),
                getString(R.string.settings_select_indices_summary),
                ContextCompat.getDrawable(requireContext(), QWeatherR.drawable.ic_list),
                onClick = { showMultiSelectDialog(requireContext(), selectedIndices) { viewModel.onIndicesChanged(it) } }
            ),
            SwitchSetting(
                useEmoji,
                getString(R.string.settings_use_emoji_title),
                getString(R.string.settings_use_emoji_summary),
                ContextCompat.getDrawable(requireContext(), QWeatherR.drawable.ic_face),
                onChanged = viewModel::onUseEmojiChanged
            ),
            Dropdown(
                getString(R.string.settings_paging_mode_title),
                getString(pagingModeSummary(paging)),
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_smartspacer),
                paging,
                { viewModel.onPagingModeChanged(it) },
                AdvicePaging.entries.toList()
            ) { pagingModeLabel(it) },
            Dropdown(
                getString(R.string.settings_page_limit_title),
                getString(R.string.settings_page_limit_summary, pageLimit),
                ContextCompat.getDrawable(requireContext(), QWeatherR.drawable.ic_list),
                pageLimit,
                { viewModel.onPageLimitChanged(it) },
                AdvicePaging.LIMIT_OPTIONS
            ) { getString(R.string.settings_page_limit_option, it) },
            SwitchSetting(
                bedtimeEnabled,
                getString(R.string.settings_bedtime_title),
                getString(R.string.settings_bedtime_summary),
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                onChanged = viewModel::onBedtimeEnabledChanged
            )
        )
        if (bedtimeEnabled) {
            items.add(
                Setting(
                    getString(R.string.settings_bedtime_time_title),
                    LifestyleDisplayWindow.formatClock(bedtimeMinutes),
                    ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                    onClick = {
                        showTimePicker(bedtimeMinutes) { viewModel.onBedtimeMinutesChanged(it) }
                    }
                )
            )
        }
        items.add(
            SwitchSetting(
                wakeEnabled,
                getString(R.string.settings_wake_title),
                getString(R.string.settings_wake_summary),
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                onChanged = viewModel::onWakeEnabledChanged
            )
        )
        if (wakeEnabled) {
            items.add(
                Setting(
                    getString(R.string.settings_wake_time_title),
                    LifestyleDisplayWindow.formatClock(wakeMinutes),
                    ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                    onClick = {
                        showTimePicker(wakeMinutes) { viewModel.onWakeMinutesChanged(it) }
                    }
                )
            )
        }
        if (bedtimeEnabled || wakeEnabled) {
            items.add(
                Dropdown(
                    getString(R.string.settings_display_duration_title),
                    getString(R.string.settings_display_duration_summary, displayDurationMinutes),
                    ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                    displayDurationMinutes,
                    { viewModel.onDisplayDurationChanged(it) },
                    durationOptions
                ) { getString(R.string.settings_display_duration_option, it) }
            )
        }
        return items
    }

    private fun pagingModeLabel(mode: AdvicePaging): String {
        return when (mode) {
            AdvicePaging.NATIVE -> getString(R.string.settings_paging_mode_native)
            AdvicePaging.WIDGET -> getString(R.string.settings_paging_mode_widget)
        }
    }

    private fun pagingModeSummary(mode: AdvicePaging): Int {
        return when (mode) {
            AdvicePaging.NATIVE -> R.string.settings_paging_mode_native_summary
            AdvicePaging.WIDGET -> R.string.settings_paging_mode_widget_summary
        }
    }

    private fun showTimePicker(initialMinutes: Int, onSet: (Int) -> Unit) {
        TimePickerDialog(
            requireContext(),
            { _, hour, minute -> onSet(hour * 60 + minute) },
            initialMinutes / 60,
            initialMinutes % 60,
            true
        ).show()
    }

    private fun showInputDialog(context: Context, title: String, initialValue: String, onValueConfirmed: (String) -> Unit) {
        val editText = EditText(context).apply {
            setText(initialValue)
            setSelection(initialValue.length)
        }
        val container = FrameLayout(context).apply {
            val margin = resources.getDimensionPixelSize(SharedR.dimen.margin_16)
            setPadding(margin, margin / 2, margin, 0)
            addView(editText)
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onValueConfirmed(editText.text.toString())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showMultiSelectDialog(context: Context, selectedIndices: Set<String>, onIndicesConfirmed: (Set<String>) -> Unit) {
        val entries = resources.getStringArray(R.array.indices_entries)
        val entryValues = resources.getStringArray(R.array.indices_values)
        val checkedItems = BooleanArray(entryValues.size) { entryValues[it] in selectedIndices }

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.settings_select_indices_title)
            .setMultiChoiceItems(entries, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val selected = entryValues.filterIndexed { index, _ -> checkedItems[index] }.toSet()
                onIndicesConfirmed(selected)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
