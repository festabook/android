package com.daedan.festabook.presentation.placeMap.intent.event

import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.model.InitialMapSettingUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCoordinateUiModel

sealed interface MapControlSideEffect {
    data object InitMap : MapControlSideEffect

    data class InitMapManager(
        val initialMapSetting: InitialMapSettingUiModel,
    ) : MapControlSideEffect

    data object BackToInitialPosition : MapControlSideEffect

    data class SetMarkerByTimeTag(
        val placeGeographies: List<PlaceCoordinateUiModel>,
        val selectedTimeTag: LoadState<TimeTag>,
        val isInitial: Boolean,
    ) : MapControlSideEffect

    data class FilterMapByCategory(
        val selectedCategories: List<PlaceCategoryUiModel>,
    ) : MapControlSideEffect

    data class SelectMarker(
        val placeDetail: LoadState<PlaceDetailUiModel>,
    ) : MapControlSideEffect

    data object UnselectMarker : MapControlSideEffect
}
