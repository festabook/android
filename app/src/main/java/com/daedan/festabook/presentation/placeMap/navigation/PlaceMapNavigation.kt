package com.daedan.festabook.presentation.placeMap.navigation

import androidx.compose.runtime.MutableState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.presentation.main.MainTabRoute

fun NavGraphBuilder.placeMapNavGraph(mapScreenVisibilityState: MutableState<Boolean>) {
    composable<MainTabRoute.PlaceMap> {
        mapScreenVisibilityState.value = true
    }
}
