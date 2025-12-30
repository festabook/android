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
import com.daedan.festabook.presentation.placeMap.model.LoadState
import com.daedan.festabook.presentation.placeMap.viewmodel.MapDelegate
import com.daedan.festabook.presentation.placeMap.viewmodel.PlaceMapAction
import com.daedan.festabook.presentation.placeMap.viewmodel.PlaceMapUiState
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
        onMapReady = { onAction(PlaceMapAction.OnMapReady) },
        onMapDrag = { onAction(PlaceMapAction.OnMapDrag) },
    ) { naverMap ->
        Column(
            modifier = Modifier.wrapContentSize(),
        ) {
            TimeTagMenu(
                timeTagsState = uiState.timeTags,
                selectedTimeTagState = uiState.selectedTimeTag,
                onTimeTagClick = { timeTag ->
                    onAction(PlaceMapAction.OnTimeTagClick(timeTag))
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
                onCategoryClick = { onAction(PlaceMapAction.OnCategoryClick(it)) },
                onDisplayAllClick = { onAction(PlaceMapAction.OnCategoryClick(it)) },
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
                    onPlaceClick = { onAction(PlaceMapAction.OnPlaceClick(it.id)) },
                    bottomSheetState = bottomSheetState,
                    isExceededMaxLength = uiState.isExceededMaxLength,
                    onPlaceLoadFinish = { onAction(PlaceMapAction.OnPlaceLoadFinish(it)) },
                    onPlaceLoad = { onAction(PlaceMapAction.OnPlaceLoad) },
                    onBackToInitialPositionClick = { onAction(PlaceMapAction.OnBackToInitialPositionClick) },
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
                        onClick = { onAction(PlaceMapAction.OnPlacePreviewClick(it)) },
                        onBackPress = { onAction(PlaceMapAction.OnBackPress) },
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
                        onBackPress = { onAction(PlaceMapAction.OnBackPress) },
                    )
                }
            }
        }
    }
}
