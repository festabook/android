package com.daedan.festabook.presentation.placeMap.intent.state

import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel

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

val LoadState.Success<PlaceDetailUiModel>.isSecondary get() = value.place.category in PlaceCategoryUiModel.Companion.SECONDARY_CATEGORIES
