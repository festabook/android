package com.daedan.festabook.presentation.placeMap.intent.event

import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel

sealed interface PlaceMapEvent {
    data class StartPlaceDetail(
        val placeDetail: LoadState.Success<PlaceDetailUiModel>,
    ) : PlaceMapEvent

    data class PreloadImages(
        val places: List<PlaceUiModel>,
    ) : PlaceMapEvent

    data class ShowErrorSnackBar(
        val error: LoadState.Error,
    ) : PlaceMapEvent

    data class MenuItemReClicked(
        val isPreviewVisible: Boolean,
    ) : PlaceMapEvent

    data class MapViewDrag(
        val isPreviewVisible: Boolean,
    ) : PlaceMapEvent
}
