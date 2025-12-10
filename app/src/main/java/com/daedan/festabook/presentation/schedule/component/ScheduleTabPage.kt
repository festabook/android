package com.daedan.festabook.presentation.schedule.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.presentation.common.component.EmptyStateScreen
import com.daedan.festabook.presentation.common.component.LoadingStateScreen
import com.daedan.festabook.presentation.common.component.PULL_OFFSET_LIMIT
import com.daedan.festabook.presentation.common.component.PullToRefreshContainer
import com.daedan.festabook.presentation.schedule.ScheduleEventsUiState
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiModel
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiStatus
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.festabookSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTabPage(
    pagerState: PagerState,
    selectedDatedId: Long?,
    eventStates: Map<Long, ScheduleEventsUiState>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(state = pagerState, modifier = modifier) {
        val uiState = eventStates[selectedDatedId]
        val isRefreshing = uiState is ScheduleEventsUiState.Refreshing

        PullToRefreshContainer(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
        ) { pullToRefreshState ->

            if (uiState == null) {
                EmptyStateScreen()
            } else {
                when (uiState) {
                    is ScheduleEventsUiState.Error -> {
                    }

                    ScheduleEventsUiState.InitialLoading -> {
                        LoadingStateScreen()
                    }

                    is ScheduleEventsUiState.Refreshing -> {
                    }

                    is ScheduleEventsUiState.Success -> {
                        if (uiState.events.isEmpty()) {
                            EmptyStateScreen()
                        } else {
                            ScheduleTabContent(
                                scheduleEvents = uiState.events,
                                modifier =
                                    Modifier
                                        .padding(end = festabookSpacing.paddingScreenGutter)
                                        .graphicsLayer {
                                            translationY =
                                                pullToRefreshState.distanceFraction * PULL_OFFSET_LIMIT
                                        },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleTabContent(
    scheduleEvents: List<ScheduleEventUiModel>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        VerticalDivider(
            thickness = 1.dp,
            color = FestabookColor.gray300,
            modifier =
                Modifier
                    .padding(start = festabookSpacing.paddingScreenGutter + festabookSpacing.paddingBody4),
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(festabookSpacing.paddingBody5),
            contentPadding = PaddingValues(vertical = festabookSpacing.paddingBody5),
        ) {
            items(items = scheduleEvents, key = { scheduleEvent -> scheduleEvent.id }) {
                ScheduleEventItem(scheduleEvent = it)
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ScheduleTabContentPreview() {
    FestabookTheme {
        ScheduleTabContent(
            scheduleEvents =
                listOf(
                    ScheduleEventUiModel(
                        id = 1,
                        status = ScheduleEventUiStatus.ONGOING,
                        startTime = "9:00",
                        endTime = "18:00",
                        title = "동아리 버스킹 공연",
                        location = "운동장",
                    ),
                    ScheduleEventUiModel(
                        id = 2,
                        status = ScheduleEventUiStatus.UPCOMING,
                        startTime = "9:00",
                        endTime = "18:00",
                        title = "동아리 버스킹 공연 동아리 버스킹 공연 동아리 버스킹 공연",
                        location = "운동장",
                    ),
                    ScheduleEventUiModel(
                        id = 3,
                        status = ScheduleEventUiStatus.COMPLETED,
                        startTime = "9:00",
                        endTime = "18:00",
                        title = "동아리 버스킹 공연",
                        location = "운동장",
                    ),
                ),
        )
    }
}
