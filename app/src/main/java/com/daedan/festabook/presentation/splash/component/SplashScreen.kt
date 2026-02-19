package com.daedan.festabook.presentation.splash.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.presentation.splash.AppVersionManager
import com.daedan.festabook.presentation.splash.SplashUiState
import com.daedan.festabook.presentation.splash.SplashViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    appVersionManager: AppVersionManager,
    onNavigateToExplore: () -> Unit,
    onNavigateToMain: (Long) -> Unit,
    onFinishApp: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnNavigateToMain by rememberUpdatedState(onNavigateToMain)
    val currentOnNavigateToExplore by rememberUpdatedState(onNavigateToExplore)

    LaunchedEffect(Unit) {
        // 앱 실행 시 즉시 앱 버전 업데이트의 필요 유무 확인
        val result = appVersionManager.getIsAppUpdateAvailable()
        viewModel.handleVersionCheckResult(result)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SplashUiState.NavigateToExplore -> {
                currentOnNavigateToExplore()
            }

            is SplashUiState.NavigateToMain -> {
                currentOnNavigateToMain(state.festivalId)
            }

            else -> {}
        }
    }

    when (uiState) {
        is SplashUiState.ShowUpdateDialog -> {
            UpdateDialog(
                onConfirm = { appVersionManager.updateApp() },
            )
        }

        is SplashUiState.ShowNetworkErrorDialog -> {
            NetworkErrorDialog(
                onConfirm = onFinishApp,
            )
        }

        else -> {}
    }
}
