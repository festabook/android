package com.daedan.festabook.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.explore.ExploreViewModel
import com.daedan.festabook.presentation.explore.navigation.exploreNavGraph
import com.daedan.festabook.presentation.main.FestabookRoute
import com.daedan.festabook.presentation.main.navigation.mainNavGraph
import com.daedan.festabook.presentation.main.rememberFestabookNavigator
import com.daedan.festabook.presentation.placeDetail.PlaceDetailViewModel
import com.daedan.festabook.presentation.splash.AppVersionManager
import com.daedan.festabook.presentation.splash.SplashViewModel
import com.daedan.festabook.presentation.splash.navigation.splashNavGraph
import com.naver.maps.map.util.FusedLocationSource

@Composable
fun FestabookScreen(
    appVersionManager: AppVersionManager,
    notificationPermissionManager: NotificationPermissionManager,
    placeDetailViewModelFactory: PlaceDetailViewModel.Factory,
    defaultViewModelFactory: ViewModelProvider.Factory,
    locationSource: FusedLocationSource,
    logger: DefaultFirebaseLogger,
    modifier: Modifier = Modifier,
    splashViewModel: SplashViewModel = viewModel(),
    exploreViewModel: ExploreViewModel = viewModel(),
) {
    val festabookNavigator = rememberFestabookNavigator()

    NavHost(
        modifier = modifier,
        startDestination = festabookNavigator.startRoute,
        navController = festabookNavigator.navController,
    ) {
        splashNavGraph(
            viewModel = splashViewModel,
            appVersionManager = appVersionManager,
            onNavigateToExplore = { festabookNavigator.navigate(FestabookRoute.Explore) },
            onNavigateToMain = { festabookNavigator.navigate(FestabookRoute.Main) },
            onFinishApp = { festabookNavigator.popBackStack() },
        )
        exploreNavGraph(
            viewModel = exploreViewModel,
            onBackClick = { festabookNavigator.popBackStack() },
            onNavigateToMain = { festabookNavigator.navigate(FestabookRoute.Main) },
        )
        mainNavGraph(
            placeDetailViewModelFactory = placeDetailViewModelFactory,
            defaultViewModelFactory = defaultViewModelFactory,
            notificationPermissionManager = notificationPermissionManager,
            locationSource = locationSource,
            logger = logger,
            onSubscriptionConfirm = { festabookNavigator.navigate(FestabookRoute.Main) },
            festabookNavigator = festabookNavigator,
        )
    }
}
