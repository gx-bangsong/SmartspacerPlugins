package com.kieronquinn.app.smartspacer.plugin.parcel.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
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

    private val importRulesLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            lifecycleScope.launch {
                val json = requireContext().contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() }
                if (json != null) {
                    val success = RuleManager(requireContext()).importRulesFromJson(json)
                    val message = if (success) "Rules imported successfully" else "Failed to import rules"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettings()
    }

    private fun setupSettings() {
        binding.settingsBaseLoading.visibility = View.GONE
        val items = listOf<BaseSettingsItem>(
            Setting(
                getString(R.string.plugin_description),
                "Privacy: All SMS processing is local.",
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                onClick = {}
            ),
            Setting(
                "Scan Inbox",
                "Scan historical SMS for parcels",
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_search),
                onClick = { checkPermissionsAndScan() }
            ),
            Setting(
                "Import Rules",
                "Load custom JSON parsing rules",
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_libraries),
                onClick = { importRulesLauncher.launch("application/json") }
            )
        )
        adapter.update(items)
        binding.settingsBaseLoading.visibility = View.GONE
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
