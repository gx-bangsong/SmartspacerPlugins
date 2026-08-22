package com.kieronquinn.app.smartspacer.plugin.checkin.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import android.app.TimePickerDialog
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Calendar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationPermissionHelper
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PermissionOnboardingLauncher
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PermissionOnboardingSettings
import com.kieronquinn.app.smartspacer.plugin.checkin.permissions.CheckInPermissions
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

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        setupSettingsAndHistory()
    }

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
            viewModel.uiState.collect { state ->
                val records = state.records
                val isRemEnabled = state.reminderEnabled
                val checkInOnly = state.checkInOnly
                val startT = state.workStartTime
                val endT = state.workEndTime
                val customText = state.customReminderText
                val linkedApp = state.linkApp

                val appOptions = listOf("none", "wecom", "dingtalk", "feishu", "feilian")
                val appLabels = mapOf(
                    "none" to getString(R.string.settings_app_none),
                    "wecom" to getString(R.string.settings_app_wecom),
                    "dingtalk" to getString(R.string.settings_app_dingtalk),
                    "feishu" to getString(R.string.settings_app_feishu),
                    "feilian" to getString(R.string.settings_app_feilian)
                )

                // 保留常用快捷时间，同时提供“自定义时间”入口，支持任意分钟。
                val timeOptions = listOf(
                    "07:00", "07:30", "08:00", "08:30", "09:00", "09:30", "10:00",
                    "16:00", "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30",
                    getString(R.string.settings_custom_time)
                )

                val settingsItems = mutableListOf<BaseSettingsItem>(
                    Setting(
                        getString(SharedR.string.permission_onboarding_settings_entry),
                        PermissionOnboardingSettings.subtitle(requireContext(), CheckInPermissions.config),
                        ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                        onClick = {
                            PermissionOnboardingLauncher.launch(requireContext(), CheckInPermissions.config)
                        }
                    ),
                    SwitchSetting(
                        checked = isRemEnabled,
                        title = getString(R.string.settings_enable_reminder),
                        subtitle = getString(R.string.settings_enable_reminder_summary),
                        icon = ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_smartspacer),
                        onChanged = { enabled ->
                            viewModel.setReminderEnabled(enabled)
                            SmartspacerTargetProvider.notifyChange(requireContext(), CheckInProvider::class.java)
                        }
                    ),
                    SwitchSetting(
                        checked = checkInOnly,
                        title = getString(R.string.settings_check_in_only),
                        subtitle = getString(R.string.settings_check_in_only_summary),
                        icon = ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_smartspacer),
                        onChanged = { enabled ->
                            viewModel.setCheckInOnly(enabled)
                            SmartspacerTargetProvider.notifyChange(requireContext(), CheckInProvider::class.java)
                        }
                    ),
                    Dropdown(
                        getString(R.string.settings_work_start_time),
                        startT,
                        ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_smartspacer),
                        setting = startT,
                        onSet = { selected ->
                            if (selected == getString(R.string.settings_custom_time)) {
                                showTimePicker(startT) {
                                    viewModel.setWorkStartTime(it)
                                    SmartspacerTargetProvider.notifyChange(requireContext(), CheckInProvider::class.java)
                                }
                            } else {
                                viewModel.setWorkStartTime(selected)
                                SmartspacerTargetProvider.notifyChange(requireContext(), CheckInProvider::class.java)
                            }
                        },
                        options = timeOptions,
                        adapter = { it }
                    )
                )

                // 仅上班打卡模式下隐藏“下班打卡时间”下拉框
                if (!checkInOnly) {
                    settingsItems.add(
                        Dropdown(
                            getString(R.string.settings_work_end_time),
                            endT,
                            ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_smartspacer),
                            setting = endT,
                            onSet = { selected ->
                                if (selected == getString(R.string.settings_custom_time)) {
                                    showTimePicker(endT) {
                                        viewModel.setWorkEndTime(it)
                                        SmartspacerTargetProvider.notifyChange(requireContext(), CheckInProvider::class.java)
                                    }
                                } else {
                                    viewModel.setWorkEndTime(selected)
                                    SmartspacerTargetProvider.notifyChange(requireContext(), CheckInProvider::class.java)
                                }
                            },
                            options = timeOptions,
                            adapter = { it }
                        )
                    )
                }

                settingsItems.add(
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
                    )
                )
                settingsItems.add(
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
                settingsItems.add(
                    Setting(
                        getString(R.string.settings_notification_permission),
                        notificationPermissionSubtitle(),
                        ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                        onClick = {
                            if (NotificationPermissionHelper.hasNotificationPermission(requireContext())) {
                                NotificationPermissionHelper.openNotificationSettings(requireContext())
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                NotificationPermissionHelper.openNotificationSettings(requireContext())
                            }
                        }
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
                        val subtitle = if (checkInOnly) {
                            "上班: $inStr"
                        } else {
                            "上班: $inStr | 下班: $outStr"
                        }
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

    private fun notificationPermissionSubtitle(): String {
        return if (NotificationPermissionHelper.hasNotificationPermission(requireContext())) {
            getString(R.string.settings_notification_permission_granted)
        } else {
            getString(R.string.settings_notification_permission_denied)
        }
    }

    private fun showTimePicker(current: String, onSelected: (String) -> Unit) {
        val parts = current.split(":")
        val initialHour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23)
            ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val initialMinute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59)
            ?: Calendar.getInstance().get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _: TimePicker, hour: Int, minute: Int ->
                onSelected("%02d:%02d".format(hour, minute))
            },
            initialHour,
            initialMinute,
            true
        ).show()
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val fragment = ManualCheckInFragment()
            fragment.setCheckInOnly(viewModel.checkInOnly.value)
            fragment.setOnCheckInAddedListener { item ->
                viewModel.addRecord(item)
                SmartspacerTargetProvider.notifyChange(requireContext(), CheckInProvider::class.java)
            }
            fragment.show(childFragmentManager, "ManualCheckInFragment")
        }
    }
}
