package com.daedan.festabook.presentation.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions

class FestabookNavigator(
    val navController: NavHostController,
    val startRoute: FestabookRoute = FestabookRoute.Splash,
) {
    private val currentDestination
        @Composable
        get() =
            navController
                .visibleEntries
                .collectAsState()
                .value
                .lastOrNull { it.destination.route != null }
                ?.destination
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

    fun navigateToMainTab(tab: FestabookMainTab) {
        navController.navigate(
            tab.route,
            navOptions {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            },
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

    fun popBackStack() {
        navController.popBackStack()
    }

    fun popBackStack(
        route: FestabookRoute,
        inclusive: Boolean = false,
    ) {
        navController.popBackStack(route, inclusive)
    }
}

@Composable
fun rememberFestabookNavigator(startRoute: FestabookRoute = FestabookRoute.Splash): FestabookNavigator {
    val navController = rememberNavController()
    return remember {
        FestabookNavigator(navController = navController, startRoute = startRoute)
    }
}
