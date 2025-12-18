package com.daedan.festabook.presentation.schedule

import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiModel

sealed interface ScheduleEventsUiState {
    data object InitialLoading : ScheduleEventsUiState

    data class Refreshing(
        val oldEvents: List<ScheduleEventUiModel>,
    ) : ScheduleEventsUiState

    data class Success(
        val eventsByDate: Map<Int, List<ScheduleEventUiModel>>,
        val currentEventPosition: Int,
    ) : ScheduleEventsUiState

    data class Error(
        val throwable: Throwable,
    ) : ScheduleEventsUiState
}
