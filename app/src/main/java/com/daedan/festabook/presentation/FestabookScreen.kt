package com.daedan.festabook.presentation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import com.daedan.festabook.presentation.explore.navigation.exploreNavGraph
import com.daedan.festabook.presentation.main.FestabookRoute
import com.daedan.festabook.presentation.main.MainViewModel
import com.daedan.festabook.presentation.main.navigation.mainNavGraph
import com.daedan.festabook.presentation.main.rememberFestabookNavigator
import com.daedan.festabook.presentation.news.NewsViewModel
import com.daedan.festabook.presentation.platform.DeepLinkKeys
import com.daedan.festabook.presentation.platform.RememberDeepLinkHandler
import com.daedan.festabook.presentation.platform.rememberAppGraph
import com.daedan.festabook.presentation.platform.rememberAppVersionManager
import com.daedan.festabook.presentation.platform.rememberLocationSource
import com.daedan.festabook.presentation.splash.SplashViewModel
import com.daedan.festabook.presentation.splash.navigation.splashNavGraph

@Composable
fun FestabookScreen(
    modifier: Modifier = Modifier,
    newsViewModel: NewsViewModel = viewModel(),
    splashViewModel: SplashViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel(),
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

    RememberDeepLinkHandler { intent ->
        handleNavigation(intent, newsViewModel, mainViewModel)
    }

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
            onFinishApp = { festabookNavigator.popBackStack() },
        )
        exploreNavGraph(
            appGraph = appGraph,
            onBackClick = { festabookNavigator.popBackStack() },
            onNavigateToMain = { festabookNavigator.navigate(FestabookRoute.Main) },
        )
        mainNavGraph(
            appGraph = appGraph,
            locationSource = locationSource,
            festabookNavigator = festabookNavigator,
        )
    }
}

private fun handleNavigation(
    intent: Intent,
    newsViewModel: NewsViewModel,
    mainViewModel: MainViewModel,
) {
    val noticeIdToExpand =
        intent.getLongExtra(DeepLinkKeys.KEY_NOTICE_ID_TO_EXPAND, DeepLinkKeys.INITIALIZED_ID)
    if (noticeIdToExpand != DeepLinkKeys.INITIALIZED_ID) newsViewModel.expandNotice(noticeIdToExpand)
    val canNavigateToNews = intent.getBooleanExtra(DeepLinkKeys.KEY_CAN_NAVIGATE_TO_NEWS, false)
    if (canNavigateToNews) mainViewModel.navigateToNews()
}
