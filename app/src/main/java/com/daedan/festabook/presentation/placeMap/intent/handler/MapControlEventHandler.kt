package com.daedan.festabook.presentation.placeMap.intent.handler

import com.daedan.festabook.di.placeMapHandler.PlaceMapViewModelScope
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.placeMap.intent.event.MapControlEvent
import com.daedan.festabook.presentation.placeMap.intent.sideEffect.MapControlSideEffect
import com.daedan.festabook.presentation.placeMap.intent.sideEffect.PlaceMapSideEffect
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import com.daedan.festabook.presentation.placeMap.intent.state.await
import com.daedan.festabook.presentation.placeMap.logging.PlaceBackToSchoolClick
import com.daedan.festabook.presentation.placeMap.model.InitialMapSettingUiModel
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

@Inject
@ContributesBinding(PlaceMapViewModelScope::class)
class MapControlEventHandler(
    override val uiState: StateFlow<PlaceMapUiState>,
    override val onUpdateState: ((before: PlaceMapUiState) -> PlaceMapUiState) -> Unit,
    private val _mapControlSideEffect: Channel<MapControlSideEffect>,
    private val _placeMapSideEffect: Channel<PlaceMapSideEffect>,
    private val logger: DefaultFirebaseLogger,
) : EventHandler<MapControlEvent, PlaceMapUiState> {
    override suspend operator fun invoke(event: MapControlEvent) {
        when (event) {
            is MapControlEvent.OnMapReady -> {
                _mapControlSideEffect.send(MapControlSideEffect.InitMap)
                val setting =
                    uiState.await<LoadState.Success<InitialMapSettingUiModel>> { it.initialMapSetting }
                _mapControlSideEffect.send(MapControlSideEffect.InitMapManager(setting.value))
            }

            is MapControlEvent.OnPlaceLoadFinish ->
                _placeMapSideEffect.send(
                    PlaceMapSideEffect.PreloadImages(
                        event.places,
                    ),
                )

            is MapControlEvent.OnBackToInitialPositionClick -> {
                logger.log(
                    PlaceBackToSchoolClick(
                        baseLogData = logger.getBaseLogData(),
                    ),
                )
                _mapControlSideEffect.send(MapControlSideEffect.BackToInitialPosition)
            }

            is MapControlEvent.OnMapDrag -> {
                _placeMapSideEffect.send(
                    PlaceMapSideEffect.MapViewDrag(
                        uiState.value.isPlacePreviewVisible || uiState.value.isPlaceSecondaryPreviewVisible,
                    ),
                )
            }
        }
    }
}
