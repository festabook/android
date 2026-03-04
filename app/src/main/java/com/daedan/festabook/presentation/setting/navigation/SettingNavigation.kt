package com.daedan.festabook.presentation.setting.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.presentation.NotificationPermissionManager
import com.daedan.festabook.presentation.home.HomeViewModel
import com.daedan.festabook.presentation.main.MainTabRoute
import com.daedan.festabook.presentation.setting.SettingViewModel
import com.daedan.festabook.presentation.setting.component.SettingRoute

fun NavGraphBuilder.settingNavGraph(
    homeViewModel: HomeViewModel,
    settingViewModel: SettingViewModel,
    notificationPermissionManager: NotificationPermissionManager,
    onShowSnackBar: (String) -> Unit,
    onShowErrorSnackBar: (Throwable) -> Unit,
) {
    composable<MainTabRoute.Setting> {
        SettingRoute(
            homeViewModel = homeViewModel,
            settingViewModel = settingViewModel,
            notificationPermissionManager = notificationPermissionManager,
            onShowSnackBar = onShowSnackBar,
            onShowErrorSnackBar = onShowErrorSnackBar,
        )
    }
}
