package com.daedan.festabook.presentation.placeMap.intent.state

import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.model.InitialMapSettingUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCoordinateUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel

data class PlaceMapUiState(
    val initialMapSetting: LoadState<InitialMapSettingUiModel> = LoadState.Loading,
    val placeGeographies: LoadState<List<PlaceCoordinateUiModel>> = LoadState.Loading,
    val timeTags: LoadState<List<TimeTag>> = LoadState.Empty,
    val selectedTimeTag: LoadState<TimeTag> = LoadState.Empty,
    val selectedPlace: LoadState<PlaceDetailUiModel> = LoadState.Empty,
    val places: ListLoadState<List<PlaceUiModel>> = ListLoadState.Loading(),
    val isExceededMaxLength: Boolean = false,
    val selectedCategories: Set<PlaceCategoryUiModel> = emptySet(),
    val initialCategories: List<PlaceCategoryUiModel> = PlaceCategoryUiModel.entries,
) {
    val isPlacePreviewVisible: Boolean =
        (selectedPlace is LoadState.Success && !selectedPlace.isSecondary)

    val isPlaceSecondaryPreviewVisible: Boolean =
        (selectedPlace is LoadState.Success && selectedPlace.isSecondary)

    val hasAnyError: LoadState<*>?
        get() =
            listOf(
                initialMapSetting,
                placeGeographies,
                timeTags,
                selectedTimeTag,
                selectedPlace,
                if (places is ListLoadState.Error) LoadState.Error(places.throwable) else LoadState.Empty,
            ).filterIsInstance<LoadState.Error>()
                .firstOrNull()
}
