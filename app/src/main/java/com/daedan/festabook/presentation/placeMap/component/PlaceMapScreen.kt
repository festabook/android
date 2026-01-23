package com.daedan.festabook.presentation.placeMap.component

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.ImageResult
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.common.ObserveAsEvents
import com.daedan.festabook.presentation.common.convertImageUrl
import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.intent.event.FilterEvent
import com.daedan.festabook.presentation.placeMap.intent.event.MapControlEvent
import com.daedan.festabook.presentation.placeMap.intent.event.PlaceMapEvent
import com.daedan.festabook.presentation.placeMap.intent.event.SelectEvent
import com.daedan.festabook.presentation.placeMap.intent.handler.MapControlSideEffectHandler
import com.daedan.festabook.presentation.placeMap.intent.handler.PlaceMapSideEffectHandler
import com.daedan.festabook.presentation.placeMap.intent.sideEffect.PlaceMapSideEffect
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.intent.state.MapDelegate
import com.daedan.festabook.presentation.placeMap.intent.state.MapManagerDelegate
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.festabookSpacing
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber

@Composable
@Suppress("ktlint:compose:vm-forwarding-check")
fun PlaceMapRoute(
    placeMapViewModel: PlaceMapViewModel,
    onStartPlaceDetail: (PlaceMapSideEffect.StartPlaceDetail) -> Unit,
    locationSource: FusedLocationSource,
    logger: DefaultFirebaseLogger,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by placeMapViewModel.uiState.collectAsStateWithLifecycle()
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
                onPreloadImages = {
                    preloadImages(
                        context = context,
                        scope = scope,
                        places = it.places,
                    )
                },
                onShowErrorSnackBar = { },
            )
        }

    ObserveAsEvents(flow = placeMapViewModel.mapControlSideEffect) { event ->
        mapControlSideEffectHandler(event)
    }

    ObserveAsEvents(flow = placeMapViewModel.placeMapSideEffect) { event ->
        placeMapSideEffectHandler(event)
    }

    PlaceMapScreen(
        uiState = uiState,
        modifier = modifier,
        onEvent = { placeMapViewModel.onPlaceMapEvent(it) },
        bottomSheetState = bottomSheetState,
        mapDelegate = mapDelegate,
    )
}

@Composable
fun PlaceMapScreen(
    uiState: PlaceMapUiState,
    onEvent: (PlaceMapEvent) -> Unit,
    bottomSheetState: PlaceListBottomSheetState,
    mapDelegate: MapDelegate,
    modifier: Modifier = Modifier,
) {
    NaverMapContent(
        modifier = modifier.fillMaxSize(),
        mapDelegate = mapDelegate,
        onMapReady = { onEvent(MapControlEvent.OnMapReady) },
        onMapDrag = { onEvent(MapControlEvent.OnMapDrag) },
    ) { naverMap ->
        Column(
            modifier = Modifier.wrapContentSize(),
        ) {
            TimeTagMenu(
                timeTagsState = uiState.timeTags,
                selectedTimeTagState = uiState.selectedTimeTag,
                onTimeTagClick = { timeTag ->
                    onEvent(SelectEvent.OnTimeTagClick(timeTag))
                },
                modifier =
                    Modifier
                        .background(
                            FestabookColor.white,
                        ).padding(horizontal = festabookSpacing.timeTagHorizontalPadding),
            )
            PlaceCategoryScreen(
                initialCategories = uiState.initialCategories,
                selectedCategories = uiState.selectedCategories,
                onCategoryClick = { onEvent(FilterEvent.OnCategoryClick(it)) },
                onDisplayAllClick = { onEvent(FilterEvent.OnCategoryClick(it)) },
            )

            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                NaverMapLogo(
                    modifier =
                        Modifier.padding(
                            horizontal = festabookSpacing.paddingScreenGutter,
                        ),
                )

                PlaceListScreen(
                    modifier =
                        Modifier.alpha(
                            if (uiState.selectedPlace is LoadState.Empty) {
                                1f
                            } else {
                                0f
                            },
                        ),
                    placesUiState = uiState.places,
                    map = naverMap,
                    onPlaceClick = { onEvent(SelectEvent.OnPlaceClick(it.id)) },
                    bottomSheetState = bottomSheetState,
                    isExceededMaxLength = uiState.isExceededMaxLength,
                    onPlaceLoadFinish = { onEvent(MapControlEvent.OnPlaceLoadFinish(it)) },
                    onPlaceLoad = { onEvent(FilterEvent.OnPlaceLoad) },
                    onBackToInitialPositionClick = { onEvent(MapControlEvent.OnBackToInitialPositionClick) },
                )

                if (uiState.isPlacePreviewVisible) {
                    PlaceDetailPreviewScreen(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(
                                    vertical = festabookSpacing.paddingBody4,
                                    horizontal = festabookSpacing.paddingScreenGutter,
                                ),
                        selectedPlace = uiState.selectedPlace,
                        visible = true,
                        onClick = { onEvent(SelectEvent.OnPlacePreviewClick(it)) },
                        onBackPress = { onEvent(SelectEvent.OnBackPress) },
                    )
                }

                if (uiState.isPlaceSecondaryPreviewVisible) {
                    PlaceDetailPreviewSecondaryScreen(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(
                                    vertical = festabookSpacing.paddingBody4,
                                    horizontal = festabookSpacing.paddingScreenGutter,
                                ),
                        selectedPlace = uiState.selectedPlace,
                        visible = true,
                        onBackPress = { onEvent(SelectEvent.OnBackPress) },
                    )
                }
            }
        }
    }
}

private fun preloadImages(
    context: Context,
    scope: CoroutineScope,
    places: List<PlaceUiModel?>,
    maxSize: Int = 20,
) {
    val imageLoader = context.imageLoader
    val deferredList = mutableListOf<Deferred<ImageResult?>>()
    scope.launch(Dispatchers.IO) {
        places
            .take(maxSize)
            .filterNotNull()
            .forEach { place ->
                val deferred =
                    async {
                        val request =
                            ImageRequest
                                .Builder(context)
                                .data(place.imageUrl.convertImageUrl())
                                .build()

                        runCatching {
                            withTimeout(2000) {
                                imageLoader.execute(request)
                            }
                        }.onFailure {
                            Timber.d("preload 실패")
                        }.getOrNull()
                    }
                deferredList.add(deferred)
            }
        deferredList.awaitAll()
    }
}
