package com.daedan.festabook.presentation.main.component

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.NotificationPermissionManager
import com.daedan.festabook.presentation.home.HomeViewModel
import com.daedan.festabook.presentation.home.navigation.homeNavGraph
import com.daedan.festabook.presentation.main.FestabookRoute
import com.daedan.festabook.presentation.main.rememberFestabookNavigator
import com.daedan.festabook.presentation.news.NewsViewModel
import com.daedan.festabook.presentation.news.navigation.newsNavGraph
import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.intent.sideEffect.PlaceMapSideEffect
import com.daedan.festabook.presentation.placeMap.navigation.placeMapNavGraph
import com.daedan.festabook.presentation.schedule.ScheduleViewModel
import com.daedan.festabook.presentation.schedule.navigation.scheduleNavGraph
import com.daedan.festabook.presentation.setting.SettingViewModel
import com.daedan.festabook.presentation.setting.navigation.settingNavGraph
import com.naver.maps.map.util.FusedLocationSource

@Composable
fun MainScreen(
    notificationPermissionManager: NotificationPermissionManager,
    logger: DefaultFirebaseLogger,
    locationSource: FusedLocationSource,
    onPreloadImages: (PlaceMapSideEffect.PreloadImages) -> Unit, // TODO: 추후 Context에 의존적이지 않게 변경
    onNavigateToExplore: () -> Unit, // TODO 검색화면 마이그레이션 시 제거
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    placeMapViewModel: PlaceMapViewModel = viewModel(),
    newsViewModel: NewsViewModel = viewModel(),
    settingViewModel: SettingViewModel = viewModel(),
) {
    val navigator = rememberFestabookNavigator()

    Scaffold(
        // TODO: 스낵바 구현 및 하위 프래그먼트에 해당 SnackBar 적용
        bottomBar = {
            if (navigator.shouldShowBottomBar) {
                FestabookBottomNavigationBar(
                    currentTab = navigator.currentTab,
                    onTabSelect = { navigator.navigateToMainTab(it.route) },
                )
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        NavHost(
            startDestination = navigator.startRoute,
            navController = navigator.navController,
        ) {
            homeNavGraph(
                padding = innerPadding,
                viewModel = homeViewModel,
                onNavigateToExplore = onNavigateToExplore,
            )
            scheduleNavGraph(
                padding = innerPadding,
                viewModel = scheduleViewModel,
            )
            placeMapNavGraph(
                padding = innerPadding,
                placeMapViewModel = placeMapViewModel,
                logger = logger,
                locationSource = locationSource,
                onStartPlaceDetail = { navigator.navigate(FestabookRoute.PlaceDetail) },
                onPreloadImages = onPreloadImages,
                onShowErrorSnackBar = { },
            )
            newsNavGraph(
                padding = innerPadding,
                viewModel = newsViewModel,
            )
            settingNavGraph(
                padding = innerPadding,
                homeViewModel = homeViewModel,
                settingViewModel = settingViewModel,
                notificationPermissionManager = notificationPermissionManager,
                onShowSnackBar = { },
                onShowErrorSnackBar = { },
            )
        }
    }
}
