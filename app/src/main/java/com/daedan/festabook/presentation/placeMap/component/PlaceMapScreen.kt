package com.daedan.festabook.presentation.placeMap.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.placeMap.placeCategory.component.PlaceCategoryScreen
import com.daedan.festabook.presentation.placeMap.timeTagSpinner.component.TimeTagMenu
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.naver.maps.map.NaverMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceMapScreen(
    timeTagTitle: String,
    timeTags: List<TimeTag>,
    places: List<PlaceUiModel>,
    modifier: Modifier = Modifier,
    onMapReady: (NaverMap) -> Unit = {},
    onPlaceClick: (PlaceUiModel) -> Unit = {},
    onTimeTagClick: (TimeTag) -> Unit = {},
) {
    PlaceMapContent(
        title = timeTagTitle,
        timeTags = timeTags,
        onMapReady = onMapReady,
        onTimeTagClick = onTimeTagClick,
    )
}

@Composable
private fun PlaceMapContent(
    timeTags: List<TimeTag>,
    title: String,
    onMapReady: (NaverMap) -> Unit,
    onTimeTagClick: (TimeTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    NaverMapContent(
        modifier = modifier.fillMaxSize(),
        onMapReady = onMapReady,
    ) {
        Column(
            modifier = Modifier.wrapContentSize(),
        ) {
            if (timeTags.isNotEmpty()) {
                TimeTagMenu(
                    title = title,
                    timeTags = timeTags,
                    onTimeTagClick = { timeTag ->
                        onTimeTagClick(timeTag)
                    },
                    modifier =
                        Modifier
                            .background(
                                FestabookColor.white,
                            ).padding(horizontal = 24.dp),
                )
            }
            PlaceCategoryScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceMapScreenPreview() {
    FestabookTheme {
        PlaceMapScreen(
            timeTagTitle = "테스트",
            timeTags =
                listOf(
                    TimeTag(1, "테스트1"),
                    TimeTag(2, "테스트2"),
                ),
            places =
                (0..100).map {
                    PlaceUiModel(
                        id = it.toLong(),
                        imageUrl = null,
                        title = "테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트",
                        description = "테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트",
                        location = "테스트테스트테스트테스트테스트테스트테스트테스트테스트",
                        category = PlaceCategoryUiModel.BAR,
                        isBookmarked = true,
                        timeTagId = listOf(1),
                    )
                },
        )
    }
}
