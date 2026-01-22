package com.daedan.festabook.presentation.main.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.NotificationPermissionManager
import com.daedan.festabook.presentation.home.HomeViewModel
import com.daedan.festabook.presentation.home.navigation.homeNavGraph
import com.daedan.festabook.presentation.main.FestabookMainTab
import com.daedan.festabook.presentation.main.FestabookNavigator
import com.daedan.festabook.presentation.main.FestabookRoute
import com.daedan.festabook.presentation.main.rememberFestabookNavigator
import com.daedan.festabook.presentation.news.NewsViewModel
import com.daedan.festabook.presentation.news.navigation.newsNavGraph
import com.daedan.festabook.presentation.placeDetail.PlaceDetailViewModel
import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.component.PlaceMapRoute
import com.daedan.festabook.presentation.placeMap.intent.event.SelectEvent
import com.daedan.festabook.presentation.placeMap.intent.sideEffect.PlaceMapSideEffect
import com.daedan.festabook.presentation.placeMap.navigation.placeMapNavGraph
import com.daedan.festabook.presentation.schedule.ScheduleViewModel
import com.daedan.festabook.presentation.schedule.navigation.scheduleNavGraph
import com.daedan.festabook.presentation.setting.SettingViewModel
import com.daedan.festabook.presentation.setting.navigation.settingNavGraph
import com.naver.maps.map.util.FusedLocationSource

@Composable
@Suppress("ktlint:compose:vm-forwarding-check")
fun MainScreen(
    notificationPermissionManager: NotificationPermissionManager,
    logger: DefaultFirebaseLogger,
    locationSource: FusedLocationSource,
    placeDetailViewModelFactory: PlaceDetailViewModel.Factory,
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
                    onTabSelect = { navigator.navigateToMainTab(it) },
                    onTabReSelect = { tab ->
                        when (tab) {
                            FestabookMainTab.SCHEDULE -> {
                                scheduleViewModel.loadSchedules()
                            }

                            FestabookMainTab.PLACE_MAP -> {
                                placeMapViewModel.onPlaceMapEvent(SelectEvent.UnSelectPlace)
                                placeMapViewModel.onMenuItemReClicked()
                            }

                            else -> {
                                Unit
                            }
                        }
                    },
                )
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        PlaceMapRoute(
            modifier =
                Modifier
                    .alpha(
                        if (navigator.currentTab == FestabookMainTab.PLACE_MAP) 1f else 0f,
                    ).padding(innerPadding),
            placeMapViewModel = placeMapViewModel,
            locationSource = locationSource,
            logger = logger,
            onStartPlaceDetail = {
                navigator.navigate(
                    FestabookRoute.PlaceDetail(
                        placeDetailUiModel = it.placeDetail.value,
                    ),
                )
            },
            onPreloadImages = onPreloadImages,
        )
        FestabookNavHost(
            modifier = Modifier.padding(innerPadding),
            navigator = navigator,
            homeViewModel = homeViewModel,
            scheduleViewModel = scheduleViewModel,
            placeDetailViewModelFactory = placeDetailViewModelFactory,
            newsViewModel = newsViewModel,
            settingViewModel = settingViewModel,
            notificationPermissionManager = notificationPermissionManager,
            onNavigateToExplore = onNavigateToExplore,
        )
    }
}

@Composable
private fun FestabookNavHost(
    navigator: FestabookNavigator,
    homeViewModel: HomeViewModel,
    scheduleViewModel: ScheduleViewModel,
    placeDetailViewModelFactory: PlaceDetailViewModel.Factory,
    newsViewModel: NewsViewModel,
    settingViewModel: SettingViewModel,
    notificationPermissionManager: NotificationPermissionManager,
    onNavigateToExplore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        startDestination = navigator.startRoute,
        navController = navigator.navController,
    ) {
        homeNavGraph(
            viewModel = homeViewModel,
            onNavigateToExplore = onNavigateToExplore,
        )
        scheduleNavGraph(
            viewModel = scheduleViewModel,
        )
        placeMapNavGraph(
            placeDetailViewModelFactory = placeDetailViewModelFactory,
            onBackToPreviousClick = { navigator.popBackStack() },
        )
        newsNavGraph(
            viewModel = newsViewModel,
        )
        settingNavGraph(
            homeViewModel = homeViewModel,
            settingViewModel = settingViewModel,
            notificationPermissionManager = notificationPermissionManager,
            onShowSnackBar = { },
            onShowErrorSnackBar = { },
        )
    }
}
