package com.daedan.festabook.presentation.placeMap.intent.event

import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel

sealed interface MapControlEvent : PlaceMapEvent {
    data object OnMapReady : MapControlEvent

    data object OnMapDrag : MapControlEvent

    data class OnPlaceLoadFinish(
        val places: List<PlaceUiModel>,
    ) : MapControlEvent

    data object OnBackToInitialPositionClick : MapControlEvent
}
