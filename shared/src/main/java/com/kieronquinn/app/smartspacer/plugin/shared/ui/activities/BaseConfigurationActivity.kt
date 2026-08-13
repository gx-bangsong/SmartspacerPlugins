package com.kieronquinn.app.smartspacer.plugin.shared.ui.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.color.DynamicColors
import com.kieronquinn.app.shared.R
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenCreated
import com.kieronquinn.app.smartspacer.sdk.utils.applySecurity
import com.kieronquinn.monetcompat.core.MonetCompat

abstract class BaseConfigurationActivity: FragmentActivity() {

    private val monet by lazy { MonetCompat.getInstance() }

    companion object {
        fun createIntent(context: Context, mapping: NavGraphMapping): Intent {
            return Intent().apply {
                applySecurity(context)
                component = ComponentName(
                    context.packageName, "${context.packageName}${mapping.className}"
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // BaseConfigurationActivity no longer extends MonetCompatActivity because
        // that class requires an AppCompat theme. Initialise MonetCompat manually
        // so the Material3 theme can be retained without the AppCompat crash.
        MonetCompat.setup(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        DynamicColors.applyToActivityIfAvailable(this)
        whenCreated {
            monet.awaitMonetReady()
            setContentView(R.layout.activity_configuration)
        }
    }

}