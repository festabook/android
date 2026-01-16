package com.daedan.festabook.presentation.main.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.daedan.festabook.presentation.home.HomeViewModel
import com.daedan.festabook.presentation.home.navigation.homeNavGraph
import com.daedan.festabook.presentation.main.FestabookRoute
import com.daedan.festabook.presentation.main.rememberFestabookNavigator

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val navigator = rememberFestabookNavigator()

    Scaffold(
        // TODO: 스낵바 구현 및 하위 프래그먼트에 해당 SnackBar 적용
        bottomBar = {
            if (navigator.shouldShowBottomBar) {
                FestabookBottomNavigationBar(
                    currentTab = navigator.currentTab,
                    onTabSelect = { navigator.navigateToMainTab(it.route) },
                )
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        NavHost(
            modifier = Modifier.fillMaxSize(),
            startDestination = navigator.startRoute,
            navController = navigator.navController,
        ) {
            homeNavGraph(
                padding = innerPadding,
                viewModel = homeViewModel,
                onNavigateToExplore = { navigator.navigate(FestabookRoute.Explore) },
            )

            // TODO: 각 화면에서 나머지 graph들 정의
            // 만약 Fragment에서 Screen에 넣어줄 부가적인 작업 시 각 화면에서 Route 컴포저블로 감싸서 전달 요망
            // 참고:https://github.com/Project-Unifest/unifest-android/blob/develop/feature/home/src/main/kotlin/com/unifest/android/feature/home/HomeScreen.kt
        }
    }
}
