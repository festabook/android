package com.daedan.festabook.presentation.placeMap.intent.action

import com.daedan.festabook.domain.model.PlaceCategory
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.placeMap.intent.event.MapControlEvent
import com.daedan.festabook.presentation.placeMap.intent.state.ListLoadState
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import com.daedan.festabook.presentation.placeMap.intent.state.await
import com.daedan.festabook.presentation.placeMap.logging.PlaceCategoryClick
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.placeMap.model.toUiModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FilterActionHandler(
    private val _mapControlUiEvent: Channel<MapControlEvent>,
    private val logger: DefaultFirebaseLogger,
    private val uiState: StateFlow<PlaceMapUiState>,
    private val cachedPlaces: StateFlow<List<PlaceUiModel>>,
    private val cachedPlaceByTimeTag: StateFlow<List<PlaceUiModel>>,
    private val onUpdateCachedPlace: (List<PlaceUiModel>) -> Unit,
    private val onUpdateState: ((PlaceMapUiState) -> PlaceMapUiState) -> Unit,
) {
    suspend operator fun invoke(action: FilterAction) {
        when (action) {
            is FilterAction.OnCategoryClick -> {
                uiState.await<ListLoadState.Success<PlaceUiModel>> { it.places }
                unselectPlace()
                updatePlacesByCategories(action.categories.toList())

                onUpdateState.invoke {
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

            is FilterAction.OnPlaceLoad -> {
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
        }
    }

    private fun unselectPlace() {
        onUpdateState.invoke { it.copy(selectedPlace = LoadState.Empty) }
        _mapControlUiEvent.trySend(MapControlEvent.UnselectMarker)
    }

    fun updatePlacesByTimeTag(timeTagId: Long) {
        val filteredPlaces =
            if (timeTagId == TimeTag.EMTPY_TIME_TAG_ID) {
                cachedPlaces.value
            } else {
                filterPlacesByTimeTag(timeTagId)
            }
        onUpdateState.invoke {
            it.copy(places = ListLoadState.Success(filteredPlaces))
        }
        onUpdateCachedPlace(filteredPlaces)
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
            cachedPlaceByTimeTag.value
                .filter { place ->
                    place.category in category
                }
        onUpdateState.invoke {
            it.copy(places = ListLoadState.Success(filteredPlaces))
        }
    }

    private fun filterPlacesByTimeTag(timeTagId: Long): List<PlaceUiModel> {
        val filteredPlaces =
            cachedPlaces.value.filter { place ->
                place.timeTagId.contains(timeTagId)
            }
        return filteredPlaces
    }

    private fun clearPlacesFilter() {
        onUpdateState.invoke {
            it.copy(places = ListLoadState.Success(cachedPlaceByTimeTag.value))
        }
    }
}
