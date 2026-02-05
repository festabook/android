package com.daedan.festabook.presentation.schedule

import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiModel

sealed interface ScheduleEventsUiState {
    data object InitialLoading : ScheduleEventsUiState

    data class Refreshing(
        val lastState: ScheduleEventsUiState,
    ) : ScheduleEventsUiState

    data class Success(
        val events: List<ScheduleEventUiModel>,
        val currentEventPosition: Int,
    ) : ScheduleEventsUiState

    data class Error(
        val throwable: Throwable = Throwable("알수 없는 오류"),
    ) : ScheduleEventsUiState
}
