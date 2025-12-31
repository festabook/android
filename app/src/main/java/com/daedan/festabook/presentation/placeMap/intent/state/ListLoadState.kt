package com.daedan.festabook.presentation.placeMap.intent.state

import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel

sealed interface ListLoadState<T> {
    class Loading<T> : ListLoadState<T>

    data class Success<T>(
        val value: T,
    ) : ListLoadState<T>

    data class PlaceLoaded(
        val value: List<PlaceUiModel>,
    ) : ListLoadState<List<PlaceUiModel>>

    data class Error<T>(
        val throwable: Throwable,
    ) : ListLoadState<T>
}
