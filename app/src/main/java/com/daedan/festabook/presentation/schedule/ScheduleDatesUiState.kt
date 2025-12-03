package com.daedan.festabook.presentation.schedule

import com.daedan.festabook.presentation.schedule.model.ScheduleDateUiModel

sealed interface ScheduleDatesUiState {
    data object InitialLoading : ScheduleDatesUiState

    data class Success(
        val dates: List<ScheduleDateUiModel>,
        val initialDatePosition: Int,
    ) : ScheduleDatesUiState

    data class Error(
        val throwable: Throwable,
    ) : ScheduleDatesUiState
}
