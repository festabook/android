package com.daedan.festabook.presentation.placeMap.model

import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>

    data object Empty : LoadState<Nothing>

    data class Success<out T>(
        val value: T,
    ) : LoadState<T>

    data class Error(
        val throwable: Throwable,
    ) : LoadState<Nothing>
}

val LoadState.Success<PlaceDetailUiModel>.isSecondary get() = value.place.category in PlaceCategoryUiModel.SECONDARY_CATEGORIES
