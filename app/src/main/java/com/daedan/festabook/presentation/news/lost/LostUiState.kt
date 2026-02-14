package com.daedan.festabook.presentation.news.lost

import com.daedan.festabook.presentation.news.lost.model.LostUiModel

data class LostUiState(
    val content: Content,
    val isRefreshing: Boolean = false,
) {
    sealed interface Content {
        data object InitialLoading : Content

        data class Success(
            val lostItems: List<LostUiModel>,
        ) : Content

        data class Error(
            val throwable: Throwable,
        ) : Content
    }
}
