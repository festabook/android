package com.daedan.festabook.presentation.schedule.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.presentation.main.MainTabRoute
import com.daedan.festabook.presentation.schedule.ScheduleViewModel
import com.daedan.festabook.presentation.schedule.component.ScheduleScreen

fun NavGraphBuilder.scheduleNavGraph(
    padding: PaddingValues,
    viewModel: ScheduleViewModel,
) {
    composable<MainTabRoute.Schedule> {
        ScheduleScreen(
            modifier = Modifier.padding(padding),
            scheduleViewModel = viewModel,
        )
    }
}
