package com.daedan.festabook.presentation.explore

data class ExploreUiState(
    val query: String = "",
    val searchState: SearchUiState = SearchUiState.Idle,
    val hasFestivalId: Boolean = false,
)
