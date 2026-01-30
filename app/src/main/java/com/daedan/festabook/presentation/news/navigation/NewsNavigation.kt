package com.daedan.festabook.presentation.news.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.daedan.festabook.presentation.main.MainTabRoute
import com.daedan.festabook.presentation.news.NewsViewModel
import com.daedan.festabook.presentation.news.component.NewsScreen

fun NavGraphBuilder.newsNavGraph(
    viewModel: NewsViewModel,
    onShowErrorSnackbar: (Throwable) -> Unit,
) {
    composable<MainTabRoute.News> {
        NewsScreen(
            newsViewModel = viewModel,
            onShowErrorSnackbar = onShowErrorSnackbar,
        )
    }
}
