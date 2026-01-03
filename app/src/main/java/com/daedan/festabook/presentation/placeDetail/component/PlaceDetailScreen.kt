package com.daedan.festabook.presentation.placeDetail.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.EmptyStateScreen
import com.daedan.festabook.presentation.common.component.LoadingStateScreen
import com.daedan.festabook.presentation.common.component.URLText
import com.daedan.festabook.presentation.common.convertImageUrl
import com.daedan.festabook.presentation.placeDetail.model.ImageUiModel
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiState
import com.daedan.festabook.presentation.placeMap.component.PlaceCategoryLabel
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.FestabookTypography
import com.daedan.festabook.presentation.theme.festabookSpacing
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.crossfade.CrossfadePlugin
import com.skydoves.landscapist.zoomable.ZoomablePlugin
import com.skydoves.landscapist.zoomable.rememberZoomableState

@Composable
fun PlaceDetailScreen(
    uiState: PlaceDetailUiState,
    onBackToPreviousClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    var isDialogOpen by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(0) }

    when (uiState) {
        is PlaceDetailUiState.Success -> {
            PlaceDetailImageDialog(
                isDialogOpen = isDialogOpen,
                onDismissRequest = { isDialogOpen = false },
                initialPage = currentPage,
                images = uiState.placeDetail.images,
            )

            Column(
                modifier =
                    modifier
                        .scrollable(
                            state = scrollState,
                            orientation = Orientation.Vertical,
                        ),
            ) {
                PlaceDetailImageContent(
                    images = uiState.placeDetail.images,
                    onBackToPreviousClick = onBackToPreviousClick,
                    onPageUpdate = { currentPage = it },
                    modifier =
                        Modifier
                            .clickable { isDialogOpen = true }
                            .fillMaxWidth(),
                )

                PlaceDetailContent(placeDetail = uiState.placeDetail)
            }
        }

        is PlaceDetailUiState.Loading -> {
            LoadingStateScreen()
        }

        is PlaceDetailUiState.Error -> {
            EmptyStateScreen()
        }
    }
}

@Composable
private fun PlaceDetailImageDialog(
    isDialogOpen: Boolean,
    initialPage: Int,
    images: List<ImageUiModel>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState =
        rememberPagerState(
            initialPage = initialPage,
            pageCount = { images.size },
        )

    if (isDialogOpen) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties =
                DialogProperties(
                    usePlatformDefaultWidth = false,
                ),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(color = FestabookColor.black),
                verticalAlignment = Alignment.CenterVertically,
                beyondViewportPageCount = 5,
            ) { page ->
                val zoomableState = rememberZoomableState()

                CoilImage(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleY = zoomableState.transformation.scale.scaleY
                                scaleX = zoomableState.transformation.scale.scaleX
                            },
                    component =
                        rememberImageComponent {
                            +CrossfadePlugin()
                            +ZoomablePlugin(state = zoomableState)
                        },
                    imageModel = { images[page].url.convertImageUrl() },
                    imageOptions =
                        ImageOptions(
                            contentScale = ContentScale.Crop,
                        ),
                    loading = {
                        Image(
                            painter = ColorPainter(Color.LightGray),
                            contentDescription = null,
                        )
                    },
                    failure = {
                        Image(
                            painter = painterResource(R.drawable.img_fallback),
                            contentDescription = null,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun PlaceDetailImageContent(
    images: List<ImageUiModel>,
    onBackToPreviousClick: (() -> Unit),
    modifier: Modifier = Modifier,
    onPageUpdate: (page: Int) -> Unit = {},
) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(modifier = modifier) {
        BackToPreviousButton(
            modifier =
                Modifier
                    .padding(
                        start = festabookSpacing.paddingScreenGutter,
                        top = festabookSpacing.paddingBody4,
                    ).zIndex(1f),
            onClick = onBackToPreviousClick,
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            beyondViewportPageCount = 5,
        ) { page ->
            onPageUpdate(page)
            CoilImage(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                component =
                    rememberImageComponent {
                        +CrossfadePlugin()
                    },
                imageModel = { images[page].url.convertImageUrl() },
                imageOptions =
                    ImageOptions(
                        contentScale = ContentScale.Crop,
                    ),
                loading = {
                    Image(
                        painter = ColorPainter(Color.LightGray),
                        contentDescription = null,
                    )
                },
                failure = {
                    Image(
                        painter = painterResource(R.drawable.img_fallback),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                    )
                },
            )
        }

        PagerIndicator(
            pagerState = pagerState,
            modifier =
                Modifier
                    .height(24.dp)
                    .align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun PlaceDetailContent(
    placeDetail: PlaceDetailUiModel,
    modifier: Modifier = Modifier,
) {
    var isDescriptionExpand by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(horizontal = festabookSpacing.paddingScreenGutter),
    ) {
        PlaceCategoryLabel(
            modifier = Modifier.padding(top = 24.dp),
            category = placeDetail.place.category,
        )

        Text(
            modifier = Modifier.padding(top = festabookSpacing.paddingBody2),
            text = placeDetail.place.title ?: stringResource(R.string.place_list_default_title),
            style = FestabookTypography.displayMedium,
        )

        Row(
            modifier = Modifier.padding(top = festabookSpacing.paddingBody4),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_place_detail_clock),
                contentDescription = stringResource(R.string.content_description_iv_clock),
            )

            Text(
                modifier = Modifier.padding(start = festabookSpacing.paddingBody1),
                text = formattedDate(placeDetail.startTime, placeDetail.endTime),
                style = FestabookTypography.bodySmall,
                color = FestabookColor.gray500,
            )
        }

        Row(
            modifier = Modifier.padding(top = festabookSpacing.paddingBody1),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location),
                contentDescription = stringResource(R.string.content_description_iv_location),
            )

            Text(
                modifier = Modifier.padding(start = festabookSpacing.paddingBody1),
                text =
                    placeDetail.place.location
                        ?: stringResource(R.string.place_list_default_location),
                style = FestabookTypography.bodySmall,
                color = FestabookColor.gray500,
            )
        }

        Row(
            modifier = Modifier.padding(top = festabookSpacing.paddingBody1),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_place_detail_host),
                contentDescription = stringResource(R.string.content_description_iv_host),
            )

            Text(
                modifier = Modifier.padding(start = festabookSpacing.paddingBody1),
                text =
                    placeDetail.host
                        ?: stringResource(R.string.place_detail_default_host),
                style = FestabookTypography.bodySmall,
                color = FestabookColor.gray500,
            )
        }

        URLText(
            modifier =
                Modifier
                    .animateContentSize(
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                    ).padding(
                        top = festabookSpacing.paddingBody3,
                    ),
            onClick = {
                isDescriptionExpand = !isDescriptionExpand
            },
            text =
                placeDetail.place.description
                    ?: stringResource(R.string.place_list_default_description),
            style = FestabookTypography.bodySmall,
            maxLines =
                if (isDescriptionExpand) {
                    Int.MAX_VALUE
                } else {
                    1
                },
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BackToPreviousButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier =
            modifier
                .padding(top = festabookSpacing.paddingBody4)
                .size(30.dp)
                .clickable { onClick() },
        painter = painterResource(id = R.drawable.btn_back_to_previous),
        contentDescription = null,
    )
}

