package com.daedan.festabook.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daedan.festabook.di.viewmodel.ViewModelKey
import com.daedan.festabook.domain.model.ScheduleDate
import com.daedan.festabook.domain.repository.ScheduleRepository
import com.daedan.festabook.presentation.schedule.model.ScheduleDateUiModel
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiModel
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiStatus
import com.daedan.festabook.presentation.schedule.model.toUiModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

@ContributesIntoMap(AppScope::class)
@ViewModelKey(ScheduleViewModel::class)
@Inject
class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
) : ViewModel() {
    private val _scheduleDateUiState: MutableStateFlow<ScheduleDatesUiState> =
        MutableStateFlow(ScheduleDatesUiState.InitialLoading)
    val scheduleDateUiState: StateFlow<ScheduleDatesUiState> = _scheduleDateUiState.asStateFlow()

    private val _scheduleEventUiState: MutableStateFlow<ScheduleEventsUiState> =
        MutableStateFlow(ScheduleEventsUiState.InitialLoading)
    val scheduleEventUiState: StateFlow<ScheduleEventsUiState> = _scheduleEventUiState.asStateFlow()

    init {
        loadSchedules(scheduleEventUiState = ScheduleEventsUiState.InitialLoading)
    }

    fun onDateSelected(selectedPosition: Int) {
        (_scheduleDateUiState.value as? ScheduleDatesUiState.Success)?.let {
            _scheduleDateUiState.value = it.copy(currentDatePosition = selectedPosition)
        }
    }

    fun loadSchedules(
        scheduleEventUiState: ScheduleEventsUiState,
        selectedDatePosition: Int? = null,
    ) {
        viewModelScope.launch {
            _scheduleEventUiState.value = scheduleEventUiState
            val datesResult = loadAllDates(selectedDatePosition)

            datesResult.onSuccess { scheduleDateUiModels ->
                loadEvents(scheduleDateUiModels)
            }
        }
    }

    private suspend fun loadAllDates(selectedDatePosition: Int?): Result<List<ScheduleDateUiModel>> {
        _scheduleDateUiState.value = ScheduleDatesUiState.InitialLoading
        val result = scheduleRepository.fetchAllScheduleDates()

        return result.fold(
            onSuccess = { scheduleDates ->
                val scheduleDateUiModels = scheduleDates.map { it.toUiModel() }
                val currentDatePosition =
                    selectedDatePosition ?: getCurrentDatePosition(scheduleDates)

                _scheduleDateUiState.value =
                    ScheduleDatesUiState.Success(
                        dates = scheduleDateUiModels,
                        currentDatePosition = currentDatePosition,
                    )

                Result.success(scheduleDateUiModels)
            },
            onFailure = { throwable ->
                _scheduleDateUiState.value = ScheduleDatesUiState.Error(throwable)
                Result.failure(throwable)
            },
        )
    }

    private suspend fun loadEvents(scheduleDateUiModels: List<ScheduleDateUiModel>) {
        val allEvents = mutableMapOf<Int, List<ScheduleEventUiModel>>()
        scheduleDateUiModels.forEachIndexed { position, scheduleDateUiModel ->
            val eventsResult =
                scheduleRepository.fetchScheduleEventsById(scheduleDateUiModel.id)

            eventsResult
                .onSuccess { scheduleEvents ->
                    val scheduleEventUiModels = scheduleEvents.map { it.toUiModel() }
                    allEvents[position] = scheduleEventUiModels

                    val currentEventPosition =
                        getCurrentEventPosition(scheduleEventUiModels)

                    _scheduleEventUiState.value =
                        ScheduleEventsUiState.Success(
                            eventsByDate = allEvents,
                            currentEventPosition = currentEventPosition,
                        )
                }.onFailure {
                    _scheduleEventUiState.value = ScheduleEventsUiState.Error(it)
                }
        }
    }

    private fun getCurrentEventPosition(scheduleEventUiModels: List<ScheduleEventUiModel>): Int {
        val currentEventPosition =
            scheduleEventUiModels
                .indexOfFirst {
                    it.status == ScheduleEventUiStatus.ONGOING
                }.coerceAtLeast(FIRST_INDEX)
        return currentEventPosition
    }

    private fun getCurrentDatePosition(scheduleDates: List<ScheduleDate>): Int {
        val today = LocalDate.now()
        val currentDatePosition =
            scheduleDates
                .indexOfFirst { !it.date.isBefore(today) }
                .coerceAtLeast(FIRST_INDEX)
        return currentDatePosition
    }

    companion object {
        const val INVALID_ID: Long = -1L
        private const val FIRST_INDEX: Int = 0
    }
}
