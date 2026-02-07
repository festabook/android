package com.daedan.festabook.presentation.explore

import com.daedan.festabook.presentation.explore.model.SearchResultUiModel

sealed interface SearchUiState {
    val isEmptyResult: Boolean get() = false
    val isFailure: Boolean get() = this is Error
    val shouldShowErrorUi: Boolean get() = isFailure || isEmptyResult

    data object Idle : SearchUiState

    data object Loading : SearchUiState

    data class Success(
        val universitiesFound: List<SearchResultUiModel> = emptyList(),
//        val selectedUniversity: University? = null,
    ) : SearchUiState {
        override val isEmptyResult: Boolean
            get() = universitiesFound.isEmpty()
    }

    data class Error(
        val throwable: Throwable,
    ) : SearchUiState
}
