package com.daedan.festabook.presentation.placeMap.intent.handler

import com.daedan.festabook.di.placeMapHandler.CachedPlaceByTimeTag
import com.daedan.festabook.di.placeMapHandler.CachedPlaces
import com.daedan.festabook.di.placeMapHandler.PlaceMapViewModelScope
import com.daedan.festabook.domain.model.PlaceCategory
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.placeMap.intent.action.FilterAction
import com.daedan.festabook.presentation.placeMap.intent.event.MapControlSideEffect
import com.daedan.festabook.presentation.placeMap.intent.state.ListLoadState
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import com.daedan.festabook.presentation.placeMap.intent.state.await
import com.daedan.festabook.presentation.placeMap.logging.PlaceCategoryClick
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.placeMap.model.toUiModel
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(PlaceMapViewModelScope::class)
class FilterActionHandler(
    override val uiState: StateFlow<PlaceMapUiState>,
    override val onUpdateState: ((PlaceMapUiState) -> PlaceMapUiState) -> Unit,
    private val _mapControlSideEffect: Channel<MapControlSideEffect>,
    private val logger: DefaultFirebaseLogger,
    private val onUpdateCachedPlace: (List<PlaceUiModel>) -> Unit,
    @param:CachedPlaces private val cachedPlaces: StateFlow<List<PlaceUiModel>>,
    @param:CachedPlaceByTimeTag private val cachedPlaceByTimeTag: StateFlow<List<PlaceUiModel>>,
) : ActionHandler<FilterAction, PlaceMapUiState> {
    override suspend operator fun invoke(action: FilterAction) {
        when (action) {
            is FilterAction.OnCategoryClick -> {
                uiState.await<ListLoadState.Success<PlaceUiModel>> { it.places }
                unselectPlace()
                updatePlacesByCategories(action.categories.toList())

                onUpdateState.invoke {
                    it.copy(selectedCategories = action.categories)
                }

                _mapControlSideEffect.send(MapControlSideEffect.FilterMapByCategory(action.categories.toList()))

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
        _mapControlSideEffect.trySend(MapControlSideEffect.UnselectMarker)
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
        val primaryCategoriesSelected = category.none { it in secondaryCategories }

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
