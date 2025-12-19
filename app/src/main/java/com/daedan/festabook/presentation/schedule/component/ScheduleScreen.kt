package com.daedan.festabook.presentation.schedule.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.FestabookTopAppBar
import com.daedan.festabook.presentation.common.component.LoadingStateScreen
import com.daedan.festabook.presentation.schedule.ScheduleEventsUiState
import com.daedan.festabook.presentation.schedule.ScheduleUiState
import com.daedan.festabook.presentation.schedule.ScheduleViewModel
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun ScheduleScreen(
    scheduleViewModel: ScheduleViewModel,
    modifier: Modifier = Modifier,
) {
    val scheduleUiState by scheduleViewModel.scheduleUiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { FestabookTopAppBar(title = stringResource(R.string.schedule_title)) },
        modifier = modifier,
    ) { innerPadding ->

        when (scheduleUiState) {
            is ScheduleUiState.Error -> {
            }

            ScheduleUiState.InitialLoading -> {
                LoadingStateScreen()
            }

            is ScheduleUiState.Success -> {
                val scheduleUiStateSuccess =
                    scheduleUiState as ScheduleUiState.Success
                val pageState =
                    rememberPagerState(
                        initialPage = scheduleUiStateSuccess.currentDatePosition,
                    ) { scheduleUiStateSuccess.dates.size }
                val scope = rememberCoroutineScope()

                val scheduleEventsUiState =
                    scheduleUiStateSuccess.eventsUiStateByPosition[pageState.currentPage]
                        ?: ScheduleEventsUiState.Error(IllegalArgumentException())
                val isRefreshing = scheduleEventsUiState is ScheduleEventsUiState.Refreshing

                Column(
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                ) {
                    ScheduleTabRow(
                        pageState = pageState,
                        scope = scope,
                        dates = scheduleUiStateSuccess.dates,
                    )
                    Spacer(modifier = Modifier.height(festabookSpacing.paddingBody4))
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = FestabookColor.gray300,
                        modifier = Modifier.padding(horizontal = festabookSpacing.paddingScreenGutter),
                    )
                    ScheduleTabPage(
                        pagerState = pageState,
                        scheduleEventsUiState = scheduleEventsUiState,
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            val oldEvents =
                                (scheduleEventsUiState as? ScheduleEventsUiState.Success)?.events
                                    ?: emptyList()
                            scheduleViewModel.loadSchedules(
                                scheduleEventUiState = ScheduleEventsUiState.Refreshing(oldEvents),
                                selectedDatePosition = pageState.currentPage,
                            )
                        },
                    )
                }
            }
        }
    }
}
