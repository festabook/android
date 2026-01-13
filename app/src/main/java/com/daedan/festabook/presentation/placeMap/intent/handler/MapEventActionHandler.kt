package com.daedan.festabook.presentation.placeMap.intent.handler

import com.daedan.festabook.di.placeMapHandler.PlaceMapViewModelScope
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.placeMap.intent.action.MapEventAction
import com.daedan.festabook.presentation.placeMap.intent.event.MapControlSideEffect
import com.daedan.festabook.presentation.placeMap.intent.event.PlaceMapSideEffect
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
class MapEventActionHandler(
    override val uiState: StateFlow<PlaceMapUiState>,
    override val onUpdateState: ((before: PlaceMapUiState) -> PlaceMapUiState) -> Unit,
    private val _mapControlSideEffect: Channel<MapControlSideEffect>,
    private val _placeMapSideEffect: Channel<PlaceMapSideEffect>,
    private val logger: DefaultFirebaseLogger,
) : ActionHandler<MapEventAction, PlaceMapUiState> {
    override suspend operator fun invoke(action: MapEventAction) {
        when (action) {
            is MapEventAction.OnMapReady -> {
                _mapControlSideEffect.send(MapControlSideEffect.InitMap)
                val setting =
                    uiState.await<LoadState.Success<InitialMapSettingUiModel>> { it.initialMapSetting }
                _mapControlSideEffect.send(MapControlSideEffect.InitMapManager(setting.value))
            }

            is MapEventAction.OnPlaceLoadFinish ->
                _placeMapSideEffect.send(
                    PlaceMapSideEffect.PreloadImages(
                        action.places,
                    ),
                )

            is MapEventAction.OnBackToInitialPositionClick -> {
                logger.log(
                    PlaceBackToSchoolClick(
                        baseLogData = logger.getBaseLogData(),
                    ),
                )
                _mapControlSideEffect.send(MapControlSideEffect.BackToInitialPosition)
            }

            is MapEventAction.OnMapDrag -> {
                _placeMapSideEffect.send(
                    PlaceMapSideEffect.MapViewDrag(
                        uiState.value.isPlacePreviewVisible || uiState.value.isPlaceSecondaryPreviewVisible,
                    ),
                )
            }
        }
    }
}
