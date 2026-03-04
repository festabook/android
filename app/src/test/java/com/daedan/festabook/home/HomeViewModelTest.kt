package com.daedan.festabook.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.daedan.festabook.domain.repository.FestivalRepository
import com.daedan.festabook.presentation.home.HomeViewModel
import com.daedan.festabook.presentation.home.LineUpItemOfDayUiModel
import com.daedan.festabook.presentation.home.LineupUiState
import com.daedan.festabook.presentation.home.adapter.FestivalUiState
import com.daedan.festabook.presentation.home.toUiModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var festivalRepository: FestivalRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        festivalRepository = mockk()
        coEvery { festivalRepository.getFestivalInfo() } returns Result.success(FAKE_ORGANIZATION)
        coEvery { festivalRepository.getLineUpGroupByDate() } returns
            Result.success(
                mapOf(
                    FAKE_LINEUP[0].performanceAt.toLocalDate() to FAKE_LINEUP,
                ),
            )

        homeViewModel = HomeViewModel(festivalRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `축제 정보를 불러올 수 있다`() =
        runTest {
            // given
            val expect = FestivalUiState.Success(FAKE_ORGANIZATION)

            // when
            homeViewModel.loadFestival()
            advanceUntilIdle()

            // then
            val actual = homeViewModel.festivalUiState.value
            assertThat(actual).isInstanceOf(FestivalUiState.Success::class.java)
            assertThat((actual as FestivalUiState.Success).organization).isEqualTo(FAKE_ORGANIZATION)
        }

    @Test
    fun `연예인 정보를 불러올 수 있다`() =
        runTest {
            // given
            val expectedLineup =
                listOf(
                    LineUpItemOfDayUiModel(
                        id = 0,
                        date = FAKE_LINEUP[0].performanceAt.toLocalDate(),
                        isDDay = false,
                        lineupItems = FAKE_LINEUP.map { it.toUiModel() },
                    ),
                )

            // when
            advanceUntilIdle()

            // then
            val actual = homeViewModel.lineupUiState.value
            assertThat(actual).isInstanceOf(LineupUiState.Success::class.java)

            val actualItems = (actual as LineupUiState.Success).lineups
            assertThat(actualItems)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedLineup)
        }

    @Test
    fun `축제 정보를 불러오는 동안은 Loading 상태로 전환한다`() =
        runTest {
            // given
            val results = mutableListOf<FestivalUiState>()
            val job =
                launch(UnconfinedTestDispatcher()) {
                    homeViewModel.festivalUiState.collect { results.add(it) }
                }

            // when
            homeViewModel.loadFestival()

            // then
            testScheduler.runCurrent()
            assertThat(results).contains(FestivalUiState.Loading)

            advanceUntilIdle()
            assertThat(results.last()).isInstanceOf(FestivalUiState.Success::class.java)

            job.cancel()
        }

    @Test
    fun `축제 정보를 불러오는 데 실패하면 Error 상태로 전환한다`() =
        runTest {
            // given
            val exception = Throwable("Network Error")
            coEvery { festivalRepository.getFestivalInfo() } returns Result.failure(exception)

            // when: 정보를 불러옴
            homeViewModel.loadFestival()
            advanceUntilIdle()

            // then
            val actual = homeViewModel.festivalUiState.value
            assertThat(actual).isInstanceOf(FestivalUiState.Error::class.java)
        }

    @Test
    fun `스케줄 이동 이벤트를 발생시킬 수 있다`() =
        runTest {
            // given
            val events = mutableListOf<Unit>()
            val job =
                launch(UnconfinedTestDispatcher()) {
                    homeViewModel.navigateToScheduleEvent.collect { events.add(it) }
                }

            // when
            homeViewModel.navigateToScheduleClick()

            // then
            assertThat(events).hasSize(1)

            job.cancel()
        }
}
