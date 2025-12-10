package com.daedan.festabook.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daedan.festabook.di.viewmodel.ViewModelKey
import com.daedan.festabook.domain.repository.ScheduleRepository
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiStatus
import com.daedan.festabook.presentation.schedule.model.toUiModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

@ContributesIntoMap(AppScope::class)
@ViewModelKey(ScheduleViewModel::class)
@Inject
class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
) : ViewModel() {
    private val _scheduleDatesUiState: MutableStateFlow<ScheduleDatesUiState> =
        MutableStateFlow(ScheduleDatesUiState.InitialLoading)
    val scheduleDatesUiState: StateFlow<ScheduleDatesUiState> = _scheduleDatesUiState.asStateFlow()

    private val _scheduleEventsByDate: MutableStateFlow<Map<Long, ScheduleEventsUiState>> =
        MutableStateFlow(emptyMap())
    val scheduleEventsByDate = _scheduleEventsByDate.asStateFlow()

    private val _selectedDateId: MutableStateFlow<Long?> = MutableStateFlow(null)
    val selectedDateId = _selectedDateId.asStateFlow()

    init {
        loadAllDates()
    }

    fun onDateSelected(dateId: Long) {
        _selectedDateId.value = dateId

        if (_scheduleEventsByDate.value.containsKey(dateId)) return

        loadScheduleByDate(dateId)
    }

    fun loadScheduleByDate(dateId: Long) {
        viewModelScope.launch {
            _scheduleEventsByDate.value[dateId]?.let { return@launch }

            val result = scheduleRepository.fetchScheduleEventsById(dateId)
            val uiState =
                result.fold(
                    onSuccess = { scheduleEvents ->
                        val scheduleEventUiModels = scheduleEvents.map { it.toUiModel() }
                        val currentEventPosition =
                            scheduleEventUiModels
                                .indexOfFirst {
                                    it.status == ScheduleEventUiStatus.ONGOING
                                }.coerceAtLeast(FIRST_INDEX)

                        ScheduleEventsUiState.Success(
                            scheduleEventUiModels,
                            currentEventPosition,
                        )
                    },
                    onFailure = { ScheduleEventsUiState.Error(it) },
                )

            _scheduleEventsByDate.update { old ->
                old + (dateId to uiState)
            }
        }
    }

    fun loadAllDates() {
        viewModelScope.launch {
            _scheduleDatesUiState.value = ScheduleDatesUiState.InitialLoading

            val result = scheduleRepository.fetchAllScheduleDates()

            result
                .onSuccess { scheduleDates ->
                    val scheduleDateUiModels = scheduleDates.map { it.toUiModel() }
                    val today = LocalDate.now()

                    val initialDateId =
                        scheduleDates
                            .find { !it.date.isBefore(today) }
                            ?.id ?: scheduleDates.firstOrNull()?.id

                    _selectedDateId.value = initialDateId

                    _scheduleDatesUiState.value =
                        ScheduleDatesUiState.Success(scheduleDateUiModels, initialDateId)
                }.onFailure {
                    _scheduleDatesUiState.value = ScheduleDatesUiState.Error(it)
                }
        }
    }

    companion object {
        const val INVALID_ID: Long = -1L
        private const val FIRST_INDEX: Int = 0
    }
}
