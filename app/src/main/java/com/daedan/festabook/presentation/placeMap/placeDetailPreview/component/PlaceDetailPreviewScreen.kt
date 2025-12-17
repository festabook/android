package com.daedan.festabook.presentation.placeMap.placeDetailPreview.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.CoilImage
import com.daedan.festabook.presentation.common.component.URLText
import com.daedan.festabook.presentation.common.component.cardBackground
import com.daedan.festabook.presentation.common.convertImageUrl
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.component.PlaceCategoryLabel
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.placeMap.model.SelectedPlaceUiState
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.FestabookTypography
import com.daedan.festabook.presentation.theme.festabookShapes
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun PlaceDetailPreviewScreen(
    placeUiState: SelectedPlaceUiState,
    modifier: Modifier = Modifier,
    onClick: (SelectedPlaceUiState) -> Unit = {},
    onError: () -> Unit = {},
    onEmpty: () -> Unit = {},
) {
    val visibleState =
        remember {
            MutableTransitionState(false).apply { targetState = true }
        }
    val density = LocalDensity.current

    AnimatedVisibility(
        visibleState = visibleState,
        enter =
            fadeIn(
                initialAlpha = 0.3f, // 시작 투명도 0.3f
            ) +
                slideInVertically(
                    initialOffsetY = { with(density) { 120.dp.roundToPx() } }, // 120dp 아래에서 시작
                ),
        modifier = modifier,
    ) {
        Box(
            modifier =
                Modifier
                    .cardBackground(
                        backgroundColor = FestabookColor.white,
                        borderColor = FestabookColor.gray200,
                        shape = festabookShapes.radius5,
                    ),
        ) {
            when (placeUiState) {
                is SelectedPlaceUiState.Loading -> Unit
                is SelectedPlaceUiState.Success -> {
                    PlaceDetailPreviewContent(placeDetail = placeUiState.value)
                }

                is SelectedPlaceUiState.Error -> onError()
                is SelectedPlaceUiState.Empty -> onEmpty()
            }
        }
    }
}

@Composable
private fun PlaceDetailPreviewContent(
    placeDetail: PlaceDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.padding(
                horizontal = festabookSpacing.paddingScreenGutter,
                vertical = 20.dp,
            ),
    ) {
        PlaceCategoryLabel(
            category = placeDetail.place.category,
        )

        Row(modifier = Modifier.wrapContentSize()) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    modifier =
                        Modifier
                            .padding(top = festabookSpacing.paddingBody1),
                    style = FestabookTypography.displaySmall,
                    text =
                        placeDetail.place.title
                            ?: stringResource(R.string.place_list_default_title),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.padding(top = festabookSpacing.paddingBody3),
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
            }

            CoilImage(
                modifier =
                    Modifier
                        .size(88.dp)
                        .clip(festabookShapes.radius2),
                url = placeDetail.place.imageUrl.convertImageUrl() ?: "",
                contentDescription = stringResource(R.string.content_description_booth_image),
            )
        }

        URLText(
            modifier = Modifier.padding(top = festabookSpacing.paddingBody3),
            text =
                placeDetail.place.description
                    ?: stringResource(R.string.place_list_default_description),
            style = FestabookTypography.bodySmall,
        )
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

@Preview
@Composable
private fun PlaceDetailPreviewScreenPreview() {
    FestabookTheme {
        PlaceDetailPreviewScreen(
            modifier =
                Modifier
                    .padding(festabookSpacing.paddingScreenGutter),
            placeUiState =
                SelectedPlaceUiState.Success(
                    value = FAKE_PLACE_DETAIL,
                ),
        )
    }
}

private val FAKE_PLACE =
    PlaceUiModel(
        id = 1,
        imageUrl = null,
        category = PlaceCategoryUiModel.FOOD_TRUCK,
        title = "테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트",
        description = "https://onlyfor-me-blog.tistory.com/1190",
        location = null,
        isBookmarked = false,
        timeTagId = listOf(1),
    )

private val FAKE_PLACE_DETAIL =
    PlaceDetailUiModel(
        place = FAKE_PLACE,
        notices = listOf(),
        host = null,
        startTime = null,
        endTime = null,
        images = listOf(),
    )
