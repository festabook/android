package com.daedan.festabook.presentation.news.notice

import com.daedan.festabook.presentation.news.notice.model.NoticeUiModel

data class NoticeUiState(
    val content: Content,
    val isRefreshing: Boolean = false,
) {
    sealed interface Content {
        data object InitialLoading : Content

        data class Success(
            val notices: List<NoticeUiModel>,
            val expandPosition: Int,
        ) : Content

        data class Error(
            val throwable: Throwable,
        ) : Content
    }

    companion object {
        const val DEFAULT_POSITION: Int = 0
    }
}
