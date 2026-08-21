package com.kieronquinn.app.smartspacer.plugin.travel.ui.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kieronquinn.app.smartspacer.plugin.travel.R
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TravelShareOpState

/**
 * In-activity progress / fallback UI for the share-parse flow. The review step itself is shown
 * by the (reused) [ManualPasteFragment] on top of this content.
 */
@Composable
fun ShareFlowContent(
    state: TravelShareOpState?,
    onManualPaste: () -> Unit,
    onClose: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                TravelShareOpState.PARSING -> {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.share_op_parsing_activity_text),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                TravelShareOpState.FAILED -> {
                    Text(
                        text = stringResource(R.string.share_op_failed_activity_text),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(onClick = onManualPaste, modifier = Modifier.padding(top = 16.dp)) {
                        Text(stringResource(R.string.share_op_manual_paste))
                    }
                    Button(onClick = onClose, modifier = Modifier.padding(top = 8.dp)) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
                else -> {
                    // REVIEW_REQUIRED renders the review dialog fragment on top.
                }
            }
        }
    }
}
