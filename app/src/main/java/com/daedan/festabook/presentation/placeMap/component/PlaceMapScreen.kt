package com.daedan.festabook.presentation.placeMap.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.presentation.placeMap.timeTagSpinner.component.TimeTagMenu
import com.daedan.festabook.presentation.theme.FestabookColor
import com.naver.maps.map.NaverMap

@Composable
fun PlaceMapScreen(
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
        }
    }
}
