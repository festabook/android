package com.daedan.festabook.explore

import com.daedan.festabook.domain.model.University
import com.daedan.festabook.domain.repository.ExploreRepository
import com.daedan.festabook.presentation.explore.ExploreSideEffect
import com.daedan.festabook.presentation.explore.ExploreViewModel
import com.daedan.festabook.presentation.explore.SearchUiState
import com.daedan.festabook.presentation.explore.model.SearchResultUiModel
import com.daedan.festabook.presentation.explore.model.toUiModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var exploreRepository: ExploreRepository
    private lateinit var exploreViewModel: ExploreViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        exploreRepository = mockk(relaxed = true)
        coEvery { exploreRepository.search(any()) } returns Result.success(emptyList())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `뷰모델을 생성하면 저장된 축제 id가 있는지 확인한다`() =
        runTest {
            // given
            coEvery { exploreRepository.getFestivalId() } returns 1L

            // when
            exploreViewModel = ExploreViewModel(exploreRepository)
            advanceUntilIdle()

            // then
            coVerify { exploreRepository.getFestivalId() }
            assertThat(exploreViewModel.uiState.value.hasFestivalId).isTrue()
        }

    @Test
    fun `검색어가 변경되면 query 상태가 업데이트된다`() =
        runTest {
            // given
            exploreViewModel = ExploreViewModel(exploreRepository)
            val query = "테스트"

            // when
            exploreViewModel.onTextInputChanged(query)
            advanceUntilIdle()

            // then
            assertThat(exploreViewModel.uiState.value.query).isEqualTo(query)
        }

    @Test
    fun `검색어가 변경되고 일정 시간이 지나면 검색을 수행하고 성공 시 결과를 업데이트한다`() =
        runTest {
            // given
            exploreViewModel = ExploreViewModel(exploreRepository)
            val query = "테스트"
            val universities =
                listOf(
                    University(
                        festivalId = 1L,
                        universityName = "테스트대학교",
                        festivalName = "테스트축제",
                        startDate = "2024-05-01",
                        endDate = "2024-05-03",
                    ),
                    University(
                        festivalId = 2L,
                        universityName = "테스트대학교2",
                        festivalName = "테스트축제2",
                        startDate = "2024-09-01",
                        endDate = "2024-09-03",
                    ),
                )
            val uiModels = universities.map { it.toUiModel() }

            coEvery { exploreRepository.search(query) } returns Result.success(universities)

            // when
            exploreViewModel.onTextInputChanged(query)
            advanceTimeBy(500L) // Debounce time (300L) 보다 충분히 길게
            advanceUntilIdle()

            // then
            coVerify { exploreRepository.search(query) }
            val searchState = exploreViewModel.uiState.value.searchState
            assertThat(searchState).isInstanceOf(SearchUiState.Success::class.java)
            assertThat((searchState as SearchUiState.Success).universitiesFound)
                .isEqualTo(uiModels)
        }

    @Test
    fun `검색어가 비어있으면 Idle 상태로 변경된다`() =
        runTest {
            // given
            exploreViewModel = ExploreViewModel(exploreRepository)
            exploreViewModel.onTextInputChanged("이전검색어")
            advanceTimeBy(500L)
            advanceUntilIdle()

            // when
            exploreViewModel.onTextInputChanged("")
            advanceTimeBy(500L) // Debounce time 보다 충분히 길게
            advanceUntilIdle()

            // then
            assertThat(exploreViewModel.uiState.value.searchState)
                .isEqualTo(SearchUiState.Idle)
        }

    @Test
    fun `검색 실패 시 Error 상태로 업데이트된다`() =
        runTest {
            // given
            exploreViewModel = ExploreViewModel(exploreRepository)
            val query = "에러발생"
            val exception = Exception("Network Error")
            coEvery { exploreRepository.search(query) } returns Result.failure(exception)

            // when
            exploreViewModel.onTextInputChanged(query)
            advanceTimeBy(500L)
            advanceUntilIdle()

            // then
            coVerify { exploreRepository.search(query) }
            val searchState = exploreViewModel.uiState.value.searchState
            assertThat(searchState).isInstanceOf(SearchUiState.Error::class.java)
            assertThat((searchState as SearchUiState.Error).throwable).isEqualTo(exception)
        }

    @Test
    fun `대학교가 선택되었을 때 축제 Id를 저장하고 Main으로 이동하는 이벤트를 발생시킨다`() =
        runTest {
            // given
            exploreViewModel = ExploreViewModel(exploreRepository)
            val searchResult =
                SearchResultUiModel(
                    1L,
                    "테스트대학교",
                    "테스트축제",
                )
            val collectedSideEffects = mutableListOf<ExploreSideEffect>()

            // SideEffect 수집 시작
            val job =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    exploreViewModel.sideEffect.collect {
                        collectedSideEffects.add(it)
                    }
                }

            // when
            exploreViewModel.onUniversitySelected(searchResult)
            advanceUntilIdle()

            // then
            coVerify { exploreRepository.saveFestivalId(searchResult.festivalId) }
            assertThat(collectedSideEffects).hasSize(1)
            assertThat(collectedSideEffects.first())
                .isEqualTo(ExploreSideEffect.NavigateToMain(searchResult))

            job.cancel()
        }
}
