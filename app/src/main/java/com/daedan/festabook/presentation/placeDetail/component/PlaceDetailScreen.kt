package com.daedan.festabook.presentation.placeDetail.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.EmptyStateScreen
import com.daedan.festabook.presentation.common.component.FestabookImage
import com.daedan.festabook.presentation.common.component.LoadingStateScreen
import com.daedan.festabook.presentation.common.component.URLText
import com.daedan.festabook.presentation.placeDetail.PlaceDetailViewModel
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

@Composable
fun PlaceDetailRoute(
    viewModel: PlaceDetailViewModel,
    onBackToPreviousClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val placeDetailUiState by viewModel.placeDetail.collectAsStateWithLifecycle()
    PlaceDetailScreen(
        modifier = modifier,
        uiState = placeDetailUiState,
        onBackToPreviousClick = onBackToPreviousClick,
    )
}

@Composable
fun PlaceDetailScreen(
    uiState: PlaceDetailUiState,
    onBackToPreviousClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    var isDialogOpen by remember { mutableStateOf(false) }

    when (uiState) {
        is PlaceDetailUiState.Success -> {
            val pagerState =
                rememberPagerState(
                    pageCount = { uiState.placeDetail.images.size },
                )

            PlaceDetailImageDialog(
                isDialogOpen = isDialogOpen,
                onDismissRequest = { isDialogOpen = false },
                pagerState = pagerState,
                images = uiState.placeDetail.images,
            )

            Column(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(color = FestabookColor.white)
                        .verticalScroll(scrollState),
            ) {
                PlaceDetailImageContent(
                    images = uiState.placeDetail.images,
                    onBackToPreviousClick = onBackToPreviousClick,
                    onPageUpdate = { pagerState.scrollToPage(it) },
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
    pagerState: PagerState,
    images: List<ImageUiModel>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isDialogOpen) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties =
                DialogProperties(
                    usePlatformDefaultWidth = false,
                ),
        ) {
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(FestabookColor.black.copy(alpha = 0.8f)),
            ) {
                HorizontalPager(
                    state = pagerState,
                    verticalAlignment = Alignment.CenterVertically,
                    beyondViewportPageCount = 5,
                ) { page ->

                    FestabookImage(
                        imageUrl = images[page].url,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        isZoomable = true,
                        enablePopUp = false,
                    )
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "close the popup",
                        tint = FestabookColor.white,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceDetailImageContent(
    images: List<ImageUiModel>,
    onBackToPreviousClick: (() -> Unit),
    modifier: Modifier = Modifier,
    onPageUpdate: suspend (page: Int) -> Unit = {},
) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    val currentOnPageUpdate by rememberUpdatedState(onPageUpdate)
    LaunchedEffect(pagerState.settledPage) {
        currentOnPageUpdate(pagerState.settledPage)
    }

    Box(modifier = modifier) {
        BackToPreviousButton(
            modifier =
                Modifier
                    .padding(
                        top = festabookSpacing.paddingBody4,
                        start = festabookSpacing.paddingScreenGutter,
                    ).zIndex(1f),
            onClick = onBackToPreviousClick,
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            beyondViewportPageCount = 5,
        ) { page ->
            FestabookImage(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                imageUrl = images[page].url,
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
    var isDescriptionExpand by remember { mutableStateOf(true) }

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

        PlaceDetailInfo(placeDetail = placeDetail)

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
private fun PlaceDetailInfo(
    placeDetail: PlaceDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PlaceDetailInfoItem(
            modifier = Modifier.padding(top = festabookSpacing.paddingBody4),
            painter = painterResource(R.drawable.ic_place_detail_clock),
            contentDescription = stringResource(R.string.content_description_iv_clock),
            text = formattedDate(placeDetail.startTime, placeDetail.endTime),
        )

        PlaceDetailInfoItem(
            modifier = Modifier.padding(top = festabookSpacing.paddingBody1),
            painter = painterResource(R.drawable.ic_location),
            contentDescription = stringResource(R.string.content_description_iv_location),
            text =
                placeDetail.place.location
                    ?: stringResource(R.string.place_list_default_location),
        )

        PlaceDetailInfoItem(
            modifier = Modifier.padding(top = festabookSpacing.paddingBody1),
            painter = painterResource(R.drawable.ic_place_detail_host),
            contentDescription = stringResource(R.string.content_description_iv_host),
            text =
                placeDetail.host
                    ?: stringResource(R.string.place_detail_default_host),
        )
    }
}

@Composable
private fun PlaceDetailInfoItem(
    painter: Painter,
    contentDescription: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
        )

        Text(
            modifier = Modifier.padding(start = festabookSpacing.paddingBody1),
            text = text,
            style = FestabookTypography.bodySmall,
            color = FestabookColor.gray500,
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
                .size(30.dp)
                .clickable { onClick() },
        painter = painterResource(id = R.drawable.btn_back_to_previous),
        contentDescription = stringResource(R.string.content_description_exit_place_detail),
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
        stringResource(R.string.format_date, startTime.toString(), endTime.toString())
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
                                            "트테스트테스트테스트http://i1.sndcdn.com/art 트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테" +
                                            "스트테스트테스트테스트https://i.ytimg.com/vi/Wr8egRRLU28/maxresdefault.com테스트테스트테스트테스트" +
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
