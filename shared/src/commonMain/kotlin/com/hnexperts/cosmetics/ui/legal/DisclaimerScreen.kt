package com.hnexperts.cosmetics.ui.legal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.legal.domain.LegalStore
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.disclaimer_accept
import com.hnexperts.cosmetics.resources.disclaimer_body
import com.hnexperts.cosmetics.resources.disclaimer_title
import com.hnexperts.cosmetics.resources.onboarding_next
import com.hnexperts.cosmetics.resources.onboarding_scan_body
import com.hnexperts.cosmetics.resources.onboarding_scan_title
import com.hnexperts.cosmetics.resources.onboarding_unknown_body
import com.hnexperts.cosmetics.resources.onboarding_unknown_title
import com.hnexperts.cosmetics.resources.a11y_onboarding_step
import com.hnexperts.cosmetics.ui.a11y.screenHeading
import com.hnexperts.cosmetics.ui.common.FailureBanner
import com.hnexperts.cosmetics.ui.layout.AppScrollPane
import com.hnexperts.cosmetics.ui.motion.rememberReduceMotion
import com.hnexperts.cosmetics.ui.runUiAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

data class DisclaimerUiState(
    val busy: Boolean = false,
    val failure: AppFailure? = null,
    val accepted: Boolean = false
)

class DisclaimerViewModel(
    private val legal: LegalStore
) : ViewModel() {
    private val state: MutableStateFlow<DisclaimerUiState> = MutableStateFlow(DisclaimerUiState())
    val uiState: StateFlow<DisclaimerUiState> = state.asStateFlow()

    fun accept() {
        viewModelScope.launch {
            state.update { current -> current.copy(busy = true, failure = null) }
            try {
                runUiAction(::showFailure) { legal.acceptDisclaimer() } ?: return@launch
                state.update { current -> current.copy(accepted = true) }
            } finally {
                state.update { current -> current.copy(busy = false) }
            }
        }
    }

    private fun showFailure(failure: AppFailure) {
        state.update { current -> current.copy(failure = failure) }
    }
}

private data class OnboardingPage(
    val title: StringResource,
    val body: StringResource
)

@Composable
fun DisclaimerScreen(
    viewModel: DisclaimerViewModel,
    onAccepted: () -> Unit
) {
    val uiState: DisclaimerUiState by viewModel.uiState.collectAsState()
    var page: Int by remember { mutableStateOf(0) }
    val pages: List<OnboardingPage> = listOf(
        OnboardingPage(Res.string.onboarding_scan_title, Res.string.onboarding_scan_body),
        OnboardingPage(Res.string.onboarding_unknown_title, Res.string.onboarding_unknown_body),
        OnboardingPage(Res.string.disclaimer_title, Res.string.disclaimer_body)
    )
    LaunchedEffect(uiState.accepted) {
        if (uiState.accepted) {
            onAccepted()
        }
    }
    val current: OnboardingPage = pages[page]
    val lastPage: Boolean = page == pages.lastIndex
    val reduceMotion: Boolean = rememberReduceMotion()
    AppScrollPane(
        modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LinearProgressIndicator(
            progress = { (page + 1).toFloat() / pages.size.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(Res.string.a11y_onboarding_step, page + 1, pages.size),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (reduceMotion) {
            OnboardingCopy(current)
        } else {
            AnimatedContent(
                targetState = current,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { shown ->
                OnboardingCopy(shown)
            }
        }
        FailureBanner(failure = uiState.failure, onRetry = viewModel::accept)
        Button(
            onClick = {
                if (lastPage) {
                    viewModel.accept()
                } else {
                    page += 1
                }
            },
            enabled = !uiState.busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    if (lastPage) Res.string.disclaimer_accept else Res.string.onboarding_next
                )
            )
        }
    }
}

@Composable
private fun OnboardingCopy(page: OnboardingPage) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(page.title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.screenHeading()
        )
        Text(text = stringResource(page.body), style = MaterialTheme.typography.bodyLarge)
    }
}
