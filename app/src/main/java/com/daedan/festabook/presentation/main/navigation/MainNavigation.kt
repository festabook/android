package com.daedan.festabook.presentation.main.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.di.FestaBookAppGraph
import com.daedan.festabook.presentation.main.FestabookNavigator
import com.daedan.festabook.presentation.main.FestabookRoute
import com.daedan.festabook.presentation.main.component.MainScreen
import com.naver.maps.map.util.FusedLocationSource

fun NavGraphBuilder.mainNavGraph(
    appGraph: FestaBookAppGraph,
    locationSource: FusedLocationSource,
    festabookNavigator: FestabookNavigator,
) {
    composable<FestabookRoute.Main> {
        val mainBackEntry =
            festabookNavigator.navController.getBackStackEntry<FestabookRoute.Main>()
        MainScreen(
            appGraph = appGraph,
            locationSource = locationSource,
            onAppFinish = festabookNavigator::popBackStack,
            festabookNavigator = festabookNavigator,
            homeViewModel = viewModel(mainBackEntry, factory = appGraph.metroViewModelFactory),
            scheduleViewModel = viewModel(mainBackEntry, factory = appGraph.metroViewModelFactory),
            placeMapViewModel = viewModel(mainBackEntry, factory = appGraph.metroViewModelFactory),
            settingViewModel = viewModel(mainBackEntry, factory = appGraph.metroViewModelFactory),
            mainViewModel = viewModel(mainBackEntry, factory = appGraph.metroViewModelFactory),
            newsViewModel = viewModel(mainBackEntry, factory = appGraph.metroViewModelFactory),
        )
    }
}
