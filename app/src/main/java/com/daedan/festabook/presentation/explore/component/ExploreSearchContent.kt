package com.daedan.festabook.presentation.explore.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.presentation.explore.SearchUiState
import com.daedan.festabook.presentation.explore.model.SearchResultUiModel
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme

@Composable
fun ExploreSearchContent(
    query: String,
    searchState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onUniversitySelected: (SearchResultUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSearchResultEmpty =
        searchState is SearchUiState.Success && searchState.universitiesFound.isEmpty()
    val isSearchError = searchState is SearchUiState.Error

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
    ) {
        item {
            Box(modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)) {
                ExploreSearchBar(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = { /* Search is handled by query change */ },
                    isError = isSearchResultEmpty || isSearchError,
                )
            }
        }

        when (searchState) {
            is SearchUiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(top = 20.dp),
                            color = FestabookColor.accentBlue,
                        )
                    }
                }
            }

            is SearchUiState.Success -> {
                items(searchState.universitiesFound) { university ->
                    ExploreResultItem(
                        university = university,
                        onItemClick = onUniversitySelected,
                    )
                }
            }

            is SearchUiState.Error -> {}

            is SearchUiState.Idle -> {}
        }
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
            onUniversitySelected = {},
        )
    }
}
