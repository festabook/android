package com.daedan.festabook.presentation.explore.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.R
import com.daedan.festabook.presentation.explore.ExploreSideEffect
import com.daedan.festabook.presentation.explore.ExploreViewModel
import com.daedan.festabook.presentation.explore.SearchUiState
import com.daedan.festabook.presentation.explore.model.SearchResultUiModel
import com.daedan.festabook.presentation.theme.FestabookTheme

@Composable
fun ExploreScreen(
    onNavigateToMain: (SearchResultUiModel) -> Unit,
    onBackClick: () -> Unit,
    viewModel: ExploreViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    val latestNavigateToMain by rememberUpdatedState(onNavigateToMain)
    val latestKeyboardController by rememberUpdatedState(keyboardController)

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ExploreSideEffect.NavigateToMain -> {
                    latestKeyboardController?.hide()
                    latestNavigateToMain(effect.searchResult)
                }
            }
        }
    }

    if (uiState.hasFestivalId) {
        ExploreSearchScreen(
            query = uiState.query,
            searchState = uiState.searchState,
            onQueryChange = viewModel::onTextInputChanged,
            onUniversitySelect = viewModel::onUniversitySelected,
            onBackClick = onBackClick,
        )
    } else {
        ExploreLandingScreen(
            query = uiState.query,
            searchState = uiState.searchState,
            onQueryChange = viewModel::onTextInputChanged,
            onUniversitySelect = viewModel::onUniversitySelected,
        )
    }
}

@Composable
fun ExploreSearchScreen(
    query: String,
    searchState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onUniversitySelect: (SearchResultUiModel) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.White,
        topBar = {
            ExploreBackHeader(
                onBackClick = onBackClick,
                modifier = Modifier.statusBarsPadding(),
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
        ) {
            ExploreSearchContent(
                query = query,
                searchState = searchState,
                onQueryChange = onQueryChange,
                onUniversitySelect = onUniversitySelect,
            )
        }
    }
}

@Composable
fun ExploreLandingScreen(
    query: String,
    searchState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onUniversitySelect: (SearchResultUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isError = searchState.shouldShowErrorUi

    val isSearchMode = query.isNotBlank()

    Scaffold(
        modifier = modifier,
        containerColor = Color.White,
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding(),
        ) {
            AnimatedContent(
                targetState = isSearchMode,
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = fadeIn(tween(200)),
                        initialContentExit = fadeOut(tween(200)),
                    )
                },
            ) { searching ->
                if (!searching) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.weight(0.3f))

                        Image(
                            painter = painterResource(id = R.drawable.logo_title),
                            contentDescription = stringResource(id = R.string.explore_festabook_logo),
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

                        Spacer(modifier = Modifier.weight(0.7f))
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Image(
                            painter = painterResource(id = R.drawable.logo_title),
                            contentDescription = "FestaBook Logo",
                            modifier = Modifier.height(24.dp),
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            ExploreSearchContent(
                                query = query,
                                searchState = searchState,
                                onQueryChange = onQueryChange,
                                onUniversitySelect = onUniversitySelect,
                            )
                        }
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
            onUniversitySelect = {},
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
            onUniversitySelect = {},
        )
    }
}
