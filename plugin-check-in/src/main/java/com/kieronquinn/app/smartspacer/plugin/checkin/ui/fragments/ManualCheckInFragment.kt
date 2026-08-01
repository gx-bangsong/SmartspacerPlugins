package com.kieronquinn.app.smartspacer.plugin.checkin.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInItem

@Composable
fun CheckInTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content
    )
}

class ManualCheckInFragment : DialogFragment() {

    private var listener: ((CheckInItem) -> Unit)? = null

    fun setOnCheckInAddedListener(listener: (CheckInItem) -> Unit) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent_NoTitleBar)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CheckInTheme {
                    ManualCheckInDialog(
                        onDismiss = { dismiss() },
                        onSave = { item ->
                            listener?.invoke(item)
                            dismiss()
                        }
                    )
                }
            }
        }
    }
}
