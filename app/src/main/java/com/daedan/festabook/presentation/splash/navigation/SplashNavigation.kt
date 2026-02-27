package com.daedan.festabook.presentation.splash.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.presentation.main.FestabookRoute
import com.daedan.festabook.presentation.splash.AppVersionManager
import com.daedan.festabook.presentation.splash.SplashViewModel
import com.daedan.festabook.presentation.splash.component.SplashScreen

fun NavGraphBuilder.splashNavGraph(
    viewModel: SplashViewModel,
    appVersionManager: AppVersionManager,
    onNavigateToExplore: () -> Unit,
    onNavigateToMain: (Long) -> Unit,
    onFinishApp: () -> Unit,
) {
    composable<FestabookRoute.Splash> {
        SplashScreen(
            viewModel = viewModel,
            appVersionManager = appVersionManager,
            onNavigateToExplore = onNavigateToExplore,
            onNavigateToMain = onNavigateToMain,
            onFinishApp = onFinishApp,
        )
    }
}
