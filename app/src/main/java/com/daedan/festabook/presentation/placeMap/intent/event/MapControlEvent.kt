package com.daedan.festabook.presentation.placeMap.intent.event

import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.model.InitialMapSettingUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCoordinateUiModel

sealed interface MapControlEvent {
    data object InitMap : MapControlEvent

    data class InitMapManager(
        val initialMapSetting: InitialMapSettingUiModel,
    ) : MapControlEvent

    data object BackToInitialPosition : MapControlEvent

    data class SetMarkerByTimeTag(
        val placeGeographies: List<PlaceCoordinateUiModel>,
        val selectedTimeTag: LoadState<TimeTag>,
        val isInitial: Boolean,
    ) : MapControlEvent

    data class FilterMapByCategory(
        val selectedCategories: List<PlaceCategoryUiModel>,
    ) : MapControlEvent

    data class SelectMarker(
        val placeDetail: LoadState<PlaceDetailUiModel>,
    ) : MapControlEvent

    data object UnselectMarker : MapControlEvent
}
