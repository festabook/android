package com.daedan.festabook.presentation.news.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.FestabookTopAppBar
import com.daedan.festabook.presentation.news.NewsTab
import com.daedan.festabook.presentation.news.NewsViewModel
import com.daedan.festabook.presentation.news.faq.FAQUiState
import com.daedan.festabook.presentation.news.lost.LostUiState
import com.daedan.festabook.presentation.news.notice.NoticeUiState

@Composable
fun NewsScreen(
    newsViewModel: NewsViewModel,
    modifier: Modifier = Modifier,
    onShowErrorSnackbar: (Throwable) -> Unit = {}, // TODO Fragment 제거 시 필수 파라미터로 변경
) {
    val pageState = rememberPagerState { NewsTab.entries.size }
    val scope = rememberCoroutineScope()

    val noticeUiState by newsViewModel.noticeUiState.collectAsStateWithLifecycle()
    val lostUiState by newsViewModel.lostUiState.collectAsStateWithLifecycle()
    val faqUiState by newsViewModel.faqUiState.collectAsStateWithLifecycle()
    val currentOnShowErrorSnackbar by rememberUpdatedState(onShowErrorSnackbar)

    LaunchedEffect(noticeUiState) {
        when (val content = noticeUiState.content) {
            is NoticeUiState.Content.Success -> {
                pageState.animateScrollToPage(NewsTab.NOTICE.ordinal)
            }

            is NoticeUiState.Content.Error -> {
                currentOnShowErrorSnackbar(content.throwable)
            }

            else -> {}
        }
    }
    LaunchedEffect(lostUiState) {
        when (val content = lostUiState.content) {
            is LostUiState.Content.Error -> {
                currentOnShowErrorSnackbar(content.throwable)
            }

            else -> {}
        }
    }

    LaunchedEffect(faqUiState) {
        when (val uiState = faqUiState) {
            is FAQUiState.Error -> {
                currentOnShowErrorSnackbar(uiState.throwable)
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = { FestabookTopAppBar(title = stringResource(R.string.news_title)) },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
        ) {
            NewsTabRow(pageState, scope)
            NewsTabPage(
                pageState = pageState,
                noticeUiState = noticeUiState,
                faqUiState = faqUiState,
                lostUiState = lostUiState,
                onNoticeRefresh = {
                    val currentUiState = noticeUiState.copy(isRefreshing = true)
                    newsViewModel.loadAllNotices(currentUiState)
                },
                onLostItemRefresh = {
                    val currentUiState = lostUiState.copy(isRefreshing = true)
                    newsViewModel.loadAllLostItems(currentUiState)
                },
                onNoticeClick = { newsViewModel.toggleNotice(it) },
                onFaqClick = { newsViewModel.toggleFAQ(it) },
                onLostGuideClick = { newsViewModel.toggleLostGuide() },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
