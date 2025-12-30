package com.daedan.festabook.presentation.placeMap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daedan.festabook.di.viewmodel.ViewModelKey
import com.daedan.festabook.domain.model.PlaceCategory
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.domain.repository.PlaceDetailRepository
import com.daedan.festabook.domain.repository.PlaceListRepository
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.placeDetail.model.toUiModel
import com.daedan.festabook.presentation.placeMap.logging.PlaceBackToSchoolClick
import com.daedan.festabook.presentation.placeMap.logging.PlaceCategoryClick
import com.daedan.festabook.presentation.placeMap.logging.PlaceItemClick
import com.daedan.festabook.presentation.placeMap.logging.PlacePreviewClick
import com.daedan.festabook.presentation.placeMap.logging.PlaceTimeTagSelected
import com.daedan.festabook.presentation.placeMap.model.InitialMapSettingUiModel
import com.daedan.festabook.presentation.placeMap.model.ListLoadState
import com.daedan.festabook.presentation.placeMap.model.LoadState
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
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
import kotlinx.coroutines.flow.first
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
    private var cachedPlaces = listOf<PlaceUiModel>()
    private var cachedPlaceByTimeTag: List<PlaceUiModel> = emptyList()

    private val _uiState = MutableStateFlow(PlaceMapUiState())
    val uiState: StateFlow<PlaceMapUiState> = _uiState.asStateFlow()

    private val _placeMapUiEvent = Channel<PlaceMapEvent>()
    val placeMapUiEvent: Flow<PlaceMapEvent> = _placeMapUiEvent.receiveAsFlow()

    private val _mapControlUiEvent = Channel<MapControlEvent>()
    val mapControlUiEvent: Flow<MapControlEvent> = _mapControlUiEvent.receiveAsFlow()

    init {
        loadOrganizationGeography()
        loadTimeTags()
        loadAllPlaces()
        waitEvent()
    }

    fun onPlaceMapAction(action: PlaceMapAction) {
        viewModelScope.launch {
            when (action) {
                is PlaceMapAction.OnMapReady -> {
                    _mapControlUiEvent.send(MapControlEvent.InitMap)
                    val setting =
                        uiState.await<LoadState.Success<InitialMapSettingUiModel>> { it.initialMapSetting }
                    _mapControlUiEvent.send(MapControlEvent.InitMapManager(setting.value))
                }

                is PlaceMapAction.OnTimeTagClick -> {
                    onDaySelected(action.timeTag)
                    logger.log(
                        PlaceTimeTagSelected(
                            baseLogData = logger.getBaseLogData(),
                            timeTagName = action.timeTag.name,
                        ),
                    )
                }

                is PlaceMapAction.OnPlaceClick -> {
                    selectPlace(action.placeId)
                }

                is PlaceMapAction.OnPlaceLoad -> {
                    val selectedTimeTag =
                        uiState
                            .map { it.selectedTimeTag }
                            .distinctUntilChanged()
                            .first()

                    when (selectedTimeTag) {
                        is LoadState.Success -> {
                            updatePlacesByTimeTag(selectedTimeTag.value.timeTagId)
                        }

                        is LoadState.Empty -> {
                            updatePlacesByTimeTag(TimeTag.EMTPY_TIME_TAG_ID)
                        }

                        else -> Unit
                    }
                }

                is PlaceMapAction.OnPlaceLoadFinish ->
                    _placeMapUiEvent.send(
                        PlaceMapEvent.PreloadImages(
                            action.places,
                        ),
                    )

                is PlaceMapAction.OnBackToInitialPositionClick -> {
                    logger.log(
                        PlaceBackToSchoolClick(
                            baseLogData = logger.getBaseLogData(),
                        ),
                    )
                    _mapControlUiEvent.send(MapControlEvent.BackToInitialPosition)
                }

                is PlaceMapAction.OnCategoryClick -> {
                    uiState.await<ListLoadState.Success<PlaceUiModel>> { it.places }
                    unselectPlace()
                    updatePlacesByCategories(action.categories.toList())

                    _uiState.update {
                        it.copy(selectedCategories = action.categories)
                    }

                    _mapControlUiEvent.send(MapControlEvent.FilterMapByCategory(action.categories.toList()))

                    logger.log(
                        PlaceCategoryClick(
                            baseLogData = logger.getBaseLogData(),
                            currentCategories = action.categories.joinToString(",") { it.toString() },
                        ),
                    )
                }

                is PlaceMapAction.OnMapDrag -> {
                    _placeMapUiEvent.send(
                        PlaceMapEvent.MapViewDrag(
                            uiState.value.isPlacePreviewVisible || uiState.value.isPlaceSecondaryPreviewVisible,
                        ),
                    )
                }

                is PlaceMapAction.OnBackPress -> {
                    unselectPlace()
                }

                is PlaceMapAction.OnPlacePreviewClick -> {
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

                is PlaceMapAction.ExceededMaxLength -> {
                    _uiState.update {
                        it.copy(
                            isExceededMaxLength = action.isExceededMaxLength,
                        )
                    }
                }

                is PlaceMapAction.UnSelectPlace -> {
                    unselectPlace()
                }
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

    private fun onDaySelected(item: TimeTag) {
        unselectPlace()
        _uiState.update {
            it.copy(selectedTimeTag = LoadState.Success(item))
        }
        viewModelScope.launch {
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

    private fun selectPlace(placeId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedPlace = LoadState.Loading) }
            placeDetailRepository
                .getPlaceDetail(placeId = placeId)
                .onSuccess { item ->
                    _uiState.update {
                        it.copy(selectedPlace = LoadState.Success(item.toUiModel()))
                    }
                    _mapControlUiEvent.send(MapControlEvent.SelectMarker(uiState.value.selectedPlace))
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
                    _uiState.update { it.copy(selectedPlace = LoadState.Error(item)) }
                }
        }
    }

    private fun unselectPlace() {
        _uiState.update { it.copy(selectedPlace = LoadState.Empty) }
        _mapControlUiEvent.trySend(MapControlEvent.UnselectMarker)
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

    private fun updatePlacesByCategories(category: List<PlaceCategoryUiModel>) {
        if (category.isEmpty()) {
            clearPlacesFilter()
            return
        }

        val secondaryCategories =
            PlaceCategory.SECONDARY_CATEGORIES.map {
                it.toUiModel()
            }
        val primaryCategoriesSelected = category.any { it !in secondaryCategories }

        if (!primaryCategoriesSelected) {
            clearPlacesFilter()
            return
        }

        val filteredPlaces =
            cachedPlaceByTimeTag
                .filter { place ->
                    place.category in category
                }
        _uiState.update { it.copy(places = ListLoadState.Success(filteredPlaces)) }
    }

    private fun filterPlacesByTimeTag(timeTagId: Long): List<PlaceUiModel> {
        val filteredPlaces =
            cachedPlaces.filter { place ->
                place.timeTagId.contains(timeTagId)
            }
        return filteredPlaces
    }

    private fun updatePlacesByTimeTag(timeTagId: Long) {
        val filteredPlaces =
            if (timeTagId == TimeTag.EMTPY_TIME_TAG_ID) {
                cachedPlaces
            } else {
                filterPlacesByTimeTag(timeTagId)
            }

        _uiState.update { it.copy(places = ListLoadState.Success(filteredPlaces)) }
        cachedPlaceByTimeTag = filteredPlaces
    }

    private fun clearPlacesFilter() {
        _uiState.update { it.copy(places = ListLoadState.Success(cachedPlaceByTimeTag)) }
    }

    private fun loadAllPlaces() {
        viewModelScope.launch {
            val result = placeListRepository.getPlaces()
            result
                .onSuccess { places ->
                    val placeUiModels = places.map { it.toUiModel() }
                    cachedPlaces = placeUiModels
                    _uiState.update { it.copy(places = ListLoadState.PlaceLoaded(placeUiModels)) }
                }.onFailure { error ->
                    _uiState.update { it.copy(places = ListLoadState.Error(error)) }
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun waitEvent() {
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
