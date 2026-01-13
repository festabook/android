package com.daedan.festabook.placeMap.handler

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.domain.repository.PlaceDetailRepository
import com.daedan.festabook.observeEvent
import com.daedan.festabook.placeDetail.FAKE_ETC_PLACE_DETAIL
import com.daedan.festabook.placeDetail.FAKE_PLACE_DETAIL
import com.daedan.festabook.placeMap.FAKE_TIME_TAG
import com.daedan.festabook.presentation.placeDetail.model.toUiModel
import com.daedan.festabook.presentation.placeMap.intent.action.SelectAction
import com.daedan.festabook.presentation.placeMap.intent.event.MapControlSideEffect
import com.daedan.festabook.presentation.placeMap.intent.event.PlaceMapSideEffect
import com.daedan.festabook.presentation.placeMap.intent.handler.SelectActionHandler
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
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
class SelectActionHandlerTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var selectActionHandler: SelectActionHandler

    private lateinit var uiState: MutableStateFlow<PlaceMapUiState>

    private lateinit var placeDetailRepository: PlaceDetailRepository

    private val mapControlUiEvent: Channel<MapControlSideEffect> =
        Channel(
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    private val placeMapUiEvent: Channel<PlaceMapSideEffect> =
        Channel(
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        placeDetailRepository = mockk()
        uiState = MutableStateFlow(PlaceMapUiState())

        selectActionHandler =
            SelectActionHandler(
                _mapControlSideEffect = mapControlUiEvent,
                _placeMapSideEffect = placeMapUiEvent,
                filterActionHandler = mockk(relaxed = true),
                logger = mockk(relaxed = true),
                uiState = uiState,
                onUpdateState = { uiState.update(it) },
                scope = CoroutineScope(testDispatcher),
                placeDetailRepository = placeDetailRepository,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `플레이스의 아이디와 카테고리가 있으면 플레이스 상세를 선택할 수 있다`() =
        runTest {
            // given
            coEvery { placeDetailRepository.getPlaceDetail(1) } returns
                Result.success(
                    FAKE_PLACE_DETAIL,
                )
            val eventResult = observeEvent(mapControlUiEvent.receiveAsFlow())

            // when
            selectActionHandler(SelectAction.OnPlaceClick(1))
            advanceUntilIdle()

            // then
            coVerify { placeDetailRepository.getPlaceDetail(1) }

            val event = eventResult.await()
            advanceUntilIdle()

            val expected = LoadState.Success(FAKE_PLACE_DETAIL.toUiModel())
            val actual = uiState.value.selectedPlace
            assertThat(actual).isEqualTo(expected)
            assertThat(event).isEqualTo(MapControlSideEffect.SelectMarker(expected))
        }

    @Test
    fun `카테고리가 기타시설일 떄에도 플레이스 상세를 선택할 수 있다`() =
        runTest {
            // given
            coEvery { placeDetailRepository.getPlaceDetail(1) } returns
                Result.success(
                    FAKE_ETC_PLACE_DETAIL,
                )
            val eventResult = observeEvent(mapControlUiEvent.receiveAsFlow())

            // when
            selectActionHandler(SelectAction.OnPlaceClick(1))
            advanceUntilIdle()

            // then
            val event = eventResult.await()
            advanceUntilIdle()

            val expected = LoadState.Success(FAKE_ETC_PLACE_DETAIL.toUiModel())
            val actual = uiState.value.selectedPlace
            assertThat(actual).isEqualTo(expected)
            assertThat(event).isEqualTo(MapControlSideEffect.SelectMarker(expected))
        }

    @Test
    fun `플레이스 상세 선택을 해제할 수 있다`() =
        runTest {
            // given
            coEvery { placeDetailRepository.getPlaceDetail(1) } returns
                Result.success(
                    FAKE_PLACE_DETAIL,
                )
            selectActionHandler(SelectAction.OnPlaceClick(1))
            val eventResult = observeEvent(mapControlUiEvent.receiveAsFlow())
            advanceUntilIdle()

            // when
            selectActionHandler(SelectAction.UnSelectPlace)
            advanceUntilIdle()

            // then
            val event = eventResult.await()
            advanceUntilIdle()

            val expected = LoadState.Empty
            val actual = uiState.value.selectedPlace
            assertThat(actual).isEqualTo(expected)
            assertThat(event).isEqualTo(MapControlSideEffect.UnselectMarker)
        }

    @Test
    fun `학교로 돌아가기 버튼이 나타나지 않는 임계값을 넣을 수 있다`() =
        runTest {
            // given
            val isExceededMaxLength = true

            // when
            selectActionHandler(SelectAction.ExceededMaxLength(isExceededMaxLength))
            advanceUntilIdle()

            // then
            assertThat(uiState.value.isExceededMaxLength).isEqualTo(isExceededMaxLength)
        }

    @Test
    fun `현재 플레이스를 선택 후, 플레이스 상세로 이벤트를 발생시킬 수 있다`() =
        runTest {
            // given
            coEvery {
                placeDetailRepository.getPlaceDetail(FAKE_PLACE_DETAIL.id)
            } returns Result.success(FAKE_PLACE_DETAIL)

            val eventResult = observeEvent(placeMapUiEvent.receiveAsFlow())
            val expected = LoadState.Success(FAKE_PLACE_DETAIL.toUiModel())
            uiState.update {
                it.copy(
                    selectedPlace = expected,
                    selectedTimeTag = LoadState.Success(FAKE_TIME_TAG),
                )
            }

            // when
            selectActionHandler(
                SelectAction.OnPlacePreviewClick(expected),
            )
            advanceUntilIdle()

            // then
            val event = eventResult.await()
            advanceUntilIdle()

            assertThat(event).isEqualTo(
                PlaceMapSideEffect.StartPlaceDetail(expected),
            )
        }

    @Test
    fun `타임태그가 선택되었음을 알리는 이벤트를 발생시킬 수 있다`() =
        runTest {
            // given
            val expected = TimeTag(1, "테스트1")

            // when
            selectActionHandler(SelectAction.OnTimeTagClick(expected))
            advanceUntilIdle()

            // then
            val actual = uiState.value.selectedTimeTag
            assertThat(actual).isEqualTo(
                LoadState.Success(expected),
            )
        }

    @Test
    fun `뒤로가기가 클릭되었을 때 선택 해제 이벤트를 발생시킬 수 있다`() =
        runTest {
            // given
            val eventResult = observeEvent(mapControlUiEvent.receiveAsFlow())

            // when
            selectActionHandler(SelectAction.OnBackPress)

            // then
            val event = eventResult.await()
            advanceUntilIdle()

            assertThat(event).isEqualTo(MapControlSideEffect.UnselectMarker)
        }
}
