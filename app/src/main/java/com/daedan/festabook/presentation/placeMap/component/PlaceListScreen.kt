package com.daedan.festabook.presentation.placeMap.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.CoilImage
import com.daedan.festabook.presentation.common.component.EmptyStateScreen
import com.daedan.festabook.presentation.common.component.LoadingStateScreen
import com.daedan.festabook.presentation.placeMap.intent.state.ListLoadState
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.festabookShapes
import com.daedan.festabook.presentation.theme.festabookSpacing
import com.naver.maps.map.NaverMap
import kotlinx.coroutines.launch

@Composable
fun PlaceListScreen(
    placesUiState: ListLoadState<List<PlaceUiModel>>,
    modifier: Modifier = Modifier,
    map: NaverMap? = null,
    isExceededMaxLength: Boolean = false,
    bottomSheetState: PlaceListBottomSheetState =
        rememberPlaceListBottomSheetState(
            PlaceListBottomSheetValue.HALF_EXPANDED,
        ),
    onPlaceClick: (place: PlaceUiModel) -> Unit = {},
    onPlaceLoadFinish: (places: List<PlaceUiModel>) -> Unit = {},
    onPlaceLoad: suspend () -> Unit = {},
    onBackToInitialPositionClick: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var offset by remember { mutableFloatStateOf(0f) }
    val currentOnPlaceLoad by rememberUpdatedState(onPlaceLoad)
    val currentOnPlaceLoadFinish by rememberUpdatedState(onPlaceLoadFinish)

    LaunchedEffect(placesUiState) {
        when (placesUiState) {
            is ListLoadState.PlaceLoaded -> launch { currentOnPlaceLoad() }
            is ListLoadState.Success -> currentOnPlaceLoadFinish(placesUiState.value)
            else -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (bottomSheetState.currentValue != PlaceListBottomSheetValue.EXPANDED) {
            OffsetDependentLayout(
                modifier =
                    Modifier
                        .padding(horizontal = festabookSpacing.paddingBody1),
                offset = offset,
            ) {
                Box {
                    CurrentLocationButton(
                        map = map,
                    )
                    if (isExceededMaxLength) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            BackToPositionButton(
                                text = stringResource(R.string.map_back_to_initial_position),
                                onClick = onBackToInitialPositionClick,
                            )
                        }
                    }
                }
            }
        }

        PlaceListBottomSheet(
            peekHeight = festabookSpacing.placeListBottomSheetPeekHeight,
            halfExpandedRatio = festabookSpacing.placeListBottomSheetHalfRatio,
            onStateUpdate = {
                if (listState.firstVisibleItemIndex != 0) {
                    scope.launch { listState.scrollToItem(0) }
                }
            },
            onScroll = { offset = it },
            bottomSheetState = bottomSheetState,
            dragHandle = {
                Text(
                    text = stringResource(R.string.place_list_title),
                    style = MaterialTheme.typography.displayLarge,
                    modifier =
                        Modifier
                            .padding(
                                top = festabookSpacing.paddingBody4,
                                bottom = festabookSpacing.paddingBody1,
                            ).padding(horizontal = festabookSpacing.paddingScreenGutter),
                )
            },
        ) {
            when (placesUiState) {
                is ListLoadState.Loading ->
                    LoadingStateScreen(
                        modifier = Modifier.offset(y = HALF_EXPANDED_OFFSET),
                    )

                is ListLoadState.Error -> {
                    EmptyStateScreen(
                        modifier = Modifier.offset(y = HALF_EXPANDED_OFFSET),
                    )
                }

                is ListLoadState.Success -> {
                    if (placesUiState.value.isEmpty()) {
                        EmptyStateScreen(
                            modifier = Modifier.offset(y = HALF_EXPANDED_OFFSET),
                        )
                    } else {
                        PlaceListContent(
                            places = placesUiState.value,
                            modifier = Modifier.padding(horizontal = festabookSpacing.paddingScreenGutter),
                            listState = listState,
                            onPlaceClick = onPlaceClick,
                        )
                    }
                }

                is ListLoadState.PlaceLoaded -> Unit
            }
        }
    }
}

@Composable
private fun PlaceListContent(
    places: List<PlaceUiModel>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onPlaceClick: (PlaceUiModel) -> Unit = {},
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxHeight(),
    ) {
        items(
            items = places,
            key = { place -> place.id },
        ) { place ->
            PlaceListItem(
                place = place,
                onPlaceClick = onPlaceClick,
            )
        }
    }
}

@Composable
private fun PlaceListItem(
    place: PlaceUiModel,
    modifier: Modifier = Modifier,
    onPlaceClick: (PlaceUiModel) -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .padding(bottom = festabookSpacing.paddingBody3)
                .clickable(
                    onClick = { onPlaceClick(place) },
                    interactionSource = null,
                ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoilImage(
                url = place.imageUrl ?: "",
                contentDescription = stringResource(R.string.content_description_booth_image),
                modifier =
                    Modifier
                        .size(festabookSpacing.placeListImageSize)
                        .clip(festabookShapes.radius2),
            )
            PlaceListItemContent(
                modifier =
                    Modifier
                        .padding(start = festabookSpacing.paddingBody3)
                        .weight(1f),
                place = place,
            )
        }
        HorizontalDivider(
            modifier =
                Modifier
                    .padding(
                        top = festabookSpacing.paddingBody4,
                    ),
        )
    }
}

private val HALF_EXPANDED_OFFSET = (-200).dp

@Composable
private fun PlaceListItemContent(
    place: PlaceUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        PlaceCategoryLabel(
            category = place.category,
        )
        Text(
            modifier = Modifier.padding(top = festabookSpacing.paddingBody1),
            text = place.title ?: stringResource(R.string.place_list_default_title),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.padding(top = 2.dp),
            text =
                place.description
                    ?: stringResource(R.string.place_list_default_description),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(R.drawable.ic_location),
                contentDescription = stringResource(R.string.content_description_iv_location),
            )
            Text(
                modifier = Modifier.padding(start = festabookSpacing.paddingBody1),
                text =
                    place.location
                        ?: stringResource(R.string.place_list_default_location),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun PlaceListScreenPreview() {
    FestabookTheme {
        PlaceListScreen(
            placesUiState =
                ListLoadState.Success(
                    (0..100).map {
                        PlaceUiModel(
                            id = it.toLong(),
                            imageUrl = null,
                            title = "테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트",
                            description = "테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트",
                            location = "테스트테스트테스트테스트테스트테스트테스트테스트테스트",
                            category = PlaceCategoryUiModel.FOOD_TRUCK,
                            isBookmarked = true,
                            timeTagId = listOf(1),
                        )
                    },
                ),
            modifier =
                Modifier.padding(
                    horizontal = festabookSpacing.paddingScreenGutter,
                ),
        )
    }
}
