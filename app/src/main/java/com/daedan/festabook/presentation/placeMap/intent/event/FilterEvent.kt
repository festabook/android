package com.daedan.festabook.presentation.placeMap.intent.event

import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel

sealed interface FilterEvent : PlaceMapEvent {
    data class OnCategoryClick(
        val categories: Set<PlaceCategoryUiModel>,
    ) : FilterEvent

    data object OnPlaceLoad : FilterEvent
}
