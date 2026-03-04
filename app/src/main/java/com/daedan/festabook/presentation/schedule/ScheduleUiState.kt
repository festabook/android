package com.daedan.festabook.presentation.schedule

import com.daedan.festabook.presentation.schedule.model.ScheduleDateUiModel

data class ScheduleUiState(
    val content: Content,
) {
    sealed interface Content {
        data object InitialLoading : Content

        data class Success(
            val dates: List<ScheduleDateUiModel>,
            val currentDatePosition: Int,
            val eventsUiStateByPosition: Map<Int, ScheduleEventsUiState> = emptyMap(),
        ) : Content

        data class Error(
            val throwable: Throwable,
        ) : Content
    }

    companion object {
        const val DEFAULT_POSITION: Int = 0
    }
}
