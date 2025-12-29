package com.daedan.festabook.presentation.placeMap.viewmodel

import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.model.InitialMapSettingUiModel
import com.daedan.festabook.presentation.placeMap.model.LoadState
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCoordinateUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel

sealed interface PlaceMapEvent {
    data object InitMap : PlaceMapEvent

    data class InitMapManager(
        val initialMapSetting: InitialMapSettingUiModel,
    ) : PlaceMapEvent

    data class StartPlaceDetail(
        val placeDetail: LoadState.Success<PlaceDetailUiModel>,
    ) : PlaceMapEvent

    data class PreloadImages(
        val places: List<PlaceUiModel>,
    ) : PlaceMapEvent

    data class ShowErrorSnackBar(
        val error: LoadState.Error,
    ) : PlaceMapEvent

    data object BackToInitialPosition : PlaceMapEvent

    data class MenuItemReClicked(
        val isPreviewVisible: Boolean,
    ) : PlaceMapEvent

    data class SetMarkerByTimeTag(
        val placeGeographies: List<PlaceCoordinateUiModel>,
        val selectedTimeTag: LoadState<TimeTag>,
        val isInitial: Boolean,
    ) : PlaceMapEvent

    data class FilterMapByCategory(
        val selectedCategories: List<PlaceCategoryUiModel>,
    ) : PlaceMapEvent

    data class MapViewDrag(
        val isPreviewVisible: Boolean,
    ) : PlaceMapEvent

    data class SelectMarker(
        val placeDetail: LoadState<PlaceDetailUiModel>,
    ) : PlaceMapEvent

    data object UnselectMarker : PlaceMapEvent
}
