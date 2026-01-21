package com.daedan.festabook.presentation.home.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.presentation.home.HomeViewModel
import com.daedan.festabook.presentation.home.component.HomeScreen
import com.daedan.festabook.presentation.main.MainTabRoute

fun NavGraphBuilder.homeNavGraph(
    padding: PaddingValues,
    viewModel: HomeViewModel,
    onNavigateToExplore: () -> Unit,
) {
    composable<MainTabRoute.Home> {
        HomeScreen(
            modifier = Modifier.padding(padding),
            viewModel = viewModel,
            onNavigateToExplore = onNavigateToExplore,
        )
    }
}
