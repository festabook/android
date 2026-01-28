package com.daedan.festabook.presentation.main.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import com.daedan.festabook.R
import com.daedan.festabook.logging.DefaultFirebaseLogger
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
import com.daedan.festabook.presentation.main.MainViewModel
import com.daedan.festabook.presentation.main.rememberFestabookNavigator
import com.daedan.festabook.presentation.news.NewsViewModel
import com.daedan.festabook.presentation.news.navigation.newsNavGraph
import com.daedan.festabook.presentation.placeDetail.PlaceDetailViewModel
import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.component.PlaceMapRoute
import com.daedan.festabook.presentation.placeMap.intent.event.SelectEvent
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
    onAppFinish: () -> Unit,
    onSubscriptionConfirm: () -> Unit,
    onNavigateToExplore: () -> Unit, // TODO 검색화면 마이그레이션 시 제거
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    placeMapViewModel: PlaceMapViewModel = viewModel(),
    newsViewModel: NewsViewModel = viewModel(),
    settingViewModel: SettingViewModel = viewModel(),
) {
    val navigator = rememberFestabookNavigator()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarManager = rememberAppSnackbarManager(snackbarHostState)
    val backPressExitMessage = stringResource(R.string.back_press_exit_message)
    val noticeEnabledMessage = stringResource(R.string.setting_notice_enabled)

    ObserveAsEvents(flow = mainViewModel.navigateNewsEvent) {
        navigator.navigateToMainTab(FestabookMainTab.NEWS)
    }
    ObserveAsEvents(flow = mainViewModel.backPressEvent) { isDoublePress ->
        if (isDoublePress) {
            onAppFinish()
        } else {
            snackbarManager.show(backPressExitMessage)
        }
    }
    ObserveAsEvents(flow = homeViewModel.navigateToScheduleEvent) {
        navigator.navigateToMainTab(FestabookMainTab.SCHEDULE)
    }
    ObserveAsEvents(flow = settingViewModel.success) {
        snackbarManager.show(noticeEnabledMessage)
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
        val isVisible = navigator.currentTab == FestabookMainTab.PLACE_MAP
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
            logger = logger,
            onStartPlaceDetail = {
                navigator.navigate(
                    FestabookRoute.PlaceDetail(
                        placeDetailUiModel = it.placeDetail.value,
                    ),
                )
            },
        )
        FestabookNavHost(
            modifier = Modifier.padding(innerPadding),
            navigator = navigator,
            mainViewModel = mainViewModel,
            homeViewModel = homeViewModel,
            scheduleViewModel = scheduleViewModel,
            placeDetailViewModelFactory = placeDetailViewModelFactory,
            newsViewModel = newsViewModel,
            settingViewModel = settingViewModel,
            notificationPermissionManager = notificationPermissionManager,
            onNavigateToExplore = onNavigateToExplore,
            onSubscriptionConfirm = onSubscriptionConfirm,
            snackbarManager = snackbarManager,
        )
    }
}

@Composable
private fun FestabookNavHost(
    navigator: FestabookNavigator,
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    scheduleViewModel: ScheduleViewModel,
    placeDetailViewModelFactory: PlaceDetailViewModel.Factory,
    newsViewModel: NewsViewModel,
    settingViewModel: SettingViewModel,
    notificationPermissionManager: NotificationPermissionManager,
    onNavigateToExplore: () -> Unit,
    onSubscriptionConfirm: () -> Unit,
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
            onNavigateToExplore = onNavigateToExplore,
            onSubscriptionConfirm = onSubscriptionConfirm,
            onShowErrorSnackbar = snackbarManager::showError,
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