@Composable
private fun PagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val isSelected = pagerState.currentPage == iteration
            val color = if (isSelected) FestabookColor.black else FestabookColor.gray300
            val size by animateDpAsState(targetValue = if (isSelected) 10.dp else 8.dp)

            Box(
                modifier =
                    Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(size),
            )
        }
    }
}

@Composable
private fun formattedDate(
    startTime: String?,
    endTime: String?,
): String =
    if (startTime == null && endTime == null) {
        stringResource(R.string.place_detail_default_time)
    } else {
        listOf(startTime, endTime).joinToString(" ~ ")
    }

@Preview(showBackground = true)
@Composable
private fun PlaceDetailScreenPreview() {
    FestabookTheme {
        PlaceDetailScreen(
            onBackToPreviousClick = {},
            uiState =
                PlaceDetailUiState.Success(
                    placeDetail =
                        PlaceDetailUiModel(
                            place =
                                PlaceUiModel(
                                    id = 1,
                                    imageUrl = null,
                                    title = "테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트",
                                    description =
                                        "테스트테스트테스트테스트테스트테스.트테스트.테스트테스트테스트테스트//테스트테스트테스트테스트테스" +
                                            "트테스트테스트테스트htt://i1.sndcdn.com/art트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테" +
                                            "스트테스트테스트테스트https://i.ytimg.com/vi/Wr8egRRLU28/maxresdefault.jpg테스트테스트테스트테스트" +
                                            "테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트",
                                    location = "테스트테스트테스트테스트테스트테스트테스트테스트테스트",
                                    category = PlaceCategoryUiModel.FOOD_TRUCK,
                                    isBookmarked = true,
                                    timeTagId = listOf(1),
                                ),
                            notices = emptyList(),
                            host = "테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트",
                            startTime = "09:00",
                            endTime = "18:00",
                            images =
                                listOf(
                                    ImageUiModel(
                                        id = 1,
                                        url = "https://i1.sndcdn.com/artworks-AIxlEDn4gNDBnNJj-qHUnyA-t500x500.jpg",
                                    ),
                                    ImageUiModel(
                                        id = 2,
                                        url = "https://i.ytimg.com/vi/Wr8egRRLU28/maxresdefault.jpg",
                                    ),
                                ),
                        ),
                ),
        )
    }
}
