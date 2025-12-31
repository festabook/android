package com.daedan.festabook.presentation.placeMap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daedan.festabook.di.viewmodel.ViewModelKey
import com.daedan.festabook.domain.repository.PlaceDetailRepository
import com.daedan.festabook.domain.repository.PlaceListRepository
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.placeMap.intent.action.FilterAction
import com.daedan.festabook.presentation.placeMap.intent.action.FilterActionHandler
import com.daedan.festabook.presentation.placeMap.intent.action.MapEventAction
import com.daedan.festabook.presentation.placeMap.intent.action.MapEventActionHandler
import com.daedan.festabook.presentation.placeMap.intent.action.PlaceMapAction
import com.daedan.festabook.presentation.placeMap.intent.action.SelectAction
import com.daedan.festabook.presentation.placeMap.intent.action.SelectActionHandler
import com.daedan.festabook.presentation.placeMap.intent.event.MapControlEvent
import com.daedan.festabook.presentation.placeMap.intent.event.PlaceMapEvent
import com.daedan.festabook.presentation.placeMap.intent.state.ListLoadState
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import com.daedan.festabook.presentation.placeMap.intent.state.await
import com.daedan.festabook.presentation.placeMap.model.PlaceCoordinateUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.placeMap.model.toUiModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey(PlaceMapViewModel::class)
@Inject
class PlaceMapViewModel(
    private val placeListRepository: PlaceListRepository,
    private val placeDetailRepository: PlaceDetailRepository,
    private val logger: DefaultFirebaseLogger,
) : ViewModel() {
    private val cachedPlaces = MutableStateFlow(listOf<PlaceUiModel>())
    private val cachedPlaceByTimeTag = MutableStateFlow<List<PlaceUiModel>>(emptyList())

    private val _uiState = MutableStateFlow(PlaceMapUiState())
    val uiState: StateFlow<PlaceMapUiState> = _uiState.asStateFlow()

    private val _placeMapUiEvent = Channel<PlaceMapEvent>()
    val placeMapUiEvent: Flow<PlaceMapEvent> = _placeMapUiEvent.receiveAsFlow()

    private val _mapControlUiEvent = Channel<MapControlEvent>()
    val mapControlUiEvent: Flow<MapControlEvent> = _mapControlUiEvent.receiveAsFlow()

    private val mapEventActionHandler =
        MapEventActionHandler(
            _mapControlUiEvent = _mapControlUiEvent,
            _placeMapUiEvent = _placeMapUiEvent,
            uiState = uiState,
            logger = logger,
        )

    private val filterActionHandler =
        FilterActionHandler(
            _mapControlUiEvent = _mapControlUiEvent,
            logger = logger,
            uiState = uiState,
            cachedPlaces = cachedPlaces,
            cachedPlaceByTimeTag = cachedPlaceByTimeTag,
            onUpdateCachedPlace = { cachedPlaceByTimeTag.tryEmit(it) },
            onUpdateState = { _uiState.update(it) },
        )

    private val selectActionHandler =
        SelectActionHandler(
            filterActionHandler = filterActionHandler,
            _placeMapUiEvent = _placeMapUiEvent,
            _mapControlUiEvent = _mapControlUiEvent,
            uiState = uiState,
            logger = logger,
            placeDetailRepository = placeDetailRepository,
            scope = viewModelScope,
            onUpdateState = { _uiState.update(it) },
        )

    init {
        loadOrganizationGeography()
        loadTimeTags()
        loadAllPlaces()
        observeErrorEvent()
    }

    fun onPlaceMapAction(action: PlaceMapAction) {
        viewModelScope.launch {
            when (action) {
                is FilterAction -> filterActionHandler(action)
                is MapEventAction -> mapEventActionHandler(action)
                is SelectAction -> selectActionHandler(action)
            }
        }
    }

    fun onMenuItemReClicked() {
        _placeMapUiEvent.trySend(
            PlaceMapEvent.MenuItemReClicked(
                uiState.value.isPlacePreviewVisible || uiState.value.isPlaceSecondaryPreviewVisible,
            ),
        )
    }

    private fun loadTimeTags() {
        viewModelScope.launch {
            placeListRepository
                .getTimeTags()
                .onSuccess { timeTags ->
                    _uiState.update {
                        it.copy(
                            timeTags = LoadState.Success(timeTags),
                        )
                    }
                }.onFailure {
                    _uiState.update {
                        it.copy(
                            timeTags = LoadState.Empty,
                        )
                    }
                }

            // 기본 선택값
            val timeTags = uiState.value.timeTags
            val selectedTimeTag =
                if (timeTags is LoadState.Success && timeTags.value.isNotEmpty()) {
                    LoadState.Success(
                        timeTags.value.first(),
                    )
                } else {
                    LoadState.Empty
                }
            _uiState.update {
                it.copy(selectedTimeTag = selectedTimeTag)
            }

            val placeGeographies =
                uiState.await<LoadState.Success<List<PlaceCoordinateUiModel>>> { it.placeGeographies }
            _mapControlUiEvent.send(
                MapControlEvent.SetMarkerByTimeTag(
                    placeGeographies = placeGeographies.value,
                    selectedTimeTag = selectedTimeTag,
                    isInitial = true,
                ),
            )
        }
    }

    private fun loadOrganizationGeography() {
        viewModelScope.launch {
            placeListRepository.getOrganizationGeography().onSuccess { organizationGeography ->
                _uiState.update {
                    it.copy(initialMapSetting = LoadState.Success(organizationGeography.toUiModel()))
                }
            }

            launch {
                placeListRepository
                    .getPlaceGeographies()
                    .onSuccess { placeGeographies ->
                        _uiState.update {
                            it.copy(
                                placeGeographies = LoadState.Success(placeGeographies.map { it.toUiModel() }),
                            )
                        }
                    }.onFailure { item ->
                        _uiState.update {
                            it.copy(placeGeographies = LoadState.Error(item))
                        }
                    }
            }
        }
    }

    private fun loadAllPlaces() {
        viewModelScope.launch {
            val result = placeListRepository.getPlaces()
            result
                .onSuccess { places ->
                    val placeUiModels = places.map { it.toUiModel() }
                    cachedPlaces.tryEmit(placeUiModels)
                    _uiState.update { it.copy(places = ListLoadState.PlaceLoaded(placeUiModels)) }
                }.onFailure { error ->
                    _uiState.update { it.copy(places = ListLoadState.Error(error)) }
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeErrorEvent() {
        viewModelScope.launch {
            launch {
                uiState
                    .map { it.hasAnyError }
                    .distinctUntilChanged()
                    .filterIsInstance<LoadState.Error>()
                    .debounce(1000)
                    .collect {
                        _placeMapUiEvent.send(PlaceMapEvent.ShowErrorSnackBar(it))
                    }
            }
        }
    }
}
