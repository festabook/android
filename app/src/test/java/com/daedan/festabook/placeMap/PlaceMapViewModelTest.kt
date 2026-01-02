package com.daedan.festabook.placeMap

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.daedan.festabook.di.placeMapHandler.PlaceMapHandlerGraph
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.domain.repository.PlaceListRepository
import com.daedan.festabook.observeEvent
import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.intent.action.FilterAction
import com.daedan.festabook.presentation.placeMap.intent.action.MapEventAction
import com.daedan.festabook.presentation.placeMap.intent.action.SelectAction
import com.daedan.festabook.presentation.placeMap.intent.event.PlaceMapEvent
import com.daedan.festabook.presentation.placeMap.intent.state.ListLoadState
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.model.toUiModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class PlaceMapViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()

    private val handlerGraphFactory = mockk<PlaceMapHandlerGraph.Factory>(relaxed = true)
    private lateinit var placeListRepository: PlaceListRepository
    private lateinit var placeMapViewModel: PlaceMapViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        placeListRepository = mockk(relaxed = true)
        coEvery { placeListRepository.getPlaces() } returns Result.success(FAKE_PLACES)
        coEvery { placeListRepository.getPlaceGeographies() } returns
            Result.success(
                FAKE_PLACE_GEOGRAPHIES,
            )
        coEvery { placeListRepository.getOrganizationGeography() } returns
            Result.success(
                FAKE_ORGANIZATION_GEOGRAPHY,
            )
        coEvery { placeListRepository.getTimeTags() } returns
            Result.success(
                listOf(
                    FAKE_TIME_TAG,
                ),
            )

        placeMapViewModel =
            PlaceMapViewModel(
                placeListRepository,
                handlerGraphFactory,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `뷰모델을 생성했을 때 전체 타임태그, 선택된 타임태그를 불러올 수 있다`() =
        runTest {
            // given - when
            placeMapViewModel =
                PlaceMapViewModel(placeListRepository, handlerGraphFactory)
            advanceUntilIdle()

            // then
            val uiState = placeMapViewModel.uiState.value
            val actualAllTimeTag = uiState.timeTags
            val actualSelectedTimeTag = uiState.selectedTimeTag
            assertThat(actualAllTimeTag).isEqualTo(
                LoadState.Success(
                    listOf(FAKE_TIME_TAG),
                ),
            )
            assertThat(actualSelectedTimeTag).isEqualTo(
                LoadState.Success(FAKE_TIME_TAG),
            )
        }

    @Test
    fun `뷰모델을 생성했을 때 모든 플레이스 정보를 불러올 수 있다`() =
        runTest {
            // given
            coEvery { placeListRepository.getPlaces() } returns Result.success(FAKE_PLACES)

            // when
            placeMapViewModel = PlaceMapViewModel(placeListRepository, handlerGraphFactory)
            advanceUntilIdle()

            // then
            val expected = FAKE_PLACES.map { it.toUiModel() }
            val uiState = placeMapViewModel.uiState.value
            val actual = uiState.places
            coVerify { placeListRepository.getPlaces() }
            assertThat(actual).isEqualTo(ListLoadState.PlaceLoaded(expected))
        }

    @Test
    fun `뷰모델을 생성했을 때 타임 태그가 없다면 빈 리스트와 Empty타임 태그를 불러온다`() =
        runTest {
            // given
            coEvery {
                placeListRepository.getTimeTags()
            } returns Result.success(emptyList())

            // when
            placeMapViewModel = PlaceMapViewModel(placeListRepository, handlerGraphFactory)
            advanceUntilIdle()

            // then
            val uiState = placeMapViewModel.uiState.value
            val actualAllTimeTag = uiState.timeTags
            val actualSelectedTimeTag = uiState.selectedTimeTag
            assertThat(actualAllTimeTag).isEqualTo(
                LoadState.Success(emptyList<TimeTag>()),
            )
            assertThat(actualSelectedTimeTag).isEqualTo(
                LoadState.Empty,
            )
        }

    @Test
    fun `뷰모델을 생성했을 때 모든 플레이스의 지도 좌표 정보를 불러올 수 있다`() =
        runTest {
            // given
            coEvery { placeListRepository.getPlaceGeographies() } returns
                Result.success(
                    FAKE_PLACE_GEOGRAPHIES,
                )

            // when
            placeMapViewModel = PlaceMapViewModel(placeListRepository, handlerGraphFactory)
            advanceUntilIdle()

            // then
            val expected = FAKE_PLACE_GEOGRAPHIES.map { it.toUiModel() }
            val uiState = placeMapViewModel.uiState.value
            val actual = uiState.placeGeographies
            coVerify { placeListRepository.getPlaceGeographies() }
            assertThat(actual).isEqualTo(LoadState.Success(expected))
        }

    @Test
    fun `뷰모델을 생성했을 때 초기 학교 지리 정보를 불러올 수 있다`() =
        runTest {
            // given
            coEvery { placeListRepository.getOrganizationGeography() } returns
                Result.success(
                    FAKE_ORGANIZATION_GEOGRAPHY,
                )

            // when
            placeMapViewModel = PlaceMapViewModel(placeListRepository, handlerGraphFactory)
            advanceUntilIdle()

            // then
            val expected = FAKE_ORGANIZATION_GEOGRAPHY.toUiModel()
            val uiState = placeMapViewModel.uiState.value
            val actual = uiState.initialMapSetting
            assertThat(actual).isEqualTo(LoadState.Success(expected))
        }

    @Test
    fun `뷰모델을 생성했을 때 정보 로드에 실패하면 독립적으로 에러 상태를 표시한다`() =
        runTest {
            // given
            val exception = Throwable("테스트")
            coEvery { placeListRepository.getPlaces() } returns Result.failure(exception)
            coEvery { placeListRepository.getOrganizationGeography() } returns
                Result.success(
                    FAKE_ORGANIZATION_GEOGRAPHY,
                )
            coEvery { placeListRepository.getPlaceGeographies() } returns Result.failure(exception)

            // when
            placeMapViewModel = PlaceMapViewModel(placeListRepository, handlerGraphFactory)
            advanceUntilIdle()

            // then
            val uiState = placeMapViewModel.uiState.value
            val expected2 =
                LoadState.Success(FAKE_ORGANIZATION_GEOGRAPHY.toUiModel())
            val actual2 = uiState.initialMapSetting

            val expected3 = LoadState.Error(exception)
            val actual3 = uiState.placeGeographies

            assertThat(actual2).isEqualTo(expected2)
            assertThat(actual3).isEqualTo(expected3)
        }

    @Test
    fun `특정 액션을 받으면 액션 핸들러가 호출된다`() =
        runTest {
            // given
            val fakeHandlerGraph = mockk<PlaceMapHandlerGraph>(relaxed = true)
            coEvery {
                handlerGraphFactory.create(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns fakeHandlerGraph

            // when
            placeMapViewModel = PlaceMapViewModel(placeListRepository, handlerGraphFactory)
            placeMapViewModel.onPlaceMapAction(SelectAction.UnSelectPlace)
            placeMapViewModel.onPlaceMapAction(FilterAction.OnPlaceLoad)
            placeMapViewModel.onPlaceMapAction(MapEventAction.OnMapDrag)
            advanceUntilIdle()

            // then
            coVerify(exactly = 1) { fakeHandlerGraph.filterActionHandler }
            coVerify(exactly = 1) { fakeHandlerGraph.selectActionHandler }
            coVerify(exactly = 1) { fakeHandlerGraph.mapEventActionHandler }
        }

    @Test
    fun `메뉴 아이템 재클릭 이벤트를 발송할 수 있다`() =
        runTest {
            // given
            val event = observeEvent(placeMapViewModel.placeMapUiEvent)

            // when
            placeMapViewModel.onMenuItemReClicked()
            val result = event.await()
            advanceUntilIdle()

            // then
            assertThat(result).isInstanceOf(PlaceMapEvent.MenuItemReClicked::class.java)
        }

    @Test
    fun `LoadState가 하나라도 에러가 있다면 에러 이벤트를 발송할 수 있다`() =
        runTest {
            // given
            val throwable = Throwable()
            coEvery { placeListRepository.getPlaceGeographies() } returns Result.failure(throwable)

            // when
            placeMapViewModel = PlaceMapViewModel(placeListRepository, handlerGraphFactory)
            val event = observeEvent(placeMapViewModel.placeMapUiEvent)
            advanceUntilIdle()

            // then
            val result = event.await()
            advanceUntilIdle()

            assertThat(result).isEqualTo(
                PlaceMapEvent.ShowErrorSnackBar(
                    LoadState.Error(throwable),
                ),
            )
        }

    @Test
    fun `ListLoadState가 하나라도 에러가 있다면 에러 이벤트를 발송할 수 있다`() =
        runTest {
            // given
            val throwable = Throwable()
            coEvery { placeListRepository.getPlaces() } returns Result.failure(throwable)

            // when
            placeMapViewModel = PlaceMapViewModel(placeListRepository, handlerGraphFactory)
            val event = observeEvent(placeMapViewModel.placeMapUiEvent)
            advanceUntilIdle()

            // then
            val result = event.await()
            advanceUntilIdle()

            assertThat(result).isEqualTo(
                PlaceMapEvent.ShowErrorSnackBar(
                    LoadState.Error(throwable),
                ),
            )
        }
}
