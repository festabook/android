package com.daedan.festabook.presentation.splash

sealed interface SplashUiState {
    data object Loading : SplashUiState

    data object ShowUpdateDialog : SplashUiState

    data object ShowNetworkErrorDialog : SplashUiState

    data class NavigateToMain(
        val festivalId: Long,
    ) : SplashUiState

    data object NavigateToExplore : SplashUiState
}
