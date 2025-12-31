package com.daedan.festabook.presentation.placeMap.intent.action

import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel

sealed interface MapEventAction : PlaceMapAction {
    data object OnMapReady : MapEventAction

    data object OnMapDrag : MapEventAction

    data class OnPlaceLoadFinish(
        val places: List<PlaceUiModel>,
    ) : MapEventAction

    data object OnBackToInitialPositionClick : MapEventAction
}
