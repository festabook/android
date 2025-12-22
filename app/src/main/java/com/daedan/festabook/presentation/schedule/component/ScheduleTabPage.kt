package com.daedan.festabook.presentation.schedule.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.EmptyStateScreen
import com.daedan.festabook.presentation.common.component.LoadingStateScreen
import com.daedan.festabook.presentation.common.component.PULL_OFFSET_LIMIT
import com.daedan.festabook.presentation.common.component.PullToRefreshContainer
import com.daedan.festabook.presentation.schedule.ScheduleEventsUiState
import com.daedan.festabook.presentation.schedule.ScheduleUiState
import com.daedan.festabook.presentation.schedule.ScheduleUiState.Companion.DEFAULT_POSITION
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiModel
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiStatus
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.festabookSpacing
import timber.log.Timber

private const val PRELOAD_PAGE_COUNT: Int = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTabPage(
    pagerState: PagerState,
    scheduleUiState: ScheduleUiState.Success,
    onRefresh: (List<ScheduleEventUiModel>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.pulse_circle))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        beyondViewportPageCount = PRELOAD_PAGE_COUNT,
    ) { index ->
        val scheduleEventsUiState = scheduleUiState.eventsUiStateByPosition[index]
        val isRefreshing = scheduleEventsUiState is ScheduleEventsUiState.Refreshing
        val oldEvents =
            (scheduleEventsUiState as? ScheduleEventsUiState.Success)?.events ?: emptyList()

        PullToRefreshContainer(
            isRefreshing = isRefreshing,
            onRefresh = { onRefresh(oldEvents) },
        ) { pullToRefreshState ->
            when (scheduleEventsUiState) {
                is ScheduleEventsUiState.Error -> {
                    Timber.w(scheduleEventsUiState.throwable.stackTraceToString())
                }

                ScheduleEventsUiState.InitialLoading -> {
                    LoadingStateScreen()
                }

                is ScheduleEventsUiState.Refreshing -> {
                    ScheduleTabContent(
                        composition = composition,
                        progress = progress,
                        scheduleEvents = scheduleEventsUiState.oldEvents,
                        modifier =
                            Modifier
                                .padding(end = festabookSpacing.paddingScreenGutter)
                                .graphicsLayer {
                                    translationY =
                                        pullToRefreshState.distanceFraction * PULL_OFFSET_LIMIT
                                },
                    )
                }

                is ScheduleEventsUiState.Success -> {
                    ScheduleTabContent(
                        composition = composition,
                        progress = progress,
                        scheduleEvents = scheduleEventsUiState.events,
                        currentEventPosition = scheduleEventsUiState.currentEventPosition,
                        modifier =
                            Modifier
                                .padding(end = festabookSpacing.paddingScreenGutter)
                                .graphicsLayer {
                                    translationY =
                                        pullToRefreshState.distanceFraction * PULL_OFFSET_LIMIT
                                },
                    )
                }

                null -> {}
            }
        }
    }
}

@Composable
private fun ScheduleTabContent(
    composition: LottieComposition?,
    progress: Float,
    scheduleEvents: List<ScheduleEventUiModel>,
    modifier: Modifier = Modifier,
    currentEventPosition: Int = DEFAULT_POSITION,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.animateScrollToItem(currentEventPosition)
    }
    if (scheduleEvents.isEmpty()) {
        EmptyStateScreen(modifier = modifier)
    } else {
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
                state = listState,
            ) {
                items(items = scheduleEvents, key = { scheduleEvent -> scheduleEvent.id }) {
                    ScheduleEventItem(
                        composition = composition,
                        progress = progress,
                        scheduleEvent = it,
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ScheduleTabContentPreview() {
    FestabookTheme {
        ScheduleTabContent(
            composition = null,
            progress = 1f,
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
