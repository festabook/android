package com.daedan.festabook.presentation.schedule

import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiModel

data class ScheduleEventsUiState(
    val content: Content,
    val isRefreshing: Boolean = false,
) {
    sealed interface Content {
        data object InitialLoading : Content

        data class Success(
            val events: List<ScheduleEventUiModel>,
            val currentEventPosition: Int,
        ) : Content {
            val isEventsEmpty get() = events.isEmpty()
        }

        data class Error(
            val throwable: Throwable,
        ) : Content
    }
}
