package com.daedan.festabook.presentation.placeMap.intent.state

import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel

sealed interface ListLoadState<out T> {
    data object Loading : ListLoadState<Nothing>

    data class Success<T>(
        val value: T,
    ) : ListLoadState<T>

    data class PlaceLoaded(
        val value: List<PlaceUiModel>,
    ) : ListLoadState<List<PlaceUiModel>>

    data class Error(
        val throwable: Throwable,
    ) : ListLoadState<Nothing>
}
