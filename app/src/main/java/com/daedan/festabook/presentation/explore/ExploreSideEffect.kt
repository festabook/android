package com.daedan.festabook.presentation.explore

import com.daedan.festabook.presentation.explore.model.SearchResultUiModel

sealed interface ExploreSideEffect {
    data class NavigateToMain(
        val searchResult: SearchResultUiModel,
    ) : ExploreSideEffect
}
