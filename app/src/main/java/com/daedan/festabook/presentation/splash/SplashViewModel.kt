package com.daedan.festabook.presentation.splash

import androidx.lifecycle.ViewModel
import com.daedan.festabook.data.datasource.local.FestivalLocalDataSource
import com.daedan.festabook.di.viewmodel.ViewModelKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

@ContributesIntoMap(AppScope::class)
@ViewModelKey(SplashViewModel::class)
@Inject
class SplashViewModel(
    private val festivalLocalDataSource: FestivalLocalDataSource,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun handleVersionCheckResult(result: Result<Boolean>) {
        result
            .onSuccess { isUpdateAvailable ->
                if (isUpdateAvailable) {
                    _uiState.value = SplashUiState.ShowUpdateDialog
                } else {
                    checkFestivalId()
                }
            }.onFailure {
                _uiState.value = SplashUiState.ShowNetworkErrorDialog
            }
    }

    private fun checkFestivalId() {
        val festivalId = festivalLocalDataSource.getFestivalId()
        Timber.d("현재 접속중인 festival ID : $festivalId")

        if (festivalId == null) {
            _uiState.value = SplashUiState.NavigateToExplore
        } else {
            _uiState.value = SplashUiState.NavigateToMain(festivalId)
        }
    }
}
