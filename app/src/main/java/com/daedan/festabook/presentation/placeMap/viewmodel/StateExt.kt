package com.daedan.festabook.presentation.placeMap.viewmodel

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

suspend inline fun <reified R> StateFlow<PlaceMapUiState>.await(crossinline selector: (PlaceMapUiState) -> Any?): R =
    this
        .map { selector(it) }
        .distinctUntilChanged()
        .filterIsInstance<R>()
        .first()
