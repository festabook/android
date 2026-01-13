package com.daedan.festabook.presentation.placeMap.intent.handler

import com.daedan.festabook.di.placeMapHandler.PlaceMapViewModelScope
import com.daedan.festabook.domain.model.PlaceCategory
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.placeMap.intent.event.FilterEvent
import com.daedan.festabook.presentation.placeMap.intent.sideEffect.MapControlSideEffect
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Inject
@ContributesBinding(PlaceMapViewModelScope::class)
class FilterEventHandler(
    private val context: EventHandlerContext,
    private val logger: DefaultFirebaseLogger,
) : EventHandler<FilterEvent, PlaceMapUiState> {
    override val uiState: StateFlow<PlaceMapUiState> = context.uiState
    override val onUpdateState = context.onUpdateState

    override operator fun invoke(event: FilterEvent) {
        when (event) {
            is FilterEvent.OnCategoryClick -> {
                context.scope.launch {
                    uiState.await<ListLoadState.Success<PlaceUiModel>> { it.places }
                    unselectPlace()
                    updatePlacesByCategories(event.categories.toList())

                    onUpdateState.invoke {
                        it.copy(selectedCategories = event.categories)
                    }

                    context.mapControlSideEffect.send(MapControlSideEffect.FilterMapByCategory(event.categories.toList()))

                    logger.log(
                        PlaceCategoryClick(
                            baseLogData = logger.getBaseLogData(),
                            currentCategories = event.categories.joinToString(",") { it.toString() },
                        ),
                    )
                }
            }

            is FilterEvent.OnPlaceLoad -> {
                context.scope.launch {
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
    }

    private fun unselectPlace() {
        onUpdateState.invoke { it.copy(selectedPlace = LoadState.Empty) }
        context.mapControlSideEffect.trySend(MapControlSideEffect.UnselectMarker)
    }

    fun updatePlacesByTimeTag(timeTagId: Long) {
        val filteredPlaces =
            if (timeTagId == TimeTag.EMTPY_TIME_TAG_ID) {
                context.cachedPlaces.value
            } else {
                filterPlacesByTimeTag(timeTagId)
            }
        onUpdateState.invoke {
            it.copy(places = ListLoadState.Success(filteredPlaces))
        }
        context.onUpdateCachedPlace(filteredPlaces)
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
            context.cachedPlaceByTimeTag.value
                .filter { place ->
                    place.category in category
                }
        onUpdateState.invoke {
            it.copy(places = ListLoadState.Success(filteredPlaces))
        }
    }

    private fun filterPlacesByTimeTag(timeTagId: Long): List<PlaceUiModel> {
        val filteredPlaces =
            context.cachedPlaces.value.filter { place ->
                place.timeTagId.contains(timeTagId)
            }
        return filteredPlaces
    }

    private fun clearPlacesFilter() {
        onUpdateState.invoke {
            it.copy(places = ListLoadState.Success(context.cachedPlaceByTimeTag.value))
        }
    }
}
