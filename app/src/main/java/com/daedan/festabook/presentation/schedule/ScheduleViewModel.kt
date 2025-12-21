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
import kotlinx.coroutines.supervisorScope
import java.time.LocalDate

@ContributesIntoMap(AppScope::class)
@ViewModelKey(ScheduleViewModel::class)
@Inject
class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
) : ViewModel() {
    private val _scheduleUiState: MutableStateFlow<ScheduleUiState> =
        MutableStateFlow(ScheduleUiState.InitialLoading)
    val scheduleUiState: StateFlow<ScheduleUiState> = _scheduleUiState.asStateFlow()

    init {
        loadSchedules()
    }

    fun loadSchedules(
        scheduleUiState: ScheduleUiState = ScheduleUiState.InitialLoading,
        scheduleEventUiState: ScheduleEventsUiState = ScheduleEventsUiState.InitialLoading,
        selectedDatePosition: Int? = null,
    ) {
        viewModelScope.launch {
            val datesResult = loadAllDates(scheduleUiState, selectedDatePosition)

            datesResult.onSuccess { scheduleDateUiModels ->
                loadAllEvents(scheduleEventUiState, scheduleDateUiModels)
            }
        }
    }

    private suspend fun loadAllDates(
        scheduleUiState: ScheduleUiState,
        selectedDatePosition: Int?,
    ): Result<List<ScheduleDateUiModel>> {
        _scheduleUiState.value = scheduleUiState
        val result = scheduleRepository.fetchAllScheduleDates()

        return result.fold(
            onSuccess = { scheduleDates ->
                val scheduleDateUiModels = scheduleDates.map { it.toUiModel() }
                val currentDatePosition =
                    selectedDatePosition ?: getCurrentDatePosition(scheduleDates)

                _scheduleUiState.value =
                    ScheduleUiState.Success(
                        dates = scheduleDateUiModels,
                        currentDatePosition = currentDatePosition,
                    )

                Result.success(scheduleDateUiModels)
            },
            onFailure = { throwable ->
                _scheduleUiState.value = ScheduleUiState.Error(throwable)
                Result.failure(throwable)
            },
        )
    }

    private suspend fun loadAllEvents(
        scheduleEventUiState: ScheduleEventsUiState,
        scheduleDateUiModels: List<ScheduleDateUiModel>,
    ) {
        supervisorScope {
            scheduleDateUiModels.forEachIndexed { position, scheduleDateUiModel ->
                launch {
                    loadEventsByPosition(
                        position = position,
                        scheduleDateUiModel = scheduleDateUiModel,
                        scheduleEventsUiState = scheduleEventUiState,
                    )
                }
            }
        }
    }

    private suspend fun loadEventsByPosition(
        position: Int,
        scheduleDateUiModel: ScheduleDateUiModel,
        scheduleEventsUiState: ScheduleEventsUiState,
    ) {
        updateEventUiState(position, scheduleEventsUiState)

        val result =
            scheduleRepository.fetchScheduleEventsById(scheduleDateUiModel.id)

        result
            .onSuccess { scheduleEvents ->
                val uiModels = scheduleEvents.map { it.toUiModel() }
                updateEventUiState(
                    position = position,
                    scheduleEventsUiState =
                        ScheduleEventsUiState.Success(
                            events = uiModels,
                            currentEventPosition = getCurrentEventPosition(uiModels),
                        ),
                )
            }.onFailure {
                updateEventUiState(position, ScheduleEventsUiState.Error(it))
            }
    }

    private fun updateEventUiState(
        position: Int,
        scheduleEventsUiState: ScheduleEventsUiState,
    ) {
        val currentUiState = _scheduleUiState.value
        if (currentUiState !is ScheduleUiState.Success) return

        _scheduleUiState.value =
            currentUiState.copy(
                eventsUiStateByPosition =
                    currentUiState.eventsUiStateByPosition + (position to scheduleEventsUiState),
            )
    }

    private fun getCurrentEventPosition(scheduleEventUiModels: List<ScheduleEventUiModel>): Int {
        val currentEventPosition =
            scheduleEventUiModels
                .indexOfFirst {
                    it.status != ScheduleEventUiStatus.COMPLETED
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
        private const val FIRST_INDEX: Int = 0
    }
}
