package com.daedan.festabook.presentation.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions

class FestabookNavigator(
    val navController: NavHostController,
) {
    private val currentDestination
        @Composable
        get() =
            navController
                .currentBackStackEntryAsState()
                .value
                ?.destination

    private val defaultNavOptions =
        navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }

    val currentTab
        @Composable
        get() =
            FestabookMainTab.find {
                currentDestination?.hasRoute(it::class) ?: false
            }

    val shouldShowBottomBar
        @Composable
        get() =
            FestabookMainTab.find {
                currentDestination?.hasRoute(it::class) ?: false
            } != null

    val startRoute = FestabookMainRoute.Home // TODO: Splash와 Explore 연동 시 변경

    fun navigateToMainTab(route: FestabookRoute) {
        navController.navigate(
            route,
            defaultNavOptions,
        )
    }

    fun navigate(
        route: FestabookRoute,
        navOptions: NavOptions? = null,
    ) {
        navController.navigate(
            route,
            navOptions,
        )
    }
}

@Composable
fun rememberFestabookNavigator(): FestabookNavigator {
    val navController = rememberNavController()
    return remember {
        FestabookNavigator(navController = navController)
    }
}
