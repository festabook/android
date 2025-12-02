package com.daedan.festabook.presentation.schedule.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.FestabookTopAppBar
import com.daedan.festabook.presentation.schedule.ScheduleDatesUiState
import com.daedan.festabook.presentation.schedule.ScheduleViewModel
import com.daedan.festabook.presentation.schedule.model.ScheduleDateUiModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun ScheduleScreen(
    scheduleViewModel: ScheduleViewModel,
    modifier: Modifier = Modifier,
) {
    val scheduleDatesUiState by scheduleViewModel.scheduleDatesUiState.collectAsStateWithLifecycle()
    val scheduleEventsUiState by scheduleViewModel.scheduleEventsUiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { FestabookTopAppBar(title = stringResource(R.string.schedule_title)) },
        modifier = modifier,
    ) { innerPadding ->

        when (scheduleDatesUiState) {
            is ScheduleDatesUiState.Error -> {
                TODO()
            }

            ScheduleDatesUiState.Loading -> {
                TODO()
            }

            is ScheduleDatesUiState.Success -> {
                val scheduleDates = (scheduleDatesUiState as ScheduleDatesUiState.Success).dates
                val pageState = rememberPagerState { scheduleDates.size }
                val scope = rememberCoroutineScope()

                Column(modifier = Modifier.padding(innerPadding)) {
                    ScheduleTabRow(
                        pageState = pageState,
                        scope = scope,
                        scheduleDates = scheduleDates,
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleTabRow(
    pageState: PagerState,
    scope: CoroutineScope,
    scheduleDates: List<ScheduleDateUiModel>,
    modifier: Modifier = Modifier,
) {
    TabRow(selectedTabIndex = pageState.currentPage, modifier = modifier) {
        scheduleDates.forEachIndexed { index, scheduleDate ->
        }
    }
}

@Composable
fun ScheduleTabItem(
    scheduleDate: ScheduleDateUiModel,
    modifier: Modifier = Modifier,
) {
    Tab(
        selected = TODO(),
        onClick = TODO(),
        text = { Text(text = scheduleDate.date) },
    )
}

// @Composable
// @Preview
// private fun ScheduleScreenPreview() {
//    ScheduleScreen()
// }
