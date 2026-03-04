package com.daedan.festabook.presentation.placeMap.intent.handler

import com.daedan.festabook.presentation.placeMap.intent.sideEffect.MapControlSideEffect
import com.daedan.festabook.presentation.placeMap.intent.sideEffect.PlaceMapSideEffect
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

data class EventHandlerContext(
    val uiState: StateFlow<PlaceMapUiState>,
    val onUpdateState: ((PlaceMapUiState) -> PlaceMapUiState) -> Unit,
    val mapControlSideEffect: Channel<MapControlSideEffect>,
    val scope: CoroutineScope,
    val cachedPlaces: StateFlow<List<PlaceUiModel>>,
    val cachedPlaceByTimeTag: StateFlow<List<PlaceUiModel>>,
    val onUpdateCachedPlace: (List<PlaceUiModel>) -> Unit,
    val placeMapSideEffect: Channel<PlaceMapSideEffect>,
)
