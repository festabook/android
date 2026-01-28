package com.daedan.festabook.presentation.home.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.domain.model.Festival
import com.daedan.festabook.domain.model.Organization
import com.daedan.festabook.domain.model.Poster
import com.daedan.festabook.presentation.common.component.LoadingStateScreen
import com.daedan.festabook.presentation.common.formatFestivalPeriod
import com.daedan.festabook.presentation.home.HomeViewModel
import com.daedan.festabook.presentation.home.LineUpItemGroupUiModel
import com.daedan.festabook.presentation.home.LineupItemUiModel
import com.daedan.festabook.presentation.home.LineupUiState
import com.daedan.festabook.presentation.home.adapter.FestivalUiState
import com.daedan.festabook.presentation.theme.FestabookColor
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToExplore: () -> Unit,
    modifier: Modifier = Modifier,
    onShowErrorSnackbar: (Throwable) -> Unit = {}, // TODO Fragment 제거 시 필수 파라미터로 변경
) {
    val festivalUiState by viewModel.festivalUiState.collectAsStateWithLifecycle()
    val lineupUiState by viewModel.lineupUiState.collectAsStateWithLifecycle()
    val currentOnShowErrorSnackbar by rememberUpdatedState(onShowErrorSnackbar)
    LaunchedEffect(festivalUiState) {
        when (val state = festivalUiState) {
            is FestivalUiState.Error -> {
                currentOnShowErrorSnackbar(state.throwable)
            }

            else -> {
                Unit
            }
        }
    }

    when (val state = festivalUiState) {
        is FestivalUiState.Loading -> {
            LoadingStateScreen(modifier = modifier)
        }

        is FestivalUiState.Error -> {
            Box(modifier = modifier.fillMaxSize()) {
                Text(
                    text = "데이터를 불러오는데 실패했습니다.",
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }

        is FestivalUiState.Success -> {
            FestivalOverview(
                festivalUiState = state,
                lineupUiState = lineupUiState,
                onNavigateToExplore = onNavigateToExplore,
                onNavigateToSchedule = viewModel::navigateToScheduleClick,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun FestivalOverview(
    festivalUiState: FestivalUiState.Success,
    lineupUiState: LineupUiState,
    onNavigateToExplore: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val universityName = festivalUiState.organization.universityName

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            HomeHeader(
                universityName = universityName,
                onExpandClick = onNavigateToExplore,
                modifier = Modifier.padding(top = 40.dp, bottom = 12.dp),
            )

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize(),
            ) {
                // 포스터 리스트
                item {
                    val posterUrls =
                        festivalUiState.organization.festival.festivalImages
                            .sortedBy { it.sequence }
                            .map { it.imageUrl }

                    HomePosterList(
                        posterUrls = posterUrls,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }

                // 축제 정보
                item {
                    val festival = festivalUiState.organization.festival
                    HomeFestivalInfo(
                        festivalName = festival.festivalName,
                        festivalDate =
                            formatFestivalPeriod(
                                festival.startDate,
                                festival.endDate,
                            ),
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }

                // 구분선
                item {
                    HorizontalDivider(
                        thickness = 4.dp,
                        color = FestabookColor.gray200,
                        modifier =
                            Modifier
                                .padding(top = 16.dp),
                    )
                }

                // 라인업 헤더
                item {
                    HomeLineupHeader(
                        onScheduleClick = onNavigateToSchedule,
                    )
                }

                // 라인업 리스트
                when (lineupUiState) {
                    is LineupUiState.Success -> {
                        items(
                            items = lineupUiState.lineups,
                            key = { it.id },
                        ) { lineupItem ->
                            HomeLineupItem(uiModel = lineupItem)
                        }
                    }

                    is LineupUiState.Loading -> {
                        // 로딩 시 동작 논의 후 추가
                    }

                    is LineupUiState.Error -> {
                        // 에러 표시
                    }
                }

                // 하단 여백 추가
                item {
                    Spacer(modifier = Modifier.padding(bottom = 60.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FestivalOverviewPreview() {
    val sampleFestival =
        Organization(
            id = 1,
            universityName = "가천대학교",
            festival =
                Festival(
                    festivalName = "2025 가천 Water Festival\n: AQUA WAVE",
                    startDate = LocalDate.now(),
                    endDate = LocalDate.now().plusDays(2),
                    festivalImages =
                        listOf(
                            Poster(1, "sample", 1),
                            Poster(2, "sample", 2),
                        ),
                ),
        )

    val sampleLineups =
        LineUpItemGroupUiModel(
            group =
                mapOf(
                    LocalDate.now() to
                        listOf(
                            LineupItemUiModel(1, "sample", "실리카겔", LocalDateTime.now()),
                            LineupItemUiModel(2, "sample", "아이유", LocalDateTime.now()),
                        ),
                    LocalDate.now().plusDays(1) to
                        listOf(
                            LineupItemUiModel(3, "sample", "뉴진스", LocalDateTime.now()),
                        ),
                ),
        )

    FestivalOverview(
        festivalUiState = FestivalUiState.Success(sampleFestival),
        lineupUiState = LineupUiState.Success(sampleLineups.getLineupItems()),
        onNavigateToExplore = {},
        onNavigateToSchedule = {},
    )
}
