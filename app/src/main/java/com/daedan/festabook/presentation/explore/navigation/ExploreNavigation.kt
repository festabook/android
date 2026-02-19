package com.daedan.festabook.presentation.explore.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.presentation.explore.ExploreViewModel
import com.daedan.festabook.presentation.explore.component.ExploreScreen
import com.daedan.festabook.presentation.main.FestabookRoute

fun NavGraphBuilder.exploreNavGraph(
    viewModel: ExploreViewModel,
    onBackClick: () -> Unit,
    onNavigateToMain: () -> Unit,
) {
    composable<FestabookRoute.Explore> {
        ExploreScreen(
            viewModel = viewModel,
            onBackClick = onBackClick,
            onNavigateToMain = { onNavigateToMain() },
        )
    }
}
