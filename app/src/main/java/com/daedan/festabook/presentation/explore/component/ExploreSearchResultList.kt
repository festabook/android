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
fun ExploreSearchResultList(
    searchState: SearchUiState,
    onUniversitySelected: (SearchResultUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
    ) {
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
private fun ExploreSearchResultListLoadingPreview() {
    FestabookTheme {
        ExploreSearchResultList(
            searchState = SearchUiState.Loading,
            onUniversitySelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreSearchResultListSuccessPreview() {
    val fakeUniversities =
        listOf(
            SearchResultUiModel(1, "서울대학교", "대동제"),
            SearchResultUiModel(2, "서울시립대학교", "대동제"),
        )
    FestabookTheme {
        ExploreSearchResultList(
            searchState =
                SearchUiState.Success(
                    universitiesFound = fakeUniversities,
                ),
            onUniversitySelected = {},
        )
    }
}
