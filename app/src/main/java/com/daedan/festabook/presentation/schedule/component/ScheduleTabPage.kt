package com.daedan.festabook.presentation.schedule.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.EmptyStateScreen
import com.daedan.festabook.presentation.common.component.ErrorStateScreen
import com.daedan.festabook.presentation.common.component.LoadingStateScreen
import com.daedan.festabook.presentation.common.component.PullToRefreshContainer
import com.daedan.festabook.presentation.schedule.ScheduleEventsUiState
import com.daedan.festabook.presentation.schedule.ScheduleUiState
import com.daedan.festabook.presentation.schedule.ScheduleUiState.Companion.DEFAULT_POSITION
import com.daedan.festabook.presentation.schedule.ScheduleViewModel.Companion.PRELOAD_PAGE_COUNT
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiModel
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiStatus
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.festabookSpacing
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTabPage(
    pagerState: PagerState,
    scheduleContent: ScheduleUiState.Content.Success,
    onRefresh: (ScheduleEventsUiState.Content) -> Unit,
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.pulse_circle))
    val scrollState = rememberScrollState()
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        beyondViewportPageCount = PRELOAD_PAGE_COUNT,
    ) { index ->
        val scheduleEventsUiState =
            scheduleContent.eventsUiStateByPosition[index] ?: return@HorizontalPager

        PullToRefreshContainer(
            isRefreshing = scheduleEventsUiState.isRefreshing,
            onRefresh = { onRefresh(scheduleEventsUiState.content) },
        ) { graphicsLayer ->
            when (val content = scheduleEventsUiState.content) {
                is ScheduleEventsUiState.Content.Error -> {
                    Timber.w(content.throwable.stackTraceToString())
                    ErrorStateScreen(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(end = festabookSpacing.paddingScreenGutter)
                                .then(graphicsLayer)
                                .verticalScroll(scrollState),
                    )
                }

                is ScheduleEventsUiState.Content.InitialLoading -> {
                    LoadingStateScreen()
                }

                is ScheduleEventsUiState.Content.Success -> {
                    ScheduleTabContent(
                        scrollState = scrollState,
                        composition = composition,
                        scheduleEventsContent = content,
                        currentEventPosition = content.currentEventPosition,
                        modifier =
                            Modifier
                                .padding(end = festabookSpacing.paddingScreenGutter)
                                .then(graphicsLayer),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleTabContent(
    scrollState: ScrollState,
    composition: LottieComposition?,
    scheduleEventsContent: ScheduleEventsUiState.Content.Success,
    modifier: Modifier = Modifier,
    currentEventPosition: Int = DEFAULT_POSITION,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.animateScrollToItem(currentEventPosition)
    }
    if (scheduleEventsContent.isEventsEmpty) {
        EmptyStateScreen(
            modifier =
                modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
        )
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
                items(
                    items = scheduleEventsContent.events,
                    key = { scheduleEvent -> scheduleEvent.id },
                ) {
                    ScheduleEventItem(
                        composition = composition,
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
            scrollState = rememberScrollState(),
            composition = null,
            scheduleEventsContent =
                ScheduleEventsUiState.Content.Success(
                    events =
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
                    currentEventPosition = 0,
                ),
        )
    }
}
