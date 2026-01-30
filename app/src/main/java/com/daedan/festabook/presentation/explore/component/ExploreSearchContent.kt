package com.daedan.festabook.presentation.explore.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.presentation.explore.SearchUiState
import com.daedan.festabook.presentation.explore.model.SearchResultUiModel
import com.daedan.festabook.presentation.theme.FestabookTheme

@Composable
fun ExploreSearchContent(
    query: String,
    searchState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onUniversitySelect: (SearchResultUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isSearchResultEmpty =
        searchState is SearchUiState.Success && searchState.universitiesFound.isEmpty()
    val isSearchError = searchState is SearchUiState.Error

    Column(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        Box(modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)) {
            ExploreSearchBar(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = { keyboardController?.hide() },
                isError = isSearchResultEmpty || isSearchError,
            )
        }

        ExploreSearchResultList(
            searchState = searchState,
            onUniversitySelect = onUniversitySelect,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreSearchContentPreview() {
    FestabookTheme {
        ExploreSearchContent(
            query = "서울",
            searchState =
                SearchUiState.Success(
                    listOf(
                        SearchResultUiModel(1, "서울시립대학교", "2024 대동제"),
                        SearchResultUiModel(2, "서울대학교", "2024 봄축제"),
                    ),
                ),
            onQueryChange = {},
            onUniversitySelect = {},
        )
    }
}
