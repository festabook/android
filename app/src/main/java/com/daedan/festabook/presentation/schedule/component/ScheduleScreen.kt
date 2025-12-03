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
import com.daedan.festabook.presentation.schedule.ScheduleDatesUiState
import com.daedan.festabook.presentation.schedule.ScheduleViewModel
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.festabookSpacing
import timber.log.Timber

@Composable
fun ScheduleScreen(
    scheduleViewModel: ScheduleViewModel,
    modifier: Modifier = Modifier,
) {
    val scheduleDatesUiState by scheduleViewModel.scheduleDatesUiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { FestabookTopAppBar(title = stringResource(R.string.schedule_title)) },
        modifier = modifier,
    ) { innerPadding ->

        when (scheduleDatesUiState) {
            is ScheduleDatesUiState.Error -> {
            }

            ScheduleDatesUiState.InitialLoading -> {
            }

            is ScheduleDatesUiState.Success -> {
                Timber.d("Success호출")
                val scheduleDates = (scheduleDatesUiState as ScheduleDatesUiState.Success).dates
                val pageState = rememberPagerState { scheduleDates.size }
                val scope = rememberCoroutineScope()

                Column(
                    modifier =
                        Modifier
                            .padding(innerPadding),
                ) {
                    ScheduleTabRow(
                        pageState = pageState,
                        scope = scope,
                        scheduleDates = scheduleDates,
                    )
                    Spacer(modifier = Modifier.height(festabookSpacing.paddingBody4))
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = FestabookColor.gray300,
                        modifier = Modifier.padding(horizontal = festabookSpacing.paddingScreenGutter),
                    )
                }
            }
        }
    }
}
