package com.daedan.festabook.presentation.placeMap.listener

import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.viewmodel.PlaceMapAction
import com.daedan.festabook.presentation.placeMap.viewmodel.PlaceMapViewModel
import timber.log.Timber

class MapClickListenerImpl(
    private val viewModel: PlaceMapViewModel,
) : MapClickListener {
    override fun onMarkerListener(
        placeId: Long,
        category: PlaceCategoryUiModel,
    ): Boolean {
        Timber.d("Marker CLick : placeID: $placeId categoty: $category")
        viewModel.onPlaceMapAction(
            PlaceMapAction.OnPlaceClick(placeId),
        )
        return true
    }

    override fun onMapClickListener() {
        Timber.d("Map CLick")
        viewModel.onPlaceMapAction(PlaceMapAction.UnSelectPlace)
    }
}
