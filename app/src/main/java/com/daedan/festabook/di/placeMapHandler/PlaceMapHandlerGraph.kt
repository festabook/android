package com.daedan.festabook.di.placeMapHandler

import com.daedan.festabook.presentation.placeMap.intent.handler.FilterEventHandler
import com.daedan.festabook.presentation.placeMap.intent.handler.MapControlEventHandler
import com.daedan.festabook.presentation.placeMap.intent.handler.SelectEventHandler
import com.daedan.festabook.presentation.placeMap.intent.sideEffect.MapControlSideEffect
import com.daedan.festabook.presentation.placeMap.intent.sideEffect.PlaceMapSideEffect
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

@GraphExtension(PlaceMapViewModelScope::class)
interface PlaceMapHandlerGraph {
    val filterEventHandler: FilterEventHandler
    val selectEventHandler: SelectEventHandler
    val mapControlEventHandler: MapControlEventHandler

    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    interface Factory {
        fun create(
            @Provides mapControlSideEffect: Channel<MapControlSideEffect>,
            @Provides placeMapSideEffect: Channel<PlaceMapSideEffect>,
            @Provides uiState: StateFlow<PlaceMapUiState>,
            @Provides @CachedPlaces cachedPlaces: StateFlow<List<PlaceUiModel>>,
            @Provides @CachedPlaceByTimeTag cachedPlaceByTimeTag: StateFlow<List<PlaceUiModel>>,
            @Provides onUpdateCachedPlace: (List<PlaceUiModel>) -> Unit,
            @Provides onUpdateState: ((PlaceMapUiState) -> PlaceMapUiState) -> Unit,
            @Provides scope: CoroutineScope,
        ): PlaceMapHandlerGraph
    }
}
