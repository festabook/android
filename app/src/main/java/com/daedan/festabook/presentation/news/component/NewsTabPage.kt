package com.daedan.festabook.presentation.news.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.daedan.festabook.presentation.news.NewsTab
import com.daedan.festabook.presentation.news.faq.FAQUiState
import com.daedan.festabook.presentation.news.faq.component.FAQScreen
import com.daedan.festabook.presentation.news.faq.model.FAQItemUiModel
import com.daedan.festabook.presentation.news.lost.LostUiState
import com.daedan.festabook.presentation.news.lost.component.LostItemScreen
import com.daedan.festabook.presentation.news.notice.NoticeUiState
import com.daedan.festabook.presentation.news.notice.component.NoticeScreen
import com.daedan.festabook.presentation.news.notice.model.NoticeUiModel
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun NewsTabPage(
    pageState: PagerState,
    noticeUiState: NoticeUiState,
    faqUiState: FAQUiState,
    lostUiState: LostUiState,
    onNoticeRefresh: () -> Unit,
    onLostItemRefresh: () -> Unit,
    isNoticeRefreshing: Boolean,
    isLostItemRefreshing: Boolean,
    onNoticeClick: (NoticeUiModel) -> Unit,
    onFaqClick: (FAQItemUiModel) -> Unit,
    onLostGuideClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        state = pageState,
        verticalAlignment = Alignment.Top,
        modifier = modifier,
    ) { index ->
        val tab = NewsTab.entries[index]
        when (tab) {
            NewsTab.NOTICE ->
                NoticeScreen(
                    uiState = noticeUiState,
                    onNoticeClick = onNoticeClick,
                    isRefreshing = isNoticeRefreshing,
                    onRefresh = onNoticeRefresh,
                    modifier = Modifier.padding(horizontal = festabookSpacing.paddingScreenGutter),
                )

            NewsTab.FAQ ->
                FAQScreen(
                    uiState = faqUiState,
                    onFaqClick = onFaqClick,
                    modifier = Modifier.padding(horizontal = festabookSpacing.paddingScreenGutter),
                )

            NewsTab.LOST_ITEM ->
                LostItemScreen(
                    lostUiState = lostUiState,
                    onLostGuideClick = onLostGuideClick,
                    isRefreshing = isLostItemRefreshing,
                    onRefresh = onLostItemRefresh,
                    modifier = Modifier.padding(horizontal = festabookSpacing.paddingScreenGutter),
                )
        }
    }
}

@Composable
@Preview
private fun NewsTabPagePreview() {
    NewsTabPage(
        pageState = rememberPagerState { 3 },
        noticeUiState = NoticeUiState.Success(emptyList(), 0),
        faqUiState = FAQUiState.Success(emptyList()),
        lostUiState = LostUiState.Success(emptyList()),
        onNoticeRefresh = {},
        onLostItemRefresh = {},
        isNoticeRefreshing = false,
        isLostItemRefreshing = false,
        onNoticeClick = {},
        onFaqClick = {},
        onLostGuideClick = {},
    )
}
