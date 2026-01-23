package com.daedan.festabook.presentation.home.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.presentation.home.HomeViewModel
import com.daedan.festabook.presentation.home.component.HomeScreen
import com.daedan.festabook.presentation.main.MainTabRoute
import com.daedan.festabook.presentation.main.MainViewModel
import com.daedan.festabook.presentation.main.component.FirstVisitDialog

fun NavGraphBuilder.homeNavGraph(
    viewModel: HomeViewModel,
    mainViewModel: MainViewModel,
    onSubscriptionConfirm: () -> Unit,
    onNavigateToExplore: () -> Unit,
) {
    composable<MainTabRoute.Home> {
        val isFirstVisit by mainViewModel.isFirstVisit.collectAsStateWithLifecycle()
        if (isFirstVisit) {
            FirstVisitDialog(
                onConfirm = { onSubscriptionConfirm() },
                onDecline = { mainViewModel.declineAlert() },
            )
        }
        HomeScreen(
            viewModel = viewModel,
            onNavigateToExplore = onNavigateToExplore,
        )
    }
}
