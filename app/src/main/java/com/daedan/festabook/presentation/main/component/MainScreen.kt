package com.daedan.festabook.presentation.main.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import com.daedan.festabook.R
import com.daedan.festabook.di.FestaBookAppGraph
import com.daedan.festabook.presentation.NotificationPermissionManager
import com.daedan.festabook.presentation.common.ObserveAsEvents
import com.daedan.festabook.presentation.common.component.FestabookSnackbar
import com.daedan.festabook.presentation.common.component.SnackbarManager
import com.daedan.festabook.presentation.common.component.rememberAppSnackbarManager
import com.daedan.festabook.presentation.home.HomeViewModel
import com.daedan.festabook.presentation.home.navigation.homeNavGraph
import com.daedan.festabook.presentation.main.FestabookMainTab
import com.daedan.festabook.presentation.main.FestabookNavigator
import com.daedan.festabook.presentation.main.FestabookRoute
import com.daedan.festabook.presentation.main.MainTabRoute
import com.daedan.festabook.presentation.main.MainViewModel
import com.daedan.festabook.presentation.main.rememberFestabookNavigator
import com.daedan.festabook.presentation.news.NewsViewModel
import com.daedan.festabook.presentation.news.navigation.newsNavGraph
import com.daedan.festabook.presentation.placeDetail.PlaceDetailViewModel
import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.component.PlaceMapRoute
import com.daedan.festabook.presentation.placeMap.intent.event.SelectEvent
import com.daedan.festabook.presentation.placeMap.navigation.placeMapNavGraph
import com.daedan.festabook.presentation.platform.rememberNotificationPermissionManager
import com.daedan.festabook.presentation.platform.rememberOpenAppSettings
import com.daedan.festabook.presentation.schedule.ScheduleViewModel
import com.daedan.festabook.presentation.schedule.navigation.scheduleNavGraph
import com.daedan.festabook.presentation.setting.SettingViewModel
import com.daedan.festabook.presentation.setting.navigation.settingNavGraph
import com.naver.maps.map.util.FusedLocationSource

@Composable
@Suppress("ktlint:compose:vm-forwarding-check")
fun MainScreen(
    appGraph: FestaBookAppGraph,
    locationSource: FusedLocationSource,
    onAppFinish: () -> Unit,
    festabookNavigator: FestabookNavigator,
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    scheduleViewModel: ScheduleViewModel,
    placeMapViewModel: PlaceMapViewModel,
    newsViewModel: NewsViewModel,
    settingViewModel: SettingViewModel,
    modifier: Modifier = Modifier,
) {
    val mainNavigator = rememberFestabookNavigator(MainTabRoute.Home)
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarManager = rememberAppSnackbarManager(snackbarHostState)
    val backPressExitMessage = stringResource(R.string.back_press_exit_message)
    val openAppSettings = rememberOpenAppSettings()

    val notificationPermissionManager =
        rememberNotificationPermissionManager(
            factory = appGraph.notificationPermissionManagerFactory,
            onPermissionGrant = { settingViewModel.saveNotificationId() },
            onPermissionDeny = { snackbarManager.showPermissionDeniedSnackbar(openAppSettings) },
        )

    ObserveAsEvents(flow = mainViewModel.navigateNewsEvent) {
        mainNavigator.navigateToMainTab(FestabookMainTab.NEWS)
    }
    ObserveAsEvents(flow = mainViewModel.backPressEvent) { isDoublePress ->
        if (isDoublePress) {
            onAppFinish()
        } else {
            snackbarManager.show(backPressExitMessage)
        }
    }
    ObserveAsEvents(flow = homeViewModel.navigateToScheduleEvent) {
        mainNavigator.navigateToMainTab(FestabookMainTab.SCHEDULE)
    }

    LaunchedEffect(Unit) {
        mainViewModel.registerDeviceAndFcmToken()
    }

    BackHandler {
        mainViewModel.onBackPressed()
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                FestabookSnackbar(data)
            }
        },
        bottomBar = {
            if (mainNavigator.shouldShowBottomBar) {
                FestabookBottomNavigationBar(
                    currentTab = mainNavigator.currentTab,
                    onTabSelect = { mainNavigator.navigateToMainTab(it) },
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
        val isVisible = mainNavigator.currentTab == FestabookMainTab.PLACE_MAP
        PlaceMapRoute(
            modifier =
                Modifier
                    .graphicsLayer {
                        alpha = if (isVisible) 1f else 0f
                    }.padding(innerPadding)
                    .pointerInput(isVisible) {
                        if (!isVisible) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent(PointerEventPass.Initial)
                                        .changes
                                        .forEach { it.consume() }
                                }
                            }
                        }
                    },
            placeMapViewModel = placeMapViewModel,
            locationSource = locationSource,
            logger = appGraph.defaultFirebaseLogger,
            onShowErrorSnackBar = snackbarManager::showError,
            onStartPlaceDetail = {
                mainNavigator.navigate(
                    FestabookRoute.PlaceDetail(
                        placeDetailUiModel = it.placeDetail.value,
                    ),
                )
            },
        )
        FestabookNavHost(
            modifier = Modifier.padding(innerPadding),
            festabookNavigator = festabookNavigator,
            navigator = mainNavigator,
            mainViewModel = mainViewModel,
            homeViewModel = homeViewModel,
            scheduleViewModel = scheduleViewModel,
            settingViewModel = settingViewModel,
            placeDetailViewModelFactory = appGraph.placeDetailViewModelFactory,
            newsViewModel = newsViewModel,
            notificationPermissionManager = notificationPermissionManager,
            snackbarManager = snackbarManager,
        )
    }
}

@Composable
private fun FestabookNavHost(
    navigator: FestabookNavigator,
    festabookNavigator: FestabookNavigator,
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    scheduleViewModel: ScheduleViewModel,
    placeDetailViewModelFactory: PlaceDetailViewModel.Factory,
    newsViewModel: NewsViewModel,
    settingViewModel: SettingViewModel,
    notificationPermissionManager: NotificationPermissionManager,
    snackbarManager: SnackbarManager,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        startDestination = navigator.startRoute,
        navController = navigator.navController,
    ) {
        homeNavGraph(
            viewModel = homeViewModel,
            mainViewModel = mainViewModel,
            onNavigateToExplore = { festabookNavigator.navigate(FestabookRoute.Explore) },
            onSubscriptionConfirm = { settingViewModel.notificationAllowClick() },
            onShowSnackbar = snackbarManager::show,
            onShowErrorSnackbar = snackbarManager::showError,
            settingViewModel = settingViewModel,
            notificationPermissionManager = notificationPermissionManager,
        )
        scheduleNavGraph(
            viewModel = scheduleViewModel,
            onShowErrorSnackbar = snackbarManager::showError,
        )
        placeMapNavGraph(
            placeDetailViewModelFactory = placeDetailViewModelFactory,
            onBackToPreviousClick = { navigator.popBackStack() },
            onShowErrorSnackbar = snackbarManager::showError,
        )
        newsNavGraph(
            viewModel = newsViewModel,
            onShowErrorSnackbar = snackbarManager::showError,
        )
        settingNavGraph(
            homeViewModel = homeViewModel,
            settingViewModel = settingViewModel,
            notificationPermissionManager = notificationPermissionManager,
            onShowSnackBar = snackbarManager::show,
            onShowErrorSnackBar = snackbarManager::showError,
        )
    }
}
