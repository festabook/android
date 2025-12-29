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
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceListUiState
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiState
import com.daedan.festabook.presentation.placeMap.placeCategory.component.PlaceCategoryScreen
import com.daedan.festabook.presentation.placeMap.placeDetailPreview.component.PlaceDetailPreviewScreen
import com.daedan.festabook.presentation.placeMap.placeDetailPreview.component.PlaceDetailPreviewSecondaryScreen
import com.daedan.festabook.presentation.placeMap.placeList.component.PlaceListBottomSheetState
import com.daedan.festabook.presentation.placeMap.placeList.component.PlaceListScreen
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.festabookSpacing
import com.naver.maps.map.NaverMap

@Composable
fun PlaceMapScreen(
    places: PlaceListUiState<List<PlaceUiModel>>,
    initialCategories: List<PlaceCategoryUiModel>,
    selectedCategoriesState: Set<PlaceCategoryUiModel>,
    selectedPlaceUiState: PlaceUiState<PlaceDetailUiModel>,
    timeTagsState: PlaceUiState<List<TimeTag>>,
    selectedTimeTagState: PlaceUiState<TimeTag>,
    onMapReady: (NaverMap) -> Unit,
    onTimeTagClick: (TimeTag) -> Unit,
    onMapDrag: () -> Unit,
    onPlaceClick: (PlaceUiModel) -> Unit,
    onPlacePreviewClick: (PlaceUiState<PlaceDetailUiModel>) -> Unit,
    onBackPress: () -> Unit,
    onPlacePreviewError: (PlaceUiState.Error) -> Unit,
    isExceedMaxLength: Boolean,
    onPlaceLoadFinish: (List<PlaceUiModel>) -> Unit,
    onPlaceLoad: suspend () -> Unit,
    onPlaceListError: (PlaceListUiState.Error<List<PlaceUiModel>>) -> Unit,
    onBackToInitialPositionClick: () -> Unit,
    onCategoryClick: (Set<PlaceCategoryUiModel>) -> Unit,
    onDisplayAllClick: (Set<PlaceCategoryUiModel>) -> Unit,
    isPlacePreviewVisible: Boolean,
    isPlaceSecondaryPreviewVisible: Boolean,
    bottomSheetState: PlaceListBottomSheetState,
    modifier: Modifier = Modifier,
) {
    NaverMapContent(
        modifier = modifier.fillMaxSize(),
        onMapReady = onMapReady,
        onMapDrag = onMapDrag,
    ) { naverMap ->
        Column(
            modifier = Modifier.wrapContentSize(),
        ) {
            TimeTagMenu(
                timeTagsState = timeTagsState,
                selectedTimeTagState = selectedTimeTagState,
                onTimeTagClick = { timeTag ->
                    onTimeTagClick(timeTag)
                },
                modifier =
                    Modifier
                        .background(
                            FestabookColor.white,
                        ).padding(horizontal = 24.dp),
            )
            PlaceCategoryScreen(
                initialCategories = initialCategories,
                selectedCategories = selectedCategoriesState,
                onCategoryClick = onCategoryClick,
                onDisplayAllClick = onDisplayAllClick,
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
                            if (!isPlacePreviewVisible && !isPlaceSecondaryPreviewVisible) 1f else 0f,
                        ),
                    placesUiState = places,
                    map = naverMap,
                    onPlaceClick = onPlaceClick,
                    bottomSheetState = bottomSheetState,
                    isExceedMaxLength = isExceedMaxLength,
                    onPlaceLoadFinish = onPlaceLoadFinish,
                    onPlaceLoad = onPlaceLoad,
                    onError = onPlaceListError,
                    onBackToInitialPositionClick = onBackToInitialPositionClick,
                )

                PlaceDetailPreviewScreen(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(
                                vertical = festabookSpacing.paddingBody4,
                                horizontal = festabookSpacing.paddingScreenGutter,
                            ),
                    placeUiState = selectedPlaceUiState,
                    visible = isPlacePreviewVisible,
                    onClick = onPlacePreviewClick,
                    onBackPress = onBackPress,
                    onError = onPlacePreviewError,
                )

                PlaceDetailPreviewSecondaryScreen(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(
                                vertical = festabookSpacing.paddingBody4,
                                horizontal = festabookSpacing.paddingScreenGutter,
                            ),
                    placeUiState = selectedPlaceUiState,
                    visible = isPlaceSecondaryPreviewVisible,
                    onBackPress = onBackPress,
                    onError = onPlacePreviewError,
                )
            }
        }
    }
}
