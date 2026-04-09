package com.kieronquinn.app.smartspacer.plugin.parcel.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.smartspacer.plugin.parcel.databinding.FragmentSettingsBinding
import com.kieronquinn.app.smartspacer.plugin.parcel.engine.InboxScanner
import com.kieronquinn.app.smartspacer.plugin.parcel.engine.RuleManager
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnScanInbox.setOnClickListener {
            checkPermissionsAndScan()
        }

        binding.btnImportRules.setOnClickListener {
            importRulesLauncher.launch("application/json")
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
            binding.btnScanInbox.isEnabled = false
            InboxScanner(requireContext()).scan()
            binding.btnScanInbox.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
