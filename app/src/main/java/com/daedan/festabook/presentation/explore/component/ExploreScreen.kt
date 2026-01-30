package com.daedan.festabook.presentation.explore.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.R
import com.daedan.festabook.presentation.explore.ExploreSideEffect
import com.daedan.festabook.presentation.explore.ExploreViewModel
import com.daedan.festabook.presentation.explore.SearchUiState
import com.daedan.festabook.presentation.explore.model.SearchResultUiModel
import com.daedan.festabook.presentation.theme.FestabookTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onNavigateToMain: (SearchResultUiModel) -> Unit,
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is ExploreSideEffect.NavigateToMain -> onNavigateToMain(effect.searchResult)
            }
        }
    }

    if (uiState.hasFestivalId) {
        ExploreSearchScreen(
            query = uiState.query,
            searchState = uiState.searchState,
            onQueryChange = viewModel::onTextInputChanged,
            onUniversitySelected = viewModel::onUniversitySelected,
            onBackClick = onBackClick,
        )
    } else {
        ExploreLandingScreen(
            query = uiState.query,
            searchState = uiState.searchState,
            onQueryChange = viewModel::onTextInputChanged,
            onUniversitySelected = viewModel::onUniversitySelected,
        )
    }
}

@Composable
fun ExploreSearchScreen(
    query: String,
    searchState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onUniversitySelected: (SearchResultUiModel) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            ExploreBackHeader(
                onBackClick = onBackClick,
                modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues()),
            )
        },
    ) { innerPadding ->
        ExploreSearchContent(
            query = query,
            searchState = searchState,
            onQueryChange = onQueryChange,
            onUniversitySelected = onUniversitySelected,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun ExploreLandingScreen(
    query: String,
    searchState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onUniversitySelected: (SearchResultUiModel) -> Unit,
) {
    Scaffold(
        containerColor = Color.White,
    ) { innerPadding ->
        if (query.isEmpty()) {
            // 검색어가 없을 때: 중앙 정렬 (로고 + 검색창)
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_title),
                    contentDescription = "FestaBook Logo",
                    modifier = Modifier.height(24.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
                ExploreSearchBar(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = {},
                    isError = false,
                )
                Spacer(modifier = Modifier.height(80.dp))
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
            ) {
                Spacer(modifier = Modifier.height(20.dp)) // 상단 여백
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_title),
                        contentDescription = "FestaBook Logo",
                        modifier = Modifier.height(24.dp),
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                ExploreSearchContent(
                    query = query,
                    searchState = searchState,
                    onQueryChange = onQueryChange,
                    onUniversitySelected = onUniversitySelected,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreSearchScreenPreview() {
    FestabookTheme {
        ExploreSearchScreen(
            query = "서울",
            searchState = SearchUiState.Idle,
            onQueryChange = {},
            onUniversitySelected = {},
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreLandingScreenPreview() {
    FestabookTheme {
        ExploreLandingScreen(
            query = "",
            searchState = SearchUiState.Idle,
            onQueryChange = {},
            onUniversitySelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreLandingScreenPreview2() {
    FestabookTheme {
        ExploreLandingScreen(
            query = "검색 내용이 있을 때",
            searchState = SearchUiState.Idle,
            onQueryChange = {},
            onUniversitySelected = {},
        )
    }
}
