package com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments

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
import com.kieronquinn.app.smartspacer.plugin.medication.data.Medication

@Composable
fun MedicationTheme(content: @Composable () -> Unit) {
    // Simple theme, ideally this would use Monet or shared theme logic
    MaterialTheme(
        colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content
    )
}

class AddMedicationFragment : DialogFragment() {

    private var listener: ((Medication) -> Unit)? = null

    fun setOnMedicationAddedListener(listener: (Medication) -> Unit) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MedicationTheme {
                    AddMedicationDialog(
                        onDismiss = { dismiss() },
                        onSave = { medication ->
                            listener?.invoke(medication)
                            dismiss()
                        }
                    )
                }
            }
        }
    }
}
