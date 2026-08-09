package com.kieronquinn.app.smartspacer.plugin.checkin.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.smartspacer.plugin.checkin.R
import com.kieronquinn.app.smartspacer.plugin.checkin.databinding.FragmentCheckInSettingsBinding
import com.kieronquinn.app.smartspacer.plugin.checkin.providers.CheckInProvider
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Header
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class CheckInSettingsFragment : BaseFragment<FragmentCheckInSettingsBinding>(FragmentCheckInSettingsBinding::inflate) {

    private val viewModel by viewModel<CheckInSettingsViewModel>()

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

        setupSettingsAndHistory()
        setupFab()
    }

    private fun setupSettingsAndHistory() {
        lifecycleScope.launch {
            viewModel.historyList.collect { records ->
                val isRemEnabled = viewModel.isReminderEnabled.value
                val startT = viewModel.workStartTime.value
                val endT = viewModel.workEndTime.value
                val customText = viewModel.customReminderText.value
                val linkedApp = viewModel.linkApp.value

                val appOptions = listOf("none", "wecom", "dingtalk", "feishu", "feilian")
                val appLabels = mapOf(
                    "none" to getString(R.string.settings_app_none),
                    "wecom" to getString(R.string.settings_app_wecom),
                    "dingtalk" to getString(R.string.settings_app_dingtalk),
                    "feishu" to getString(R.string.settings_app_feishu),
                    "feilian" to getString(R.string.settings_app_feilian)
                )

                val timeOptions = listOf(
                    "07:00", "07:30", "08:00", "08:30", "09:00", "09:30", "10:00",
                    "16:00", "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30"
                )

                val settingsItems = mutableListOf<BaseSettingsItem>(
                    SwitchSetting(
                        checked = isRemEnabled,
                        title = getString(R.string.settings_enable_reminder),
                        subtitle = getString(R.string.settings_enable_reminder_summary),
                        icon = ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_smartspacer),
                        onChanged = { enabled ->
                            viewModel.setReminderEnabled(enabled)
                        }
                    ),
                    Dropdown(
                        getString(R.string.settings_work_start_time),
                        getString(R.string.settings_work_start_time),
                        ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_smartspacer),
                        setting = startT,
                        onSet = { viewModel.setWorkStartTime(it) },
                        options = timeOptions,
                        adapter = { it }
                    ),
                    Dropdown(
                        getString(R.string.settings_work_end_time),
                        getString(R.string.settings_work_end_time),
                        ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_smartspacer),
                        setting = endT,
                        onSet = { viewModel.setWorkEndTime(it) },
                        options = timeOptions,
                        adapter = { it }
                    ),
                    Setting(
                        getString(R.string.settings_custom_reminder_text),
                        customText.ifBlank { getString(R.string.default_reminder_text) },
                        ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                        onClick = {
                            val input = EditText(requireContext()).apply {
                                setText(customText)
                            }
                            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setTitle(R.string.settings_custom_reminder_text)
                                .setView(input)
                                .setPositiveButton(android.R.string.ok) { _, _ ->
                                    val txt = input.text.toString().trim()
                                    viewModel.setCustomReminderText(txt)
                                }
                                .setNegativeButton(android.R.string.cancel, null)
                                .show()
                        }
                    ),
                    Dropdown(
                        getString(R.string.settings_checkin_app),
                        getString(R.string.settings_checkin_app_summary),
                        ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_libraries),
                        setting = linkedApp,
                        onSet = { viewModel.setLinkApp(it) },
                        options = appOptions,
                        adapter = { appLabels[it] ?: it }
                    )
                )

                settingsItems.add(Header(getString(R.string.settings_history_header)))

                if (records.isEmpty()) {
                    settingsItems.add(
                        Setting(
                            getString(R.string.settings_no_history),
                            "",
                            null,
                            onClick = {}
                        )
                    )
                } else {
                    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    for (record in records) {
                        val inStr = if (record.checkInTime != null) sdf.format(java.util.Date(record.checkInTime)) else "未打卡"
                        val outStr = if (record.checkOutTime != null) sdf.format(java.util.Date(record.checkOutTime)) else "未打卡"
                        val subtitle = "上班: $inStr | 下班: $outStr"
                        settingsItems.add(
                            Setting(
                                record.date,
                                subtitle,
                                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_close),
                                onClick = {
                                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                        .setTitle(R.string.settings_delete)
                                        .setMessage(R.string.settings_delete_confirm)
                                        .setPositiveButton(android.R.string.ok) { _, _ ->
                                            viewModel.deleteRecord(record)
                                            SmartspacerTargetProvider.notifyChange(requireContext(), CheckInProvider::class.java)
                                            Toast.makeText(requireContext(), "记录已删除", Toast.LENGTH_SHORT).show()
                                        }
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .show()
                                }
                            )
                        )
                    }
                }

                adapter.update(settingsItems)
            }
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val fragment = ManualCheckInFragment()
            fragment.setOnCheckInAddedListener { item ->
                viewModel.addRecord(item)
                SmartspacerTargetProvider.notifyChange(requireContext(), CheckInProvider::class.java)
            }
            fragment.show(childFragmentManager, "ManualCheckInFragment")
        }
    }
}
