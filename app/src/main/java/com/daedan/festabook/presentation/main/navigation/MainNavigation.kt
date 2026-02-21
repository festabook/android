package com.daedan.festabook.presentation.main.navigation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.NotificationPermissionManager
import com.daedan.festabook.presentation.main.FestabookNavigator
import com.daedan.festabook.presentation.main.FestabookRoute
import com.daedan.festabook.presentation.main.component.MainScreen
import com.daedan.festabook.presentation.placeDetail.PlaceDetailViewModel
import com.daedan.festabook.presentation.setting.SettingViewModel
import com.naver.maps.map.util.FusedLocationSource

fun NavGraphBuilder.mainNavGraph(
    defaultViewModelFactory: ViewModelProvider.Factory,
    placeDetailViewModelFactory: PlaceDetailViewModel.Factory,
    notificationPermissionManager: NotificationPermissionManager,
    locationSource: FusedLocationSource,
    logger: DefaultFirebaseLogger,
    onSubscriptionConfirm: () -> Unit,
    festabookNavigator: FestabookNavigator,
    settingViewModel: SettingViewModel,
) {
    composable<FestabookRoute.Main> {
        val mainBackEntry =
            festabookNavigator.navController.getBackStackEntry<FestabookRoute.Main>()
        MainScreen(
            notificationPermissionManager = notificationPermissionManager,
            logger = logger,
            locationSource = locationSource,
            placeDetailViewModelFactory = placeDetailViewModelFactory,
            onAppFinish = festabookNavigator::popBackStack,
            onSubscriptionConfirm = onSubscriptionConfirm,
            festabookNavigator = festabookNavigator,
            mainViewModel = viewModel(mainBackEntry, factory = defaultViewModelFactory),
            homeViewModel = viewModel(mainBackEntry, factory = defaultViewModelFactory),
            scheduleViewModel = viewModel(mainBackEntry, factory = defaultViewModelFactory),
            placeMapViewModel = viewModel(mainBackEntry, factory = defaultViewModelFactory),
            newsViewModel = viewModel(mainBackEntry, factory = defaultViewModelFactory),
            settingViewModel = settingViewModel,
        )
    }
}
