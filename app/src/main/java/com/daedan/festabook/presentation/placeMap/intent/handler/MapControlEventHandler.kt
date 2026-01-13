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
import kotlinx.coroutines.flow.StateFlow

@Inject
@ContributesBinding(PlaceMapViewModelScope::class)
class MapControlEventHandler(
    private val context: EventHandlerContext,
    private val logger: DefaultFirebaseLogger,
) : EventHandler<MapControlEvent, PlaceMapUiState> {
    override val uiState: StateFlow<PlaceMapUiState> = context.uiState
    override val onUpdateState = context.onUpdateState

    override suspend operator fun invoke(event: MapControlEvent) {
        when (event) {
            is MapControlEvent.OnMapReady -> {
                context.mapControlSideEffect.send(MapControlSideEffect.InitMap)
                val setting =
                    uiState.await<LoadState.Success<InitialMapSettingUiModel>> { it.initialMapSetting }
                context.mapControlSideEffect.send(MapControlSideEffect.InitMapManager(setting.value))
            }

            is MapControlEvent.OnPlaceLoadFinish ->
                context.placeMapSideEffect.send(
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
                context.mapControlSideEffect.send(MapControlSideEffect.BackToInitialPosition)
            }

            is MapControlEvent.OnMapDrag -> {
                context.placeMapSideEffect.send(
                    PlaceMapSideEffect.MapViewDrag(
                        uiState.value.isPlacePreviewVisible || uiState.value.isPlaceSecondaryPreviewVisible,
                    ),
                )
            }
        }
    }
}
