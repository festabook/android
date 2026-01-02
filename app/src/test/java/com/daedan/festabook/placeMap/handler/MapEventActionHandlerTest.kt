package com.daedan.festabook.placeMap.handler

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.daedan.festabook.observeEvent
import com.daedan.festabook.observeMultipleEvent
import com.daedan.festabook.placeMap.FAKE_INITIAL_MAP_SETTING
import com.daedan.festabook.presentation.placeMap.intent.action.MapEventAction
import com.daedan.festabook.presentation.placeMap.intent.event.MapControlEvent
import com.daedan.festabook.presentation.placeMap.intent.event.PlaceMapEvent
import com.daedan.festabook.presentation.placeMap.intent.handler.MapEventActionHandler
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import io.mockk.mockk
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
class MapEventActionHandlerTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mapEventActionHandler: MapEventActionHandler

    private lateinit var uiState: MutableStateFlow<PlaceMapUiState>

    private val mapControlUiEvent: Channel<MapControlEvent> =
        Channel(
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    private val placeMapUiEvent: Channel<PlaceMapEvent> =
        Channel(
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        uiState = MutableStateFlow(PlaceMapUiState())
        mapEventActionHandler =
            MapEventActionHandler(
                uiState = uiState,
                onUpdateState = { uiState.update(it) },
                _mapControlUiEvent = mapControlUiEvent,
                _placeMapUiEvent = placeMapUiEvent,
                logger = mockk(relaxed = true),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `초기 위치로 돌아가기 버튼 클릭 시 이벤트가 방출된다`() =
        runTest {
            // given
            val eventResult = observeEvent(mapControlUiEvent.receiveAsFlow())
            advanceUntilIdle()

            // when
            mapEventActionHandler(MapEventAction.OnBackToInitialPositionClick)

            val event = eventResult.await()
            advanceUntilIdle()

            // then
            assertThat(event).isEqualTo(MapControlEvent.BackToInitialPosition)
        }

    @Test
    fun `지도가 준비되었을 때 지도 관련 로직 초기화 이벤트를 방출할 수 있다`() =
        runTest {
            // given
            val eventResult = mutableListOf<MapControlEvent>()
            observeMultipleEvent(mapControlUiEvent.receiveAsFlow(), eventResult)

            val initialSetting = FAKE_INITIAL_MAP_SETTING
            uiState.update {
                it.copy(initialMapSetting = LoadState.Success(initialSetting))
            }

            // when
            mapEventActionHandler(MapEventAction.OnMapReady)
            advanceUntilIdle()

            // then
            assertThat(eventResult).containsExactly(
                MapControlEvent.InitMap,
                MapControlEvent.InitMapManager(initialSetting),
            )
        }

    @Test
    fun `플레이스 로딩이 완료되었을 때 프리로드 이미지 이벤트를 방출할 수 있다`() =
        runTest {
            // given
            val eventResult = observeEvent(placeMapUiEvent.receiveAsFlow())
            advanceUntilIdle()

            // when
            mapEventActionHandler(MapEventAction.OnPlaceLoadFinish(emptyList()))

            // then
            val event = eventResult.await()
            advanceUntilIdle()
            assertThat(event).isEqualTo(PlaceMapEvent.PreloadImages(emptyList()))
        }

    @Test
    fun `초기 위치로 돌아갔을 때 방출할 수 있다`() =
        runTest {
            // given
            val eventResult = observeEvent(mapControlUiEvent.receiveAsFlow())
            advanceUntilIdle()

            // when
            mapEventActionHandler(MapEventAction.OnBackToInitialPositionClick)
            val event = eventResult.await()
            advanceUntilIdle()

            // then
            assertThat(event).isEqualTo(MapControlEvent.BackToInitialPosition)
        }

    @Test
    fun `지도가 드래그 되었을 때 이벤트를 방출할 수 있다`() =
        runTest {
            // given
            val eventResult = observeEvent(placeMapUiEvent.receiveAsFlow())
            advanceUntilIdle()

            // when
            mapEventActionHandler(MapEventAction.OnMapDrag)

            // then
            val event = eventResult.await()
            advanceUntilIdle()

            assertThat(event).isEqualTo(PlaceMapEvent.MapViewDrag(false))
        }
}
