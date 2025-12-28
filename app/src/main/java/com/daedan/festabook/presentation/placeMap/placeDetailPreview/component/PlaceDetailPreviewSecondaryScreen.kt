package com.daedan.festabook.presentation.placeMap.placeDetailPreview.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.R
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiState
import com.daedan.festabook.presentation.placeMap.model.getIconId
import com.daedan.festabook.presentation.placeMap.model.getTextId
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.FestabookTypography
import com.daedan.festabook.presentation.theme.festabookShapes
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun PlaceDetailPreviewSecondaryScreen(
    placeUiState: PlaceUiState<PlaceDetailUiModel>,
    modifier: Modifier = Modifier,
    onError: (PlaceUiState.Error) -> Unit = {},
    onEmpty: () -> Unit = {},
    onClick: (PlaceUiState<PlaceDetailUiModel>) -> Unit = {},
    visible: Boolean = false,
) {
    PreviewAnimatableBox(
        visible = visible,
        modifier =
            modifier
                .fillMaxWidth()
                .clickable {
                    onClick(placeUiState)
                },
        shape = festabookShapes.radius2,
    ) {
        when (placeUiState) {
            is PlaceUiState.Loading -> Unit
            is PlaceUiState.Error -> onError(placeUiState)
            is PlaceUiState.Empty -> onEmpty()
            is PlaceUiState.Success -> {
                Row(
                    modifier =
                        Modifier.padding(
                            horizontal = festabookSpacing.paddingBody4,
                            vertical = festabookSpacing.paddingBody3,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter =
                            painterResource(
                                placeUiState.value.place.category
                                    .getIconId(),
                            ),
                        tint = Color.Unspecified,
                        contentDescription = stringResource(R.string.content_description_iv_category_marker),
                    )

                    Text(
                        modifier = Modifier.padding(start = festabookSpacing.paddingBody2),
                        text =
                            placeUiState.value.place.title
                                ?: stringResource(
                                    placeUiState.value.place.category
                                        .getTextId(),
                                ),
                        style = FestabookTypography.displaySmall,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceDetailPreviewSecondaryScreenPreview() {
    FestabookTheme {
        PlaceDetailPreviewSecondaryScreen(
            visible = true,
            modifier = Modifier.padding(horizontal = festabookSpacing.paddingScreenGutter),
            placeUiState =
                PlaceUiState.Success(
                    FAKE_PLACE_DETAIL,
                ),
        )
    }
}

private val FAKE_PLACE =
    PlaceUiModel(
        id = 1,
        imageUrl = null,
        category = PlaceCategoryUiModel.TOILET,
        title = "테스트테스",
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
