package com.daedan.festabook.presentation.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.daedan.festabook.di.viewmodel.ViewModelKey
import com.daedan.festabook.domain.repository.FestivalNotificationRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesIntoMap(AppScope::class)
@ViewModelKey(SettingViewModel::class)
@Inject
class SettingViewModel(
    private val festivalNotificationRepository: FestivalNotificationRepository,
) : ViewModel() {
    private val _permissionCheckEvent: MutableSharedFlow<Unit> =
        MutableSharedFlow(
            replay = 1,
        )
    val permissionCheckEvent: SharedFlow<Unit> = _permissionCheckEvent.asSharedFlow()

    private val _isAllowed =
        MutableStateFlow(
            festivalNotificationRepository.getFestivalNotificationIsAllow(),
        )
    val isAllowed: StateFlow<Boolean> = _isAllowed.asStateFlow()

    private val _error: MutableSharedFlow<Throwable> =
        MutableSharedFlow(
            replay = 1,
        )
    val error: SharedFlow<Throwable> = _error.asSharedFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _success: MutableSharedFlow<Unit> =
        MutableSharedFlow(
            replay = 1,
        )
    val success: LiveData<Unit> = _success.asLiveData()
    val successFlow = _success.asSharedFlow()

    fun notificationAllowClick() {
        if (!_isAllowed.value) {
            _permissionCheckEvent.tryEmit(Unit)
        } else {
            deleteNotificationId()
        }
    }

    private fun saveNotificationIsAllowed(isAllowed: Boolean) {
        festivalNotificationRepository.setFestivalNotificationIsAllow(isAllowed)
    }

    private fun updateNotificationIsAllowed(allowed: Boolean) {
        _isAllowed.value = allowed
    }

    fun saveNotificationId() {
        if (_isLoading.value) return
        _isLoading.value = true

        // Optimistic UI 적용, 요청 실패 시 원복
        saveNotificationIsAllowed(true)
        updateNotificationIsAllowed(true)
        _success.tryEmit(Unit)

        viewModelScope.launch {
            val result =
                festivalNotificationRepository.saveFestivalNotification()

            result
                .onFailure {
                    _error.tryEmit(it)
                    saveNotificationIsAllowed(false)
                    updateNotificationIsAllowed(false)
                    Timber.e(it, "${this::class.java.simpleName} NotificationId 저장 실패")
                }.also {
                    _isLoading.value = false
                }
        }
    }

    private fun deleteNotificationId() {
        if (_isLoading.value) return
        _isLoading.value = true

        // Optimistic UI 적용, 요청 실패 시 원복
        saveNotificationIsAllowed(false)
        updateNotificationIsAllowed(false)

        viewModelScope.launch {
            val result =
                festivalNotificationRepository.deleteFestivalNotification()

            result
                .onFailure {
                    _error.tryEmit(it)
                    saveNotificationIsAllowed(true)
                    updateNotificationIsAllowed(true)
                    Timber.e(it, "${this::class.java.simpleName} NotificationId 삭제 실패")
                }.also {
                    _isLoading.value = false
                }
        }
    }
}
