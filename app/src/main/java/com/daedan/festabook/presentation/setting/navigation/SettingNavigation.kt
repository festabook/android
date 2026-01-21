package com.daedan.festabook.presentation.setting.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.presentation.NotificationPermissionManager
import com.daedan.festabook.presentation.home.HomeViewModel
import com.daedan.festabook.presentation.main.MainTabRoute
import com.daedan.festabook.presentation.setting.SettingViewModel
import com.daedan.festabook.presentation.setting.component.SettingRoute

fun NavGraphBuilder.settingNavGraph(
    padding: PaddingValues,
    homeViewModel: HomeViewModel,
    settingViewModel: SettingViewModel,
    notificationPermissionManager: NotificationPermissionManager,
    onShowSnackBar: (String) -> Unit,
    onShowErrorSnackBar: (Throwable) -> Unit,
) {
    composable<MainTabRoute.Setting> {
        SettingRoute(
            modifier = Modifier.padding(padding),
            homeViewModel = homeViewModel,
            settingViewModel = settingViewModel,
            notificationPermissionManager = notificationPermissionManager,
            onShowSnackBar = onShowSnackBar,
            onShowErrorSnackBar = onShowErrorSnackBar,
        )
    }
}
