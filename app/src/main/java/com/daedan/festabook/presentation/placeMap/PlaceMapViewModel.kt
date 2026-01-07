package com.daedan.festabook.presentation.placeMap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.daedan.festabook.di.viewmodel.ViewModelKey
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.domain.repository.PlaceDetailRepository
import com.daedan.festabook.domain.repository.PlaceListRepository
import com.daedan.festabook.presentation.common.Event
import com.daedan.festabook.presentation.common.SingleLiveData
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeDetail.model.toUiModel
import com.daedan.festabook.presentation.placeMap.model.InitialMapSettingUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCoordinateUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceListUiState
import com.daedan.festabook.presentation.placeMap.model.SelectedPlaceUiState
import com.daedan.festabook.presentation.placeMap.model.toUiModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey(PlaceMapViewModel::class)
@Inject
class PlaceMapViewModel(
    private val placeListRepository: PlaceListRepository,
    private val placeDetailRepository: PlaceDetailRepository,
) : ViewModel() {
    private val _initialMapSetting: MutableLiveData<PlaceListUiState<InitialMapSettingUiModel>> =
        MutableLiveData()
    val initialMapSetting: LiveData<PlaceListUiState<InitialMapSettingUiModel>> = _initialMapSetting

    private val _placeGeographies: MutableLiveData<PlaceListUiState<List<PlaceCoordinateUiModel>>> =
        MutableLiveData()
    val placeGeographies: LiveData<PlaceListUiState<List<PlaceCoordinateUiModel>>>
        get() = _placeGeographies

    private val _timeTags = MutableStateFlow<List<TimeTag>>(emptyList())
    val timeTags: StateFlow<List<TimeTag>> = _timeTags.asStateFlow()

    private val _selectedTimeTag = MutableLiveData<TimeTag>()
    val selectedTimeTag: LiveData<TimeTag> = _selectedTimeTag

    // 임시 StateFlow
    val selectedTimeTagFlow: StateFlow<TimeTag> =
        _selectedTimeTag.asFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = TimeTag.EMPTY,
        )
    private val _selectedPlace: MutableLiveData<SelectedPlaceUiState> = MutableLiveData()
    val selectedPlace: LiveData<SelectedPlaceUiState> = _selectedPlace

    val selectedPlaceFlow: StateFlow<SelectedPlaceUiState> =
        _selectedPlace
            .asFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = SelectedPlaceUiState.Loading,
            )

    private val _navigateToDetail = SingleLiveData<PlaceDetailUiModel>()
    val navigateToDetail: LiveData<PlaceDetailUiModel> = _navigateToDetail

    private val _isExceededMaxLength: MutableLiveData<Boolean> = MutableLiveData()
    val isExceededMaxLength: LiveData<Boolean> = _isExceededMaxLength

    val isExceededMaxLengthFlow: StateFlow<Boolean> =
        _isExceededMaxLength
            .asFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = false,
            )

    private val _backToInitialPositionClicked: MutableLiveData<Event<Unit>> = MutableLiveData()
    val backToInitialPositionClicked: LiveData<Event<Unit>> = _backToInitialPositionClicked

    private val _selectedCategories: MutableLiveData<List<PlaceCategoryUiModel>> = MutableLiveData()
    val selectedCategories: LiveData<List<PlaceCategoryUiModel>> = _selectedCategories

    private val _onMapViewClick: MutableLiveData<Event<Unit>> = MutableLiveData()
    val onMapViewClick: LiveData<Event<Unit>> = _onMapViewClick

    val onMapViewClickFlow: Flow<Event<Unit>> =
        _onMapViewClick
            .asFlow()

    init {
        loadOrganizationGeography()
        loadTimeTags()
    }

    private fun loadTimeTags() {
        viewModelScope.launch {
            placeListRepository
                .getTimeTags()
                .onSuccess { timeTags ->
                    _timeTags.value = timeTags
                }.onFailure {
                    _timeTags.value = emptyList()
                }

            // 기본 선택값
            if (!timeTags.value.isEmpty()) {
                _selectedTimeTag.value = _timeTags.value.first()
            } else {
                _selectedTimeTag.value = TimeTag.EMPTY
            }
        }
    }

    fun onDaySelected(item: TimeTag) {
        unselectPlace()
        _selectedTimeTag.value = item
    }

    fun selectPlace(placeId: Long) {
        viewModelScope.launch {
            _selectedPlace.value = SelectedPlaceUiState.Loading
            placeDetailRepository
                .getPlaceDetail(placeId = placeId)
                .onSuccess {
                    _selectedPlace.value = SelectedPlaceUiState.Success(it.toUiModel())
                }.onFailure {
                    _selectedPlace.value = SelectedPlaceUiState.Error(it)
                }
        }
    }

    fun unselectPlace() {
        _selectedPlace.value = SelectedPlaceUiState.Empty
    }

    fun onExpandedStateReached() {
        val currentPlace = _selectedPlace.value.let { it as? SelectedPlaceUiState.Success }?.value
        if (currentPlace != null) {
            _navigateToDetail.setValue(currentPlace)
        }
    }

    fun onBackToInitialPositionClicked() {
        _backToInitialPositionClicked.value = Event(Unit)
    }

    fun setIsExceededMaxLength(isExceededMaxLength: Boolean) {
        _isExceededMaxLength.value = isExceededMaxLength
    }

    fun setSelectedCategories(categories: List<PlaceCategoryUiModel>) {
        _selectedCategories.value = categories
    }

    fun onMapViewClick() {
        _onMapViewClick.value = Event(Unit)
    }

    private fun loadOrganizationGeography() {
        viewModelScope.launch {
            placeListRepository.getOrganizationGeography().onSuccess { organizationGeography ->
                _initialMapSetting.value =
                    PlaceListUiState.Success(organizationGeography.toUiModel())
            }

            launch {
                placeListRepository
                    .getPlaceGeographies()
                    .onSuccess { placeGeographies ->
                        _placeGeographies.value =
                            PlaceListUiState.Success(placeGeographies.map { it.toUiModel() })
                    }.onFailure {
                        _placeGeographies.value = PlaceListUiState.Error(it)
                    }
            }
        }
    }
}
