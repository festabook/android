package com.daedan.festabook.presentation.placeMap.intent.action

import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.placeMap.intent.event.MapControlEvent
import com.daedan.festabook.presentation.placeMap.intent.event.PlaceMapEvent
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import com.daedan.festabook.presentation.placeMap.intent.state.await
import com.daedan.festabook.presentation.placeMap.logging.PlaceBackToSchoolClick
import com.daedan.festabook.presentation.placeMap.model.InitialMapSettingUiModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

class MapEventActionHandler(
    private val _mapControlUiEvent: Channel<MapControlEvent>,
    private val _placeMapUiEvent: Channel<PlaceMapEvent>,
    private val uiState: StateFlow<PlaceMapUiState>,
    private val logger: DefaultFirebaseLogger,
) {
    suspend operator fun invoke(action: MapEventAction) {
        when (action) {
            is MapEventAction.OnMapReady -> {
                _mapControlUiEvent.send(MapControlEvent.InitMap)
                val setting =
                    uiState.await<LoadState.Success<InitialMapSettingUiModel>> { it.initialMapSetting }
                _mapControlUiEvent.send(MapControlEvent.InitMapManager(setting.value))
            }

            is MapEventAction.OnPlaceLoadFinish ->
                _placeMapUiEvent.send(
                    PlaceMapEvent.PreloadImages(
                        action.places,
                    ),
                )

            is MapEventAction.OnBackToInitialPositionClick -> {
                logger.log(
                    PlaceBackToSchoolClick(
                        baseLogData = logger.getBaseLogData(),
                    ),
                )
                _mapControlUiEvent.send(MapControlEvent.BackToInitialPosition)
            }

            is MapEventAction.OnMapDrag -> {
                _placeMapUiEvent.send(
                    PlaceMapEvent.MapViewDrag(
                        uiState.value.isPlacePreviewVisible || uiState.value.isPlaceSecondaryPreviewVisible,
                    ),
                )
            }
        }
    }
}
