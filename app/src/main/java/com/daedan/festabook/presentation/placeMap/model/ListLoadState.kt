package com.daedan.festabook.presentation.placeMap.model

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
