package com.daedan.festabook.presentation.placeMap.listener

import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.intent.action.SelectAction
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
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
            SelectAction.OnPlaceClick(placeId),
        )
        return true
    }

    override fun onMapClickListener() {
        Timber.d("Map CLick")
        viewModel.onPlaceMapAction(SelectAction.UnSelectPlace)
    }
}
