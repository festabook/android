package com.daedan.festabook.presentation.placeMap.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.main.MainTabRoute
import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.component.PlaceMapRoute
import com.daedan.festabook.presentation.placeMap.component.rememberPlaceListBottomSheetState
import com.daedan.festabook.presentation.placeMap.intent.handler.MapControlSideEffectHandler
import com.daedan.festabook.presentation.placeMap.intent.handler.PlaceMapSideEffectHandler
import com.daedan.festabook.presentation.placeMap.intent.sideEffect.PlaceMapSideEffect
import com.daedan.festabook.presentation.placeMap.intent.state.MapDelegate
import com.daedan.festabook.presentation.placeMap.intent.state.MapManagerDelegate
import com.naver.maps.map.util.FusedLocationSource

fun NavGraphBuilder.placeMapNavGraph(
    padding: PaddingValues,
    placeMapViewModel: PlaceMapViewModel,
    logger: DefaultFirebaseLogger,
    locationSource: FusedLocationSource,
    onStartPlaceDetail: (PlaceMapSideEffect.StartPlaceDetail) -> Unit,
    onPreloadImages: (PlaceMapSideEffect.PreloadImages) -> Unit,
    onShowErrorSnackBar: (PlaceMapSideEffect.ShowErrorSnackBar) -> Unit,
) {
    composable<MainTabRoute.PlaceMap> {
        val density = LocalDensity.current
        val bottomSheetState = rememberPlaceListBottomSheetState()
        val mapDelegate = remember { MapDelegate() }
        val mapManagerDelegate = remember { MapManagerDelegate() }
        val mapControlSideEffectHandler =
            remember {
                MapControlSideEffectHandler(
                    initialPadding = with(density) { 254.dp.toPx() }.toInt(),
                    logger = logger,
                    locationSource = locationSource,
                    viewModel = placeMapViewModel,
                    mapDelegate = mapDelegate,
                    mapManagerDelegate = mapManagerDelegate,
                )
            }
        val placeMapSideEffectHandler =
            remember {
                PlaceMapSideEffectHandler(
                    mapManagerDelegate = mapManagerDelegate,
                    bottomSheetState = bottomSheetState,
                    viewModel = placeMapViewModel,
                    logger = logger,
                    onStartPlaceDetail = onStartPlaceDetail,
                    onPreloadImages = onPreloadImages,
                    onShowErrorSnackBar = onShowErrorSnackBar,
                )
            }

        PlaceMapRoute(
            modifier = Modifier.padding(padding),
            placeMapViewModel = placeMapViewModel,
            mapControlSideEffectHandler = mapControlSideEffectHandler,
            placeMapSideEffectHandler = placeMapSideEffectHandler,
            mapDelegate = mapDelegate,
        )
    }
}
