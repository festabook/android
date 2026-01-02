package com.daedan.festabook.presentation.placeMap.intent.handler

import com.daedan.festabook.di.placeMapHandler.PlaceMapViewModelScope
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.domain.repository.PlaceDetailRepository
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.placeDetail.model.toUiModel
import com.daedan.festabook.presentation.placeMap.intent.action.SelectAction
import com.daedan.festabook.presentation.placeMap.intent.event.MapControlEvent
import com.daedan.festabook.presentation.placeMap.intent.event.PlaceMapEvent
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import com.daedan.festabook.presentation.placeMap.intent.state.await
import com.daedan.festabook.presentation.placeMap.logging.PlaceItemClick
import com.daedan.festabook.presentation.placeMap.logging.PlacePreviewClick
import com.daedan.festabook.presentation.placeMap.logging.PlaceTimeTagSelected
import com.daedan.festabook.presentation.placeMap.model.PlaceCoordinateUiModel
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Inject
@ContributesBinding(PlaceMapViewModelScope::class)
class SelectActionHandler(
    override val uiState: StateFlow<PlaceMapUiState>,
    override val onUpdateState: ((PlaceMapUiState) -> PlaceMapUiState) -> Unit,
    private val filterActionHandler: FilterActionHandler,
    private val _placeMapUiEvent: Channel<PlaceMapEvent>,
    private val _mapControlUiEvent: Channel<MapControlEvent>,
    private val logger: DefaultFirebaseLogger,
    private val placeDetailRepository: PlaceDetailRepository,
    private val scope: CoroutineScope,
) : ActionHandler<SelectAction, PlaceMapUiState> {
    override suspend operator fun invoke(action: SelectAction) {
        when (action) {
            is SelectAction.OnPlaceClick -> {
                selectPlace(action.placeId)
            }

            is SelectAction.UnSelectPlace -> {
                unselectPlace()
            }

            is SelectAction.ExceededMaxLength -> {
                onUpdateState.invoke {
                    it.copy(
                        isExceededMaxLength = action.isExceededMaxLength,
                    )
                }
            }

            is SelectAction.OnTimeTagClick -> {
                onDaySelected(action.timeTag)
                filterActionHandler.updatePlacesByTimeTag(action.timeTag.timeTagId)
                logger.log(
                    PlaceTimeTagSelected(
                        baseLogData = logger.getBaseLogData(),
                        timeTagName = action.timeTag.name,
                    ),
                )
            }

            is SelectAction.OnPlacePreviewClick -> {
                val selectedTimeTag = uiState.value.selectedTimeTag
                val selectedPlace = action.place
                if (selectedPlace is LoadState.Success &&
                    selectedTimeTag is LoadState.Success
                ) {
                    _placeMapUiEvent.send(PlaceMapEvent.StartPlaceDetail(action.place))
                    logger.log(
                        PlacePreviewClick(
                            baseLogData = logger.getBaseLogData(),
                            placeName =
                                selectedPlace.value.place.title
                                    ?: "undefined",
                            timeTag = selectedTimeTag.value.name,
                            category = selectedPlace.value.place.category.name,
                        ),
                    )
                }
            }

            is SelectAction.OnBackPress -> {
                unselectPlace()
            }
        }
    }

    private fun selectPlace(placeId: Long) {
        scope.launch {
            onUpdateState.invoke { it.copy(selectedPlace = LoadState.Loading) }
            placeDetailRepository
                .getPlaceDetail(placeId = placeId)
                .onSuccess { item ->
                    val newSelectedPlace = LoadState.Success(item.toUiModel())

                    onUpdateState.invoke {
                        it.copy(selectedPlace = newSelectedPlace)
                    }
                    _mapControlUiEvent.send(MapControlEvent.SelectMarker(newSelectedPlace))
                    val selectedTimeTag = uiState.value.selectedTimeTag
                    val timeTagName =
                        if (selectedTimeTag is LoadState.Success) selectedTimeTag.value.name else "undefined"
                    logger.log(
                        PlaceItemClick(
                            baseLogData = logger.getBaseLogData(),
                            placeId = placeId,
                            timeTagName = timeTagName,
                            category = item.place.category.name,
                        ),
                    )
                }.onFailure { item ->
                    onUpdateState.invoke {
                        it.copy(selectedPlace = LoadState.Error(item))
                    }
                }
        }
    }

    private fun unselectPlace() {
        onUpdateState.invoke { it.copy(selectedPlace = LoadState.Empty) }
        _mapControlUiEvent.trySend(MapControlEvent.UnselectMarker)
    }

    private fun onDaySelected(item: TimeTag) {
        unselectPlace()
        onUpdateState.invoke {
            it.copy(selectedTimeTag = LoadState.Success(item))
        }
        scope.launch {
            val placeGeographies =
                uiState.await<LoadState.Success<List<PlaceCoordinateUiModel>>> { it.placeGeographies }
            _mapControlUiEvent.send(
                MapControlEvent.SetMarkerByTimeTag(
                    placeGeographies = placeGeographies.value,
                    selectedTimeTag = LoadState.Success(item),
                    isInitial = false,
                ),
            )
        }
    }
}
