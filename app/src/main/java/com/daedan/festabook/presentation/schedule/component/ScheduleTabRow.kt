package com.daedan.festabook.presentation.schedule.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.daedan.festabook.presentation.common.component.cardBackground
import com.daedan.festabook.presentation.schedule.model.ScheduleDateUiModel
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.festabookShapes
import com.daedan.festabook.presentation.theme.festabookSpacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ScheduleTabRow(
    pageState: PagerState,
    scope: CoroutineScope,
    dates: List<ScheduleDateUiModel>,
    modifier: Modifier = Modifier,
) {
    ScrollableTabRow(
        edgePadding = festabookSpacing.paddingScreenGutter,
        selectedTabIndex = pageState.currentPage,
        containerColor = MaterialTheme.colorScheme.background,
        indicator = { tabPositions ->
            ScheduleTabIndicator(modifier = Modifier.tabIndicatorOffset(currentTabPosition = tabPositions[pageState.currentPage]))
        },
        divider = {},
        modifier = modifier,
    ) {
        dates.forEachIndexed { index, scheduleDate ->
            Tab(
                selected = pageState.currentPage == index,
                unselectedContentColor = FestabookColor.gray500,
                selectedContentColor = MaterialTheme.colorScheme.background,
                onClick = { scope.launch { pageState.scrollToPage(index) } },
                text = { Text(text = scheduleDate.date) },
            )
        }
    }
}

@Composable
private fun ScheduleTabIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .padding(festabookSpacing.paddingBody1)
                .fillMaxSize()
                .cardBackground(
                    backgroundColor = FestabookColor.black,
                    borderStroke = 0.dp,
                    borderColor = FestabookColor.black,
                    shape = festabookShapes.radius4,
                ).zIndex(-1f),
    )
}

@Preview
@Composable
private fun ScheduleTabRowPreview() {
    ScheduleTabRow(
        pageState = rememberPagerState { 5 },
        scope = rememberCoroutineScope(),
        dates =
            listOf(
                ScheduleDateUiModel(1, "11/12"),
                ScheduleDateUiModel(2, "11/13"),
                ScheduleDateUiModel(3, "11/13"),
                ScheduleDateUiModel(3, "11/13"),
                ScheduleDateUiModel(3, "11/13"),
            ),
    )
}
