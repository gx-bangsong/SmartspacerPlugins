package com.kieronquinn.app.smartspacer.plugin.parcel.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.smartspacer.plugin.parcel.R
import com.kieronquinn.app.smartspacer.plugin.parcel.engine.InboxScanner
import com.kieronquinn.app.smartspacer.plugin.parcel.engine.RuleManager
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationPermissionHelper
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PermissionOnboardingLauncher
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.PermissionOnboardingSettings
import com.kieronquinn.app.smartspacer.plugin.parcel.permissions.ParcelPermissions
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class SettingsFragment : BaseSettingsFragment() {

    private val viewModel by viewModel<SettingsViewModel>()

    override val adapter by lazy {
        object : BaseSettingsAdapter(recyclerView, emptyList()) {}
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_SMS] == true) {
            scanInbox()
        }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        setupSettings(viewModel.cleanupDurationHours.value, viewModel.promotedLiveUpdates.value)
    }

    private val importRulesLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            lifecycleScope.launch {
                val json = requireContext().contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() }
                if (json != null) {
                    val success = RuleManager(requireContext()).importRulesFromJson(json)
                    val message = if (success) getString(R.string.rules_imported) else getString(R.string.rules_import_failed)
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 显式隐藏加载 UI
        binding.settingsBaseLoading.visibility = View.GONE

        lifecycleScope.launchWhenStarted {
            viewModel.cleanupDurationHours.collect {
                setupSettings(it, viewModel.promotedLiveUpdates.value)
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.promotedLiveUpdates.collect {
                setupSettings(viewModel.cleanupDurationHours.value, it)
            }
        }
    }

    private fun setupSettings(currentDuration: Int, promotedEnabled: Boolean) {
        val durationOptions = listOf(12, 24, 48, 72)
        val items = mutableListOf<BaseSettingsItem>(
            Setting(
                getString(SharedR.string.permission_onboarding_settings_entry),
                PermissionOnboardingSettings.subtitle(requireContext(), ParcelPermissions.config),
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                onClick = {
                    PermissionOnboardingLauncher.launch(requireContext(), ParcelPermissions.config)
                }
            ),
            Setting(
                getString(R.string.plugin_description),
                getString(R.string.privacy_note),
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                onClick = {}
            ),
            Dropdown(
                getString(R.string.cleanup_duration),
                getString(R.string.cleanup_duration_summary),
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_smartspacer),
                setting = currentDuration,
                onSet = { hours ->
                    viewModel.setCleanupDurationHours(hours)
                },
                options = durationOptions,
                adapter = { getString(R.string.hours_format, it) }
            ),
            Setting(
                getString(R.string.scan_inbox),
                getString(R.string.scan_inbox_summary),
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_search),
                onClick = { checkPermissionsAndScan() }
            ),
            Setting(
                getString(R.string.import_rules),
                getString(R.string.import_rules_summary),
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_libraries),
                onClick = { importRulesLauncher.launch("application/json") }
            ),
            SwitchSetting(
                checked = promotedEnabled,
                title = getString(R.string.experimental_promoted_title),
                subtitle = getString(R.string.experimental_promoted_summary),
                icon = ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_smartspacer),
                onChanged = { enabled ->
                    viewModel.setPromotedLiveUpdates(enabled)
                    if (enabled) {
                        Toast.makeText(
                            requireContext(),
                            R.string.experimental_promoted_warning,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            ),
            Setting(
                getString(R.string.notification_permission),
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
        adapter.update(items)
    }

    private fun notificationPermissionSubtitle(): String {
        return if (NotificationPermissionHelper.hasNotificationPermission(requireContext())) {
            getString(R.string.notification_permission_granted)
        } else {
            getString(R.string.notification_permission_denied)
        }
    }

    private fun checkPermissionsAndScan() {
        val permissions = arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
        if (permissions.all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }) {
            scanInbox()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun scanInbox() {
        lifecycleScope.launch {
            InboxScanner(requireContext()).scan()
            Toast.makeText(requireContext(), "Inbox scan complete", Toast.LENGTH_SHORT).show()
        }
    }
}
