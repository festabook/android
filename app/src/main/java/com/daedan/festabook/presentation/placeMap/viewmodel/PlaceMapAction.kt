package com.daedan.festabook.presentation.placeMap.viewmodel

import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.model.LoadState
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel

sealed interface PlaceMapAction {
    data object OnMapReady : PlaceMapAction

    data class OnTimeTagClick(
        val timeTag: TimeTag,
    ) : PlaceMapAction

    data object OnMapDrag : PlaceMapAction

    data class OnPlaceClick(
        val placeId: Long,
    ) : PlaceMapAction

    data class OnPlacePreviewClick(
        val place: LoadState<PlaceDetailUiModel>,
    ) : PlaceMapAction

    data object OnBackPress : PlaceMapAction

    data class OnPlaceLoadFinish(
        val places: List<PlaceUiModel>,
    ) : PlaceMapAction

    data object OnBackToInitialPositionClick : PlaceMapAction

    data class OnCategoryClick(
        val categories: Set<PlaceCategoryUiModel>,
    ) : PlaceMapAction

    data object OnPlaceLoad : PlaceMapAction

    data class ExceededMaxLength(
        val isExceededMaxLength: Boolean,
    ) : PlaceMapAction

    data object UnSelectPlace : PlaceMapAction
}
