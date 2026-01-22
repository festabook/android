package com.daedan.festabook.presentation.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.presentation.home.HomeViewModel
import com.daedan.festabook.presentation.home.component.HomeScreen
import com.daedan.festabook.presentation.main.MainTabRoute

fun NavGraphBuilder.homeNavGraph(
    viewModel: HomeViewModel,
    onNavigateToExplore: () -> Unit,
) {
    composable<MainTabRoute.Home> {
        HomeScreen(
            viewModel = viewModel,
            onNavigateToExplore = onNavigateToExplore,
        )
    }
}
