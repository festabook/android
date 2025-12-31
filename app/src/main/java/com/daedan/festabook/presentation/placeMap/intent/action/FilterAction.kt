package com.daedan.festabook.presentation.placeMap.intent.action

import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel

sealed interface FilterAction : PlaceMapAction {
    data class OnCategoryClick(
        val categories: Set<PlaceCategoryUiModel>,
    ) : FilterAction

    data object OnPlaceLoad : FilterAction
}
