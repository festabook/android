package com.daedan.festabook.presentation.placeMap.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.daedan.festabook.presentation.placeMap.intent.event.FilterEvent
import com.daedan.festabook.presentation.placeMap.intent.event.MapControlEvent
import com.daedan.festabook.presentation.placeMap.intent.event.PlaceMapEvent
import com.daedan.festabook.presentation.placeMap.intent.event.SelectEvent
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.intent.state.MapDelegate
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.festabookSpacing

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
