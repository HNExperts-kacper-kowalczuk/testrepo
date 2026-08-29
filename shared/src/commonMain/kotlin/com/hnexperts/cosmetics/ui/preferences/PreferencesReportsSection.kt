package com.hnexperts.cosmetics.ui.preferences

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.prefs_reports_copy
import com.hnexperts.cosmetics.resources.prefs_reports_copied
import com.hnexperts.cosmetics.resources.prefs_reports_count
import com.hnexperts.cosmetics.resources.prefs_reports_empty
import com.hnexperts.cosmetics.resources.prefs_reports_send
import com.hnexperts.cosmetics.resources.prefs_reports_send_unavailable
import com.hnexperts.cosmetics.resources.prefs_reports_sent
import com.hnexperts.cosmetics.ui.chrome.ButtonIconLabel
import com.hnexperts.cosmetics.ui.common.StatusAnnouncement
import org.jetbrains.compose.resources.stringResource

@Composable
fun PreferencesReportsSection(
    openReportCount: Long,
    reportsCopied: Boolean,
    reportsSent: Boolean,
    reportsSendAvailable: Boolean,
    onCopyReports: (String) -> Unit,
    onSendReports: () -> Unit
) {
    Text(
        text = stringResource(Res.string.prefs_reports_count, openReportCount.toString()),
        style = MaterialTheme.typography.bodyLarge
    )
    val emptyReports: String = stringResource(Res.string.prefs_reports_empty)
    Button(
        onClick = { onCopyReports(emptyReports) },
        modifier = Modifier.fillMaxWidth()
    ) {
        ButtonIconLabel(
            imageVector = Icons.Filled.ContentCopy,
            text = stringResource(Res.string.prefs_reports_copy)
        )
    }
    StatusAnnouncement(
        message = if (reportsCopied) stringResource(Res.string.prefs_reports_copied) else null
    )
    if (reportsSendAvailable) {
        Button(onClick = onSendReports, modifier = Modifier.fillMaxWidth()) {
            ButtonIconLabel(
                imageVector = Icons.AutoMirrored.Filled.Send,
                text = stringResource(Res.string.prefs_reports_send)
            )
        }
    } else {
        Text(text = stringResource(Res.string.prefs_reports_send_unavailable))
    }
    StatusAnnouncement(
        message = if (reportsSent) stringResource(Res.string.prefs_reports_sent) else null
    )
}
