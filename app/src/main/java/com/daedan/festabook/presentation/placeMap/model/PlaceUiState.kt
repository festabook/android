package com.daedan.festabook.presentation.placeMap.model

import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel

sealed interface PlaceUiState<out T> {
    data object Loading : PlaceUiState<Nothing>

    data object Empty : PlaceUiState<Nothing>

    data class Success<out T>(
        val value: T,
    ) : PlaceUiState<T>

    data class Error(
        val throwable: Throwable,
    ) : PlaceUiState<Nothing>
}

val PlaceUiState.Success<PlaceDetailUiModel>.isSecondary get() = value.place.category in PlaceCategoryUiModel.SECONDARY_CATEGORIES
