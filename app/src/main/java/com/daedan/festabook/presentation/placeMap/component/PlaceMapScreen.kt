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
import androidx.compose.ui.unit.dp
import com.daedan.festabook.presentation.placeMap.intent.action.FilterAction
import com.daedan.festabook.presentation.placeMap.intent.action.MapEventAction
import com.daedan.festabook.presentation.placeMap.intent.action.PlaceMapAction
import com.daedan.festabook.presentation.placeMap.intent.action.SelectAction
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.intent.state.MapDelegate
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun PlaceMapScreen(
    uiState: PlaceMapUiState,
    onAction: (PlaceMapAction) -> Unit,
    bottomSheetState: PlaceListBottomSheetState,
    mapDelegate: MapDelegate,
    modifier: Modifier = Modifier,
) {
    NaverMapContent(
        modifier = modifier.fillMaxSize(),
        mapDelegate = mapDelegate,
        onMapReady = { onAction(MapEventAction.OnMapReady) },
        onMapDrag = { onAction(MapEventAction.OnMapDrag) },
    ) { naverMap ->
        Column(
            modifier = Modifier.wrapContentSize(),
        ) {
            TimeTagMenu(
                timeTagsState = uiState.timeTags,
                selectedTimeTagState = uiState.selectedTimeTag,
                onTimeTagClick = { timeTag ->
                    onAction(SelectAction.OnTimeTagClick(timeTag))
                },
                modifier =
                    Modifier
                        .background(
                            FestabookColor.white,
                        ).padding(horizontal = 24.dp),
            )
            PlaceCategoryScreen(
                initialCategories = uiState.initialCategories,
                selectedCategories = uiState.selectedCategories,
                onCategoryClick = { onAction(FilterAction.OnCategoryClick(it)) },
                onDisplayAllClick = { onAction(FilterAction.OnCategoryClick(it)) },
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
                    onPlaceClick = { onAction(SelectAction.OnPlaceClick(it.id)) },
                    bottomSheetState = bottomSheetState,
                    isExceededMaxLength = uiState.isExceededMaxLength,
                    onPlaceLoadFinish = { onAction(MapEventAction.OnPlaceLoadFinish(it)) },
                    onPlaceLoad = { onAction(FilterAction.OnPlaceLoad) },
                    onBackToInitialPositionClick = { onAction(MapEventAction.OnBackToInitialPositionClick) },
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
                        onClick = { onAction(SelectAction.OnPlacePreviewClick(it)) },
                        onBackPress = { onAction(SelectAction.OnBackPress) },
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
                        onBackPress = { onAction(SelectAction.OnBackPress) },
                    )
                }
            }
        }
    }
}
