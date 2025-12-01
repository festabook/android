package com.daedan.festabook.presentation.news.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.FestabookTopAppBar
import com.daedan.festabook.presentation.news.NewsTab
import com.daedan.festabook.presentation.news.NewsViewModel
import com.daedan.festabook.presentation.news.lost.LostUiState
import com.daedan.festabook.presentation.news.notice.NoticeUiState

@Composable
fun NewsScreen(
    newsViewModel: NewsViewModel,
    modifier: Modifier = Modifier,
) {
    val pageState = rememberPagerState { NewsTab.entries.size }
    val scope = rememberCoroutineScope()

    val noticeUiState by newsViewModel.noticeUiState.collectAsStateWithLifecycle()
    val lostUiState by newsViewModel.lostUiState.collectAsStateWithLifecycle()
    val faqUiState by newsViewModel.faqUiState.collectAsStateWithLifecycle()

    val isNoticeRefreshing = noticeUiState is NoticeUiState.Refreshing
    val isLostItemRefreshing = lostUiState is LostUiState.Refreshing

    LaunchedEffect(noticeUiState) {
        if (noticeUiState is NoticeUiState.Success) {
            pageState.animateScrollToPage(NewsTab.NOTICE.ordinal)
        }
    }
    Scaffold(
        topBar = { FestabookTopAppBar(title = stringResource(R.string.news_title)) },
        modifier = modifier,
    ) { innerPadding ->
        Column(modifier = modifier.padding(paddingValues = innerPadding)) {
            NewsTabRow(pageState, scope)
            NewsTabPage(
                pageState = pageState,
                noticeUiState = noticeUiState,
                faqUiState = faqUiState,
                lostUiState = lostUiState,
                isNoticeRefreshing = isNoticeRefreshing,
                isLostItemRefreshing = isLostItemRefreshing,
                onNoticeRefresh = {
                    val oldNotices =
                        (noticeUiState as? NoticeUiState.Success)?.notices ?: emptyList()
                    newsViewModel.loadAllNotices(NoticeUiState.Refreshing(oldNotices))
                },
                onLostItemRefresh = {
                    val oldLostItems =
                        (lostUiState as? LostUiState.Success)?.lostItems ?: emptyList()
                    newsViewModel.loadAllLostItems(LostUiState.Refreshing(oldLostItems))
                },
                onNoticeClick = { newsViewModel.toggleNotice(it) },
                onFaqClick = { newsViewModel.toggleFAQ(it) },
                onLostGuideClick = { newsViewModel.toggleLostGuide() },
            )
        }
    }
}
