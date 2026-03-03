package com.daedan.festabook.presentation.explore.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.di.FestaBookAppGraph
import com.daedan.festabook.presentation.explore.component.ExploreScreen
import com.daedan.festabook.presentation.main.FestabookRoute

fun NavGraphBuilder.exploreNavGraph(
    appGraph: FestaBookAppGraph,
    onBackClick: () -> Unit,
    onNavigateToMain: () -> Unit,
) {
    composable<FestabookRoute.Explore> {
        ExploreScreen(
            viewModel = viewModel(factory = appGraph.metroViewModelFactory),
            onBackClick = onBackClick,
            onNavigateToMain = { onNavigateToMain() },
        )
    }
}
