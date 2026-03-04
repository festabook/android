package com.daedan.festabook.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daedan.festabook.data.datasource.local.FestivalLocalDataSource
import com.daedan.festabook.di.viewmodel.ViewModelKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesIntoMap(AppScope::class)
@ViewModelKey(SplashViewModel::class)
@Inject
class SplashViewModel(
    private val festivalLocalDataSource: FestivalLocalDataSource,
    private val iODispatcher: CoroutineDispatcher = Dispatchers.IO,
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
        viewModelScope.launch(iODispatcher) {
            val festivalId = festivalLocalDataSource.getFestivalId()
            Timber.d("현재 접속중인 festival ID : $festivalId")
            _uiState.value =
                if (festivalId == null) {
                    SplashUiState.NavigateToExplore
                } else {
                    SplashUiState.NavigateToMain(festivalId)
                }
        }
    }
}
