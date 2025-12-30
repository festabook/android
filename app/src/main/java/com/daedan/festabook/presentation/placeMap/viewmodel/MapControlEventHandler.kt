package com.daedan.festabook.presentation.placeMap.viewmodel

import com.daedan.festabook.di.mapManager.MapManagerGraph
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.placeMap.logging.CurrentLocationChecked
import com.daedan.festabook.presentation.placeMap.logging.PlaceMarkerClick
import com.daedan.festabook.presentation.placeMap.mapManager.MapManager
import com.daedan.festabook.presentation.placeMap.model.LoadState
import com.naver.maps.map.LocationSource
import dev.zacsweers.metro.createGraphFactory

class MapControlEventHandler(
    private val initialPadding: Int,
    private val logger: DefaultFirebaseLogger,
    private val locationSource: LocationSource,
    private val viewModel: PlaceMapViewModel,
    private val mapDelegate: MapDelegate,
    private val mapManagerDelegate: MapManagerDelegate,
) {
    private val uiState = viewModel.uiState.value
    private val mapManager: MapManager? get() = mapManagerDelegate.value

    suspend operator fun invoke(event: MapControlEvent) {
        when (event) {
            is MapControlEvent.InitMap -> {
                val naverMap = mapDelegate.await()
                naverMap.addOnLocationChangeListener {
                    logger.log(
                        CurrentLocationChecked(
                            baseLogData = logger.getBaseLogData(),
                        ),
                    )
                }
                naverMap.locationSource = locationSource
            }

            is MapControlEvent.InitMapManager -> {
                val naverMap = mapDelegate.await()
                if (mapManager == null) {
                    val graph =
                        createGraphFactory<MapManagerGraph.Factory>().create(
                            naverMap,
                            event.initialMapSetting,
                            viewModel,
                            initialPadding,
                        )
                    mapManagerDelegate.init(graph.mapManager)
                    mapManager?.setupBackToInitialPosition { isExceededMaxLength ->
                        viewModel.onPlaceMapAction(
                            PlaceMapAction.ExceededMaxLength(isExceededMaxLength),
                        )
                    }
                }
            }

            is MapControlEvent.BackToInitialPosition -> {
                mapManager?.moveToPosition()
            }

            is MapControlEvent.SetMarkerByTimeTag -> {
                if (event.isInitial) {
                    mapManager?.setupMarker(event.placeGeographies)
                }

                when (val selectedTimeTag = event.selectedTimeTag) {
                    is LoadState.Success -> {
                        mapManager?.filterMarkersByTimeTag(
                            selectedTimeTag.value.timeTagId,
                        )
                    }

                    is LoadState.Empty -> {
                        mapManager?.filterMarkersByTimeTag(TimeTag.EMTPY_TIME_TAG_ID)
                    }

                    else -> Unit
                }
            }

            is MapControlEvent.FilterMapByCategory -> {
                val selectedCategories = event.selectedCategories
                if (selectedCategories.isEmpty()) {
                    mapManager?.clearFilter()
                } else {
                    mapManager?.filterMarkersByCategories(selectedCategories)
                }
            }

            is MapControlEvent.SelectMarker -> {
                when (val place = event.placeDetail) {
                    is LoadState.Success -> {
                        mapManager?.selectMarker(place.value.place.id)

                        val currentTimeTag = uiState.selectedTimeTag
                        val timeTagName =
                            if (currentTimeTag is LoadState.Success) {
                                currentTimeTag.value.name
                            } else {
                                "undefined"
                            }
                        logger.log(
                            PlaceMarkerClick(
                                baseLogData = logger.getBaseLogData(),
                                placeId = place.value.place.id,
                                timeTagName = timeTagName,
                                category = place.value.place.category.name,
                            ),
                        )
                    }

                    else -> Unit
                }
            }

            is MapControlEvent.UnselectMarker -> {
                mapManager?.unselectMarker()
            }
        }
    }
}
