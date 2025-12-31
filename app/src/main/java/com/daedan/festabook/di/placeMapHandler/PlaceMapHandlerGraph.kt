package com.daedan.festabook.di.placeMapHandler

import com.daedan.festabook.presentation.placeMap.intent.action.FilterActionHandler
import com.daedan.festabook.presentation.placeMap.intent.action.MapEventActionHandler
import com.daedan.festabook.presentation.placeMap.intent.action.SelectActionHandler
import com.daedan.festabook.presentation.placeMap.intent.event.MapControlEvent
import com.daedan.festabook.presentation.placeMap.intent.event.PlaceMapEvent
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
    val filterActionHandler: FilterActionHandler
    val selectActionHandler: SelectActionHandler
    val mapEventActionHandler: MapEventActionHandler

    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    interface Factory {
        fun create(
            @Provides mapControlUiEvent: Channel<MapControlEvent>,
            @Provides placeMapUiEvent: Channel<PlaceMapEvent>,
            @Provides uiState: StateFlow<PlaceMapUiState>,
            @Provides @CachedPlaces cachedPlaces: StateFlow<List<PlaceUiModel>>,
            @Provides @CachedPlaceByTimeTag cachedPlaceByTimeTag: StateFlow<List<PlaceUiModel>>,
            @Provides onUpdateCachedPlace: (List<PlaceUiModel>) -> Unit,
            @Provides onUpdateState: ((PlaceMapUiState) -> PlaceMapUiState) -> Unit,
            @Provides scope: CoroutineScope,
        ): PlaceMapHandlerGraph
    }
}
