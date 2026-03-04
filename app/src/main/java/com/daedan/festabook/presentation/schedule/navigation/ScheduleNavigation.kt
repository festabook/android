package com.daedan.festabook.presentation.schedule.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.presentation.main.MainTabRoute
import com.daedan.festabook.presentation.schedule.ScheduleViewModel
import com.daedan.festabook.presentation.schedule.component.ScheduleScreen

fun NavGraphBuilder.scheduleNavGraph(
    viewModel: ScheduleViewModel,
    onShowErrorSnackbar: (Throwable) -> Unit,
) {
    composable<MainTabRoute.Schedule> {
        ScheduleScreen(
            scheduleViewModel = viewModel,
            onShowErrorSnackbar = onShowErrorSnackbar,
        )
    }
}
