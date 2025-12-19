package com.daedan.festabook.presentation.schedule

import com.daedan.festabook.presentation.schedule.model.ScheduleDateUiModel

sealed interface ScheduleUiState {
    data object InitialLoading : ScheduleUiState

    data class Refreshing(
        val lastSuccessState: Success,
    ) : ScheduleUiState

    data class Success(
        val dates: List<ScheduleDateUiModel>,
        val currentDatePosition: Int,
        val eventsUiStateByPosition: Map<Int, ScheduleEventsUiState> = emptyMap(),
    ) : ScheduleUiState

    data class Error(
        val throwable: Throwable,
    ) : ScheduleUiState

    companion object {
        const val DEFAULT_POSITION: Int = 0
    }
}
