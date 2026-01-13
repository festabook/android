package com.daedan.festabook.presentation.placeMap.intent.event

import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel

sealed interface PlaceMapSideEffect {
    data class StartPlaceDetail(
        val placeDetail: LoadState.Success<PlaceDetailUiModel>,
    ) : PlaceMapSideEffect

    data class PreloadImages(
        val places: List<PlaceUiModel>,
    ) : PlaceMapSideEffect

    data class ShowErrorSnackBar(
        val error: LoadState.Error,
    ) : PlaceMapSideEffect

    data class MenuItemReClicked(
        val isPreviewVisible: Boolean,
    ) : PlaceMapSideEffect

    data class MapViewDrag(
        val isPreviewVisible: Boolean,
    ) : PlaceMapSideEffect
}
