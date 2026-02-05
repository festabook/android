package com.daedan.festabook.presentation.schedule.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.ErrorStateScreen
import com.daedan.festabook.presentation.common.component.FestabookTopAppBar
import com.daedan.festabook.presentation.common.component.LoadingStateScreen
import com.daedan.festabook.presentation.schedule.ScheduleEventsUiState
import com.daedan.festabook.presentation.schedule.ScheduleUiState
import com.daedan.festabook.presentation.schedule.ScheduleViewModel
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.festabookSpacing
import timber.log.Timber

@Composable
fun ScheduleScreen(
    scheduleViewModel: ScheduleViewModel,
    modifier: Modifier = Modifier,
    onShowErrorSnackbar: (Throwable) -> Unit = {}, // TODO Fragment 제거 시 필수 파라미터로 변경
) {
    val scheduleUiState by scheduleViewModel.scheduleUiState.collectAsStateWithLifecycle()
    val currentOnShowErrorSnackbar by rememberUpdatedState(onShowErrorSnackbar)
    val currentState =
        when (scheduleUiState) {
            is ScheduleUiState.Refreshing -> (scheduleUiState as ScheduleUiState.Refreshing).lastSuccessState
            is ScheduleUiState.Success -> scheduleUiState
            else -> scheduleUiState
        }

    LaunchedEffect(currentState) {
        when (currentState) {
            is ScheduleUiState.Error -> {
                currentOnShowErrorSnackbar(currentState.throwable)
            }

            else -> {
                Unit
            }
        }
    }

    Scaffold(
        topBar = { FestabookTopAppBar(title = stringResource(R.string.schedule_title)) },
        modifier = modifier,
    ) { innerPadding ->
        when (currentState) {
            ScheduleUiState.InitialLoading -> {
                LoadingStateScreen()
            }

            is ScheduleUiState.Error -> {
                Timber.w(currentState.throwable.stackTraceToString())
                ErrorStateScreen()
            }

            else -> {
                val currentStateSuccess = currentState as ScheduleUiState.Success
                val pageState =
                    rememberPagerState(initialPage = currentStateSuccess.currentDatePosition) { currentStateSuccess.dates.size }
                val scope = rememberCoroutineScope()
                LaunchedEffect(pageState.currentPage) {
                    scheduleViewModel.loadEventsInRange(currentPosition = pageState.currentPage)
                }

                Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
                    ScheduleTabRow(
                        pageState = pageState,
                        scope = scope,
                        dates = currentStateSuccess.dates,
                    )
                    Spacer(modifier = Modifier.height(festabookSpacing.paddingBody4))
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = FestabookColor.gray300,
                        modifier = Modifier.padding(horizontal = festabookSpacing.paddingScreenGutter),
                    )
                    ScheduleTabPage(
                        pagerState = pageState,
                        scheduleUiState = currentStateSuccess,
                        onRefresh = { content ->
                            scheduleViewModel.loadSchedules(
                                scheduleUiState = ScheduleUiState.Refreshing(currentStateSuccess),
                                scheduleEventUiState =
                                    ScheduleEventsUiState(content = content, isRefreshing = true),
                                selectedDatePosition = pageState.currentPage,
                                preloadCount = 0,
                            )
                        },
                    )
                }
            }
        }
    }
}
