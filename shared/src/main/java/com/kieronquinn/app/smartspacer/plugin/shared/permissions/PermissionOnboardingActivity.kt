package com.kieronquinn.app.smartspacer.plugin.shared.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.color.DynamicColors
import com.kieronquinn.app.shared.R
import com.kieronquinn.app.shared.databinding.ActivityPermissionOnboardingBinding
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.LiveUpdateEligibility
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationPermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/**
 * Step-by-step permission wizard. Each plugin APK only receives the capabilities it declared.
 * Runtime permissions are requested one at a time after an explanation; special permissions
 * (exact alarms, promoted Live Updates) never go through `requestPermissions()`.
 */
class PermissionOnboardingActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CAPABILITIES = "extra_capabilities"
        const val EXTRA_VERSION = "extra_onboarding_version"

        fun createIntent(context: Context, config: PluginPermissionConfig): Intent {
            return Intent(context, PermissionOnboardingActivity::class.java).apply {
                putExtra(EXTRA_CAPABILITIES, config.capabilities.map { it.name }.toTypedArray())
                putExtra(EXTRA_VERSION, config.onboardingVersion)
            }
        }
    }

    private lateinit var binding: ActivityPermissionOnboardingBinding
    private lateinit var capabilities: List<PluginCapability>
    private lateinit var statusRepository: PermissionStatusRepository
    private var stepIndex = 0
    private var lastExactGranted: Boolean? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { bindCurrentStep() }

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            GlobalContext.getOrNull()?.getOrNull<SmsPermissionFallback>()?.onSmsPermissionDenied()
        }
        bindCurrentStep()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        DynamicColors.applyToActivityIfAvailable(this)
        capabilities = intent.getStringArrayExtra(EXTRA_CAPABILITIES)
            ?.mapNotNull { name -> runCatching { PluginCapability.valueOf(name) }.getOrNull() }
            .orEmpty()
        val version = intent.getIntExtra(EXTRA_VERSION, PermissionOnboardingCoordinator.CURRENT_VERSION)
        PermissionOnboardingCoordinator(
            SharedPreferencesOnboardingVersionStore.from(this),
            PluginPermissionConfig(capabilities, version)
        ).markShown()

        if (capabilities.isEmpty()) {
            finish()
            return
        }

        statusRepository = PermissionStatusRepository(this)
        binding = ActivityPermissionOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.permissionOnboardingToolbar.setNavigationOnClickListener { finish() }
        binding.permissionOnboardingSkip.setOnClickListener { finish() }
        binding.permissionOnboardingNext.setOnClickListener { onNext() }
        binding.permissionOnboardingAction.setOnClickListener { onAction() }

        bindCurrentStep()
    }

    override fun onResume() {
        super.onResume()
        if (!::capabilities.isInitialized || capabilities.isEmpty()) return
        bindCurrentStep()
        recheckExactAlarmGrant()
    }

    private fun currentCapability(): PluginCapability = capabilities[stepIndex]

    private fun bindCurrentStep() {
        val capability = currentCapability()
        val snapshot = statusRepository.snapshot(capability)
        binding.permissionOnboardingStep.text = getString(
            R.string.permission_onboarding_step,
            stepIndex + 1,
            capabilities.size
        )
        binding.permissionOnboardingCapabilityTitle.setText(titleRes(capability))
        binding.permissionOnboardingPurpose.setText(purposeRes(capability))
        binding.permissionOnboardingStatus.setText(statusRes(snapshot.status))
        binding.permissionOnboardingDegradation.setText(degradationRes(capability))

        val actionLabel = actionLabel(snapshot.action)
        if (actionLabel == 0) {
            binding.permissionOnboardingAction.visibility = View.GONE
        } else {
            binding.permissionOnboardingAction.visibility = View.VISIBLE
            binding.permissionOnboardingAction.setText(actionLabel)
        }

        val isLast = stepIndex == capabilities.lastIndex
        binding.permissionOnboardingNext.setText(
            if (isLast) R.string.permission_onboarding_done else R.string.permission_onboarding_next
        )
    }

    private fun onNext() {
        if (stepIndex >= capabilities.lastIndex) {
            finish()
        } else {
            stepIndex++
            bindCurrentStep()
        }
    }

    private fun onAction() {
        val snapshot = statusRepository.snapshot(currentCapability())
        when (snapshot.action) {
            CapabilityAction.NONE -> Unit
            CapabilityAction.REQUEST_RUNTIME -> requestRuntime(currentCapability())
            CapabilityAction.OPEN_APP_NOTIFICATION_SETTINGS ->
                NotificationPermissionHelper.openNotificationSettings(this)
            CapabilityAction.OPEN_EXACT_ALARM_SETTINGS -> openExactAlarmSettings()
            CapabilityAction.OPEN_PROMOTED_SETTINGS -> openPromotedSettingsIfSupported()
        }
    }

    private fun requestRuntime(capability: PluginCapability) {
        when (capability) {
            PluginCapability.NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            PluginCapability.SMS_RECEIVE ->
                smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
            PluginCapability.SMS_READ ->
                smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
            PluginCapability.EXACT_ALARMS,
            PluginCapability.PROMOTED_LIVE_UPDATES -> Unit
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:$packageName")
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            // No settings activity available.
        }
    }

    private fun openPromotedSettingsIfSupported() {
        if (!LiveUpdateEligibility.isAtLeastBaklavaQpr1()) return
        NotificationPermissionHelper.openPromotedSettings(this)
    }

    private fun recheckExactAlarmGrant() {
        if (!capabilities.contains(PluginCapability.EXACT_ALARMS)) return
        val granted = ExactAlarmCompat.hasPermission(this)
        val previous = lastExactGranted
        lastExactGranted = granted
        if (granted && previous == false) {
            val rescheduler = GlobalContext.getOrNull()?.getOrNull<ExactAlarmRescheduler>() ?: return
            CoroutineScope(Dispatchers.IO).launch {
                ExactAlarmPermissionHandler(rescheduler).onResumeRecheck(true)
            }
        }
    }

    private fun titleRes(capability: PluginCapability): Int = when (capability) {
        PluginCapability.NOTIFICATIONS -> R.string.permission_onboarding_notifications_title
        PluginCapability.SMS_RECEIVE -> R.string.permission_onboarding_sms_receive_title
        PluginCapability.SMS_READ -> R.string.permission_onboarding_sms_read_title
        PluginCapability.EXACT_ALARMS -> R.string.permission_onboarding_exact_alarms_title
        PluginCapability.PROMOTED_LIVE_UPDATES -> R.string.permission_onboarding_promoted_title
    }

    private fun purposeRes(capability: PluginCapability): Int = when (capability) {
        PluginCapability.NOTIFICATIONS -> R.string.permission_onboarding_notifications_purpose
        PluginCapability.SMS_RECEIVE -> R.string.permission_onboarding_sms_receive_purpose
        PluginCapability.SMS_READ -> R.string.permission_onboarding_sms_read_purpose
        PluginCapability.EXACT_ALARMS -> R.string.permission_onboarding_exact_alarms_purpose
        PluginCapability.PROMOTED_LIVE_UPDATES -> R.string.permission_onboarding_promoted_purpose
    }

    private fun degradationRes(capability: PluginCapability): Int = when (capability) {
        PluginCapability.NOTIFICATIONS -> R.string.permission_onboarding_notifications_degradation
        PluginCapability.SMS_RECEIVE -> R.string.permission_onboarding_sms_receive_degradation
        PluginCapability.SMS_READ -> R.string.permission_onboarding_sms_read_degradation
        PluginCapability.EXACT_ALARMS -> R.string.permission_onboarding_exact_alarms_degradation
        PluginCapability.PROMOTED_LIVE_UPDATES -> R.string.permission_onboarding_promoted_degradation
    }

    private fun statusRes(status: CapabilityStatus): Int = when (status) {
        CapabilityStatus.GRANTED -> R.string.permission_onboarding_status_granted
        CapabilityStatus.DENIED -> R.string.permission_onboarding_status_denied
        CapabilityStatus.NOT_REQUIRED -> R.string.permission_onboarding_status_not_required
        CapabilityStatus.UNSUPPORTED -> R.string.permission_onboarding_status_unsupported
        CapabilityStatus.SETTINGS_DISABLED -> R.string.permission_onboarding_status_settings_disabled
    }

    private fun actionLabel(action: CapabilityAction): Int = when (action) {
        CapabilityAction.NONE -> 0
        CapabilityAction.REQUEST_RUNTIME -> R.string.permission_onboarding_grant
        CapabilityAction.OPEN_APP_NOTIFICATION_SETTINGS,
        CapabilityAction.OPEN_EXACT_ALARM_SETTINGS,
        CapabilityAction.OPEN_PROMOTED_SETTINGS -> R.string.permission_onboarding_open_settings
    }
}
