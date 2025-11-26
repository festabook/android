package com.daedan.festabook.presentation.news.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.Header
import com.daedan.festabook.presentation.main.MainViewModel
import com.daedan.festabook.presentation.news.NewsTab
import com.daedan.festabook.presentation.news.NewsViewModel
import com.daedan.festabook.presentation.news.faq.component.FAQScreenContainer
import com.daedan.festabook.presentation.news.lost.component.LostItemScreenContainer
import com.daedan.festabook.presentation.news.notice.component.NoticeScreenContainer
import com.daedan.festabook.presentation.theme.FestabookColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NewsScreen(
    newsViewModel: NewsViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val pageState = rememberPagerState { NewsTab.entries.size }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.background(color = MaterialTheme.colorScheme.background)) {
        Header(title = stringResource(R.string.news_title))
        NewsTabRow(pageState, scope)
        NewsTabPage(pageState, newsViewModel)
    }
}

@Composable
private fun NewsTabRow(
    pageState: PagerState,
    scope: CoroutineScope,
) {
    TabRow(
        selectedTabIndex = pageState.currentPage,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = FestabookColor.black,
        indicator = { tabPositions ->
            TabRowDefaults.PrimaryIndicator(
                color = FestabookColor.black,
                width = tabPositions[pageState.currentPage].width,
                modifier = Modifier.tabIndicatorOffset(currentTabPosition = tabPositions[pageState.currentPage]),
            )
        },
    ) {
        NewsTab.entries.forEachIndexed { index, title ->
            Tab(
                selected = pageState.currentPage == index,
                unselectedContentColor = FestabookColor.gray500,
                onClick = { scope.launch { pageState.animateScrollToPage(index) } },
                text = { Text(text = stringResource(title.tabNameRes)) },
            )
        }
    }
}

@Composable
private fun NewsTabPage(
    pageState: PagerState,
    newsViewModel: NewsViewModel,
) {
    HorizontalPager(
        state = pageState,
        verticalAlignment = Alignment.Top,
    ) { index ->
        val tab = NewsTab.entries[index]
        when (tab) {
            NewsTab.NOTICE ->
                NoticeScreenContainer(
                    newsViewModel = newsViewModel,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

            NewsTab.FAQ ->
                FAQScreenContainer(
                    newsViewModel = newsViewModel,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

            NewsTab.LOST_ITEM ->
                LostItemScreenContainer(
                    newsViewModel = newsViewModel,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
        }
    }
}
