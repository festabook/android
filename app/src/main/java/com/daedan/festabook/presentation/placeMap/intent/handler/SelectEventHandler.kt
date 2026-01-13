package com.daedan.festabook.presentation.placeMap.intent.handler

import com.daedan.festabook.di.placeMapHandler.PlaceMapViewModelScope
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.domain.repository.PlaceDetailRepository
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.placeDetail.model.toUiModel
import com.daedan.festabook.presentation.placeMap.intent.event.SelectEvent
import com.daedan.festabook.presentation.placeMap.intent.sideEffect.MapControlSideEffect
import com.daedan.festabook.presentation.placeMap.intent.sideEffect.PlaceMapSideEffect
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
class SelectEventHandler(
    override val uiState: StateFlow<PlaceMapUiState>,
    override val onUpdateState: ((PlaceMapUiState) -> PlaceMapUiState) -> Unit,
    private val filterActionHandler: FilterEventHandler,
    private val _placeMapSideEffect: Channel<PlaceMapSideEffect>,
    private val _mapControlSideEffect: Channel<MapControlSideEffect>,
    private val logger: DefaultFirebaseLogger,
    private val placeDetailRepository: PlaceDetailRepository,
    private val scope: CoroutineScope,
) : EventHandler<SelectEvent, PlaceMapUiState> {
    override suspend operator fun invoke(event: SelectEvent) {
        when (event) {
            is SelectEvent.OnPlaceClick -> {
                selectPlace(event.placeId)
            }

            is SelectEvent.UnSelectPlace -> {
                unselectPlace()
            }

            is SelectEvent.ExceededMaxLength -> {
                onUpdateState.invoke {
                    it.copy(
                        isExceededMaxLength = event.isExceededMaxLength,
                    )
                }
            }

            is SelectEvent.OnTimeTagClick -> {
                onDaySelected(event.timeTag)
                filterActionHandler.updatePlacesByTimeTag(event.timeTag.timeTagId)
                logger.log(
                    PlaceTimeTagSelected(
                        baseLogData = logger.getBaseLogData(),
                        timeTagName = event.timeTag.name,
                    ),
                )
            }

            is SelectEvent.OnPlacePreviewClick -> {
                val selectedTimeTag = uiState.value.selectedTimeTag
                val selectedPlace = event.place
                if (selectedPlace is LoadState.Success &&
                    selectedTimeTag is LoadState.Success
                ) {
                    _placeMapSideEffect.send(PlaceMapSideEffect.StartPlaceDetail(event.place))
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

            is SelectEvent.OnBackPress -> {
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
                    _mapControlSideEffect.send(MapControlSideEffect.SelectMarker(newSelectedPlace))
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
        _mapControlSideEffect.trySend(MapControlSideEffect.UnselectMarker)
    }

    private fun onDaySelected(item: TimeTag) {
        unselectPlace()
        onUpdateState.invoke {
            it.copy(selectedTimeTag = LoadState.Success(item))
        }
        scope.launch {
            val placeGeographies =
                uiState.await<LoadState.Success<List<PlaceCoordinateUiModel>>> { it.placeGeographies }
            _mapControlSideEffect.send(
                MapControlSideEffect.SetMarkerByTimeTag(
                    placeGeographies = placeGeographies.value,
                    selectedTimeTag = LoadState.Success(item),
                    isInitial = false,
                ),
            )
        }
    }
}
