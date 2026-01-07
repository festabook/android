package com.daedan.festabook.presentation.news.faq.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.EmptyStateScreen
import com.daedan.festabook.presentation.common.component.LoadingStateScreen
import com.daedan.festabook.presentation.news.component.NewsItem
import com.daedan.festabook.presentation.news.faq.FAQUiState
import com.daedan.festabook.presentation.news.faq.model.FAQItemUiModel
import com.daedan.festabook.presentation.theme.festabookSpacing
import timber.log.Timber

@Composable
fun FAQScreen(
    uiState: FAQUiState,
    onFaqClick: (FAQItemUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is FAQUiState.Error -> {
            LaunchedEffect(uiState) {
                Timber.w(uiState.throwable.stackTraceToString())
            }
        }

        is FAQUiState.InitialLoading -> LoadingStateScreen()

        is FAQUiState.Success -> {
            if (uiState.faqs.isEmpty()) {
                EmptyStateScreen()
            } else {
                LazyColumn(
                    modifier = modifier,
                    contentPadding =
                        PaddingValues(
                            top = festabookSpacing.paddingBody2,
                            bottom = festabookSpacing.paddingBody2,
                        ),
                    verticalArrangement = Arrangement.spacedBy(festabookSpacing.paddingBody2),
                ) {
                    items(
                        items = uiState.faqs,
                        key = { faq -> faq.questionId },
                    ) { faq ->
                        NewsItem(
                            title = stringResource(R.string.tab_faq_question, faq.question),
                            description = faq.answer,
                            isExpanded = faq.isExpanded,
                            onclick = { onFaqClick(faq) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FAQScreenPreview() {
    FAQScreen(uiState = FAQUiState.Success(emptyList()), onFaqClick = {})
}
