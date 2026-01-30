package com.daedan.festabook.presentation.explore.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is ExploreSideEffect.NavigateToMain -> {
                    keyboardController?.hide()
                    onNavigateToMain(effect.searchResult)
                }
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
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(20.dp),
        ) {
            ExploreSearchContent(
                query = query,
                searchState = searchState,
                onQueryChange = onQueryChange,
                onUniversitySelected = onUniversitySelected,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
fun ExploreLandingScreen(
    query: String,
    searchState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onUniversitySelected: (SearchResultUiModel) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isSearchResultEmpty =
        searchState is SearchUiState.Success && searchState.universitiesFound.isEmpty()
    val isSearchError = searchState is SearchUiState.Error
    val isError = isSearchResultEmpty || isSearchError

    Scaffold(
        containerColor = Color.White,
    ) { innerPadding ->
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding(),
        ) {
            val isSearchMode = query.isNotEmpty()

            val targetTopPadding = if (isSearchMode) 20.dp else maxHeight * 0.3f
            val animatedTopPadding by animateDpAsState(
                targetValue = targetTopPadding,
                animationSpec = tween(durationMillis = 300),
                label = "topPadding",
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(animatedTopPadding))

                Image(
                    painter = painterResource(id = R.drawable.logo_title),
                    contentDescription = "FestaBook Logo",
                    modifier = Modifier.height(24.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    ExploreSearchBar(
                        query = query,
                        onQueryChange = onQueryChange,
                        onSearch = { keyboardController?.hide() },
                        isError = isError,
                    )
                }

                AnimatedVisibility(
                    visible = isSearchMode,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        ExploreSearchResultList(
                            searchState = searchState,
                            onUniversitySelected = onUniversitySelected,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
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
            query = "ㅇㄹㅇ",
            searchState = SearchUiState.Idle,
            onQueryChange = {},
            onUniversitySelected = {},
        )
    }
}
