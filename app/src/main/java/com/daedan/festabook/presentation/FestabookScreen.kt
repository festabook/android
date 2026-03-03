package com.daedan.festabook.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import com.daedan.festabook.presentation.explore.navigation.exploreNavGraph
import com.daedan.festabook.presentation.main.FestabookRoute
import com.daedan.festabook.presentation.main.navigation.mainNavGraph
import com.daedan.festabook.presentation.main.rememberFestabookNavigator
import com.daedan.festabook.presentation.platform.rememberAppGraph
import com.daedan.festabook.presentation.platform.rememberAppVersionManager
import com.daedan.festabook.presentation.platform.rememberLocationSource
import com.daedan.festabook.presentation.splash.SplashViewModel
import com.daedan.festabook.presentation.splash.navigation.splashNavGraph

@Composable
fun FestabookScreen(
    onAppFinish: () -> Unit,
    modifier: Modifier = Modifier,
    splashViewModel: SplashViewModel = viewModel(),
) {
    val appGraph = rememberAppGraph()
    val locationSource = rememberLocationSource()
    val festabookNavigator = rememberFestabookNavigator()

    val appVersionManager =
        rememberAppVersionManager(
            factory = appGraph.appVersionManagerFactory,
            onUpdateSuccess = { splashViewModel.handleVersionCheckResult(Result.success(false)) },
            onUpdateFailure = { splashViewModel.handleVersionCheckResult(Result.failure(Exception("Update failed"))) },
        )

    NavHost(
        modifier = modifier,
        startDestination = festabookNavigator.startRoute,
        navController = festabookNavigator.navController,
    ) {
        splashNavGraph(
            appGraph = appGraph,
            appVersionManager = appVersionManager,
            onNavigateToExplore = { festabookNavigator.navigate(FestabookRoute.Explore) },
            onNavigateToMain = { festabookNavigator.navigate(FestabookRoute.Main) },
            onFinishApp = onAppFinish,
        )
        exploreNavGraph(
            appGraph = appGraph,
            onBackClick = { festabookNavigator.popBackStack() },
            onNavigateToMain = { festabookNavigator.navigate(FestabookRoute.Main) },
        )
        mainNavGraph(
            appGraph = appGraph,
            onAppFinish = onAppFinish,
            locationSource = locationSource,
            festabookNavigator = festabookNavigator,
        )
    }
}
