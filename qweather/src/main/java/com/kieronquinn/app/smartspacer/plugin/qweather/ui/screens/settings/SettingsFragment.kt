package com.kieronquinn.app.smartspacer.plugin.qweather.ui.screens.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kieronquinn.app.smartspacer.plugin.qweather.R
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
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
        return listOf(
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
            )
        )
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
