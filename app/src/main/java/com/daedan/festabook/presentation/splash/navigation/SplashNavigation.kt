package com.daedan.festabook.presentation.splash.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.di.FestaBookAppGraph
import com.daedan.festabook.presentation.main.FestabookRoute
import com.daedan.festabook.presentation.splash.AppVersionManager
import com.daedan.festabook.presentation.splash.component.SplashScreen

fun NavGraphBuilder.splashNavGraph(
    appGraph: FestaBookAppGraph,
    appVersionManager: AppVersionManager,
    onNavigateToExplore: () -> Unit,
    onNavigateToMain: (Long) -> Unit,
    onFinishApp: () -> Unit,
) {
    composable<FestabookRoute.Splash> {
        SplashScreen(
            viewModel = viewModel(factory = appGraph.metroViewModelFactory),
            appVersionManager = appVersionManager,
            onNavigateToExplore = onNavigateToExplore,
            onNavigateToMain = onNavigateToMain,
            onFinishApp = onFinishApp,
        )
    }
}
