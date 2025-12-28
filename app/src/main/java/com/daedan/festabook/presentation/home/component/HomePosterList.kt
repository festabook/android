package com.daedan.festabook.presentation.home.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.daedan.festabook.presentation.common.component.CoilImage
import com.daedan.festabook.presentation.common.component.cardBackground
import kotlin.math.absoluteValue

@Composable
fun HomePosterList(
    posterUrls: List<String>,
    modifier: Modifier = Modifier,
) {
    if (posterUrls.isEmpty()) return

    // 무한 스크롤을 위한 큰 수 설정
    val initialPage = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % posterUrls.size)
    val pagerState =
        rememberPagerState(
            initialPage = initialPage,
            pageCount = { Int.MAX_VALUE },
        )

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val itemWidth = 300.dp
    // 화면 중앙에 아이템이 오도록 패딩 계산
    val horizontalPadding = (screenWidth - itemWidth) / 2

    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fixed(itemWidth),
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        pageSpacing = 12.dp,
        modifier =
            modifier
                .fillMaxWidth()
                .height(400.dp), // item_home_poster 높이
        verticalAlignment = Alignment.CenterVertically,
    ) { page ->
        val actualIndex = page % posterUrls.size
        val imageUrl = posterUrls[actualIndex]

        // 스크롤 위치에 따른 Scale 계산
        val pageOffset =
            ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

        // 중앙(0)이면 1.0f, 멀어질수록 작아짐 (최소 0.9f)
        val scale =
            lerp(
                start = 1.0f,
                stop = 0.9f,
                fraction = pageOffset.coerceIn(0f, 1f),
            )

        // 투명도 조절 (중앙은 1.0, 멀어지면 약간 투명하게)
        val alpha =
            lerp(
                start = 1.0f,
                stop = 0.6f,
                fraction = pageOffset.coerceIn(0f, 1f),
            )

        Box(
            modifier =
                Modifier
                    .width(itemWidth)
                    .height(400.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .cardBackground(roundedCornerShape = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
        ) {
            CoilImage(
                url = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
private fun HomePosterListPreview() {
    HomePosterList(
        posterUrls =
            listOf(
                "sample",
                "sample",
                "sample",
            ),
    )
}