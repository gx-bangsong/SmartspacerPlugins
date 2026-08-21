package com.kieronquinn.app.smartspacer.plugin.travel.ui.fragments

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
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TravelShareDraft
import com.kieronquinn.app.smartspacer.shared.smsparser.TravelInfo

@Composable
fun TravelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content
    )
}

/**
 * The reusable "paste → parse → editable review" dialog. Used both from the settings screen
 * (manual paste) and from the share flow (pre-filled review, source = "share").
 *
 * Only non-sensitive fields are passed into [setInitialTravelInfo] (the share flow never passes
 * the passenger name or the raw text).
 */
class ManualPasteFragment : DialogFragment() {

    private var listener: ((TravelInfoItem) -> Unit)? = null
    private var cancelListener: (() -> Unit)? = null
    private var initialInfo: TravelInfo? = null
    private var source: String = "manual"
    private var saved = false

    fun setOnTravelItemAddedListener(listener: (TravelInfoItem) -> Unit) {
        this.listener = listener
    }

    fun setOnCancelListener(listener: () -> Unit) {
        this.cancelListener = listener
    }

    /** Pre-fills the review form from a share-flow draft (never contains the raw text). */
    fun setInitialTravelInfo(draft: TravelShareDraft?) {
        initialInfo = draft?.let {
            TravelInfo(
                trainNumber = it.trainNumber,
                departureStation = it.departureStation,
                arrivalStation = it.arrivalStation,
                departureTime = it.departureTime,
                seat = it.seat,
                passengerName = null,
                rawText = ""
            )
        }
    }

    fun setSource(source: String) {
        this.source = source
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
                TravelTheme {
                    ManualPasteDialog(
                        initialInfo = initialInfo,
                        source = source,
                        onDismiss = {
                            if (!saved) cancelListener?.invoke()
                            dismiss()
                        },
                        onSave = { item ->
                            saved = true
                            listener?.invoke(item)
                            dismiss()
                        }
                    )
                }
            }
        }
    }
}
