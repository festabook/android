package com.daedan.festabook.presentation.placeMap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daedan.festabook.di.viewmodel.ViewModelKey
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.domain.repository.PlaceDetailRepository
import com.daedan.festabook.domain.repository.PlaceListRepository
import com.daedan.festabook.presentation.common.Event
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeDetail.model.toUiModel
import com.daedan.festabook.presentation.placeMap.model.InitialMapSettingUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCoordinateUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiState
import com.daedan.festabook.presentation.placeMap.model.toUiModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey(PlaceMapViewModel::class)
@Inject
class PlaceMapViewModel(
    private val placeListRepository: PlaceListRepository,
    private val placeDetailRepository: PlaceDetailRepository,
) : ViewModel() {
    private val _initialMapSetting: MutableStateFlow<PlaceUiState<InitialMapSettingUiModel>> =
        MutableStateFlow(PlaceUiState.Loading)
    val initialMapSetting: StateFlow<PlaceUiState<InitialMapSettingUiModel>> =
        _initialMapSetting.asStateFlow()

    private val _placeGeographies: MutableStateFlow<PlaceUiState<List<PlaceCoordinateUiModel>>> =
        MutableStateFlow(PlaceUiState.Loading)
    val placeGeographies: StateFlow<PlaceUiState<List<PlaceCoordinateUiModel>>> =
        _placeGeographies.asStateFlow()

    private val _timeTags = MutableStateFlow<PlaceUiState<List<TimeTag>>>(PlaceUiState.Empty)
    val timeTags: StateFlow<PlaceUiState<List<TimeTag>>> = _timeTags.asStateFlow()

    private val _selectedTimeTag = MutableStateFlow<PlaceUiState<TimeTag>>(PlaceUiState.Empty)
    val selectedTimeTag: StateFlow<PlaceUiState<TimeTag>> = _selectedTimeTag.asStateFlow()

    private val _selectedPlace: MutableStateFlow<PlaceUiState<PlaceDetailUiModel>> =
        MutableStateFlow(PlaceUiState.Loading)
    val selectedPlace: StateFlow<PlaceUiState<PlaceDetailUiModel>> = _selectedPlace.asStateFlow()

    private val _navigateToDetail =
        MutableSharedFlow<PlaceDetailUiModel>(
            extraBufferCapacity = 1,
        )
    val navigateToDetail: SharedFlow<PlaceDetailUiModel> = _navigateToDetail.asSharedFlow()

    private val _isExceededMaxLength: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isExceededMaxLength: StateFlow<Boolean> = _isExceededMaxLength.asStateFlow()

    private val _backToInitialPositionClicked: MutableSharedFlow<Event<Unit>> =
        MutableSharedFlow(
            extraBufferCapacity = 1,
        )
    val backToInitialPositionClicked: SharedFlow<Event<Unit>> =
        _backToInitialPositionClicked.asSharedFlow()

    private val _selectedCategories: MutableStateFlow<List<PlaceCategoryUiModel>> =
        MutableStateFlow(
            PlaceCategoryUiModel.entries,
        )
    val selectedCategories: StateFlow<List<PlaceCategoryUiModel>> =
        _selectedCategories.asStateFlow()

    private val _onMapViewClick: MutableSharedFlow<Event<Unit>> =
        MutableSharedFlow(
            extraBufferCapacity = 1,
        )
    val onMapViewClick: SharedFlow<Event<Unit>> = _onMapViewClick.asSharedFlow()

    private val _onMenuItemReClick: MutableSharedFlow<Event<Unit>> =
        MutableSharedFlow(
            extraBufferCapacity = 1,
        )

    val onMenuItemReClick: SharedFlow<Event<Unit>> = _onMenuItemReClick.asSharedFlow()

    init {
        loadOrganizationGeography()
        loadTimeTags()
    }

    private fun loadTimeTags() {
        viewModelScope.launch {
            placeListRepository
                .getTimeTags()
                .onSuccess { timeTags ->
                    _timeTags.tryEmit(
                        PlaceUiState.Success(timeTags),
                    )
                }.onFailure {
                    _timeTags.tryEmit(PlaceUiState.Empty)
                }

            // 기본 선택값
            val timeTags = timeTags.value
            if (timeTags is PlaceUiState.Success && timeTags.value.isNotEmpty()) {
                _selectedTimeTag.tryEmit(
                    PlaceUiState.Success(
                        timeTags.value.first(),
                    ),
                )
            } else {
                _selectedTimeTag.tryEmit(PlaceUiState.Empty)
            }
        }
    }

    fun onDaySelected(item: TimeTag) {
        unselectPlace()
        _selectedTimeTag.tryEmit(
            PlaceUiState.Success(item),
        )
    }

    fun selectPlace(placeId: Long) {
        viewModelScope.launch {
            _selectedPlace.value = PlaceUiState.Loading
            placeDetailRepository
                .getPlaceDetail(placeId = placeId)
                .onSuccess {
                    _selectedPlace.value = PlaceUiState.Success(it.toUiModel())
                }.onFailure {
                    _selectedPlace.value = PlaceUiState.Error(it)
                }
        }
    }

    fun unselectPlace() {
        _selectedPlace.value = PlaceUiState.Empty
    }

    fun onExpandedStateReached() {
        val currentPlace = _selectedPlace.value.let { it as? PlaceUiState.Success }?.value
        if (currentPlace != null) {
            _navigateToDetail.tryEmit(currentPlace)
        }
    }

    fun onBackToInitialPositionClicked() {
        _backToInitialPositionClicked.tryEmit(Event(Unit))
    }

    fun setIsExceededMaxLength(isExceededMaxLength: Boolean) {
        _isExceededMaxLength.value = isExceededMaxLength
    }

    fun setSelectedCategories(categories: List<PlaceCategoryUiModel>) {
        _selectedCategories.value = categories
    }

    fun onMapViewClick() {
        _onMapViewClick.tryEmit(Event(Unit))
    }

    fun onMenuItemReClick() {
        _onMenuItemReClick.tryEmit(Event(Unit))
    }

    private fun loadOrganizationGeography() {
        viewModelScope.launch {
            placeListRepository.getOrganizationGeography().onSuccess { organizationGeography ->
                _initialMapSetting.tryEmit(
                    PlaceUiState.Success(organizationGeography.toUiModel()),
                )
            }

            launch {
                placeListRepository
                    .getPlaceGeographies()
                    .onSuccess { placeGeographies ->
                        _placeGeographies.tryEmit(
                            PlaceUiState.Success(placeGeographies.map { it.toUiModel() }),
                        )
                    }.onFailure {
                        _placeGeographies.tryEmit(
                            PlaceUiState.Error(it),
                        )
                    }
            }
        }
    }
}
