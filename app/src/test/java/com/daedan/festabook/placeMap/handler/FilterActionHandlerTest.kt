package com.daedan.festabook.placeMap.handler

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.observeMultipleEvent
import com.daedan.festabook.placeMap.FAKE_PLACES_CATEGORY_FIXTURE
import com.daedan.festabook.placeMap.FAKE_TIME_TAG
import com.daedan.festabook.presentation.placeMap.intent.action.FilterAction
import com.daedan.festabook.presentation.placeMap.intent.event.MapControlEvent
import com.daedan.festabook.presentation.placeMap.intent.handler.FilterActionHandler
import com.daedan.festabook.presentation.placeMap.intent.state.ListLoadState
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState
import com.daedan.festabook.presentation.placeMap.intent.state.PlaceMapUiState
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.placeMap.model.toUiModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
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
class FilterActionHandlerTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var filterActionHandler: FilterActionHandler

    private lateinit var cachedPlaces: MutableStateFlow<List<PlaceUiModel>>

    private lateinit var uiState: MutableStateFlow<PlaceMapUiState>

    private val cachedPlaceByTimeTag =
        MutableStateFlow(FAKE_PLACES_CATEGORY_FIXTURE.map { it.toUiModel() })

    private val mapControlUiEvent: Channel<MapControlEvent> =
        Channel(
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        uiState = MutableStateFlow(PlaceMapUiState())
        cachedPlaces = MutableStateFlow(FAKE_PLACES_CATEGORY_FIXTURE.map { it.toUiModel() })

        filterActionHandler =
            FilterActionHandler(
                uiState = uiState,
                _mapControlUiEvent = mapControlUiEvent,
                onUpdateState = { uiState.update(it) },
                onUpdateCachedPlace = { cachedPlaceByTimeTag.tryEmit(it) },
                cachedPlaces = cachedPlaces,
                cachedPlaceByTimeTag = cachedPlaceByTimeTag,
                logger = mockk(relaxed = true),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `선택된 카테고리 값을 선택하면 카테고리 필터 이벤트가 방출되고, 카테고리를 필터링 할 수 있다`() =
        runTest {
            // given
            val categories = setOf(PlaceCategoryUiModel.BOOTH)
            val eventResult = mutableListOf<MapControlEvent>()
            observeMultipleEvent(mapControlUiEvent.consumeAsFlow(), eventResult)
            val places = ListLoadState.Success(FAKE_PLACES_CATEGORY_FIXTURE.map { it.toUiModel() })
            uiState.update { it.copy(places = places) }

            // when
            filterActionHandler(FilterAction.OnCategoryClick(categories))

            // then
            advanceUntilIdle()

            assertThat(
                uiState.value.selectedCategories,
            ).isEqualTo(
                categories,
            )

            assertThat(eventResult).containsExactly(
                MapControlEvent.UnselectMarker,
                MapControlEvent.FilterMapByCategory(categories.toList()),
            )
            assertThat(uiState.value.places).isEqualTo(
                ListLoadState.Success(emptyList<PlaceUiModel>()),
            )
        }

    @Test
    fun `선택된 카테고리가 부스, 주점, 푸드트럭에 해당되지 않을 때 전체 목록을 불러온다`() =
        runTest {
            // given
            val targetCategories =
                setOf(PlaceCategoryUiModel.SMOKING_AREA, PlaceCategoryUiModel.TOILET)
            val places = ListLoadState.Success(FAKE_PLACES_CATEGORY_FIXTURE.map { it.toUiModel() })
            uiState.update { it.copy(places = places) }

            // when
            filterActionHandler(FilterAction.OnCategoryClick(targetCategories))
            advanceUntilIdle()

            // then
            val expected =
                ListLoadState.Success(
                    FAKE_PLACES_CATEGORY_FIXTURE.map { it.toUiModel() },
                )
            val actual = uiState.value.places
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `기타 카테고리만 선택되었다면 전체 목록을 불러온다`() =
        runTest {
            // given
            val targetCategories =
                setOf(PlaceCategoryUiModel.TOILET, PlaceCategoryUiModel.SMOKING_AREA)
            val places = ListLoadState.Success(FAKE_PLACES_CATEGORY_FIXTURE.map { it.toUiModel() })
            uiState.update { it.copy(places = places) }

            // when
            filterActionHandler(FilterAction.OnCategoryClick(targetCategories))
            advanceUntilIdle()

            // then
            val expected =
                ListLoadState.Success(FAKE_PLACES_CATEGORY_FIXTURE.map { it.toUiModel() })
            val actual = uiState.value.places
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `필터링을 해제하면 전체 목록을 반환한다`() =
        runTest {
            // given
            val targetCategories =
                setOf(PlaceCategoryUiModel.FOOD_TRUCK, PlaceCategoryUiModel.BOOTH)
            val places = ListLoadState.Success(FAKE_PLACES_CATEGORY_FIXTURE.map { it.toUiModel() })
            uiState.update { it.copy(places = places) }
            filterActionHandler(FilterAction.OnCategoryClick(targetCategories))

            // when
            filterActionHandler(FilterAction.OnCategoryClick(emptySet()))

            // then
            val expected = FAKE_PLACES_CATEGORY_FIXTURE.map { it.toUiModel() }
            val actual = uiState.value.places
            assertThat(actual).isEqualTo(ListLoadState.Success(expected))
        }

    @Test
    fun `타임 태그를 기준으로 필터링 할 수 있다`() =
        runTest {
            // given
            val expected =
                listOf(
                    FAKE_PLACES_CATEGORY_FIXTURE.first().toUiModel(),
                )
            val places = ListLoadState.Success(FAKE_PLACES_CATEGORY_FIXTURE.map { it.toUiModel() })
            uiState.update { it.copy(places = places) }

            // when
            filterActionHandler.updatePlacesByTimeTag(FAKE_TIME_TAG.timeTagId)

            // then
            val actual = uiState.value.places
            assertThat(actual).isEqualTo(ListLoadState.Success(expected))
        }

    @Test
    fun `타임 태그가 없을 때 전체 목록을 반환한다`() =
        runTest {
            // given
            val expected = FAKE_PLACES_CATEGORY_FIXTURE.map { it.toUiModel() }
            val emptyTimeTag = TimeTag.EMPTY
            val places = ListLoadState.Success(FAKE_PLACES_CATEGORY_FIXTURE.map { it.toUiModel() })
            uiState.update { it.copy(places = places) }

            // when
            filterActionHandler.updatePlacesByTimeTag(emptyTimeTag.timeTagId)
            advanceUntilIdle()

            // then
            val actual = uiState.value.places
            assertThat(actual).isEqualTo(ListLoadState.Success(expected))
        }

    @Test
    fun `플레이스가 로드가 완료되었을 때 선택된 타임 태그로 필터링할 수 있다`() =
        runTest {
            // given
            val expected =
                listOf(
                    FAKE_PLACES_CATEGORY_FIXTURE.first().toUiModel(),
                )
            val places = ListLoadState.Success(FAKE_PLACES_CATEGORY_FIXTURE.map { it.toUiModel() })
            uiState.update {
                it.copy(
                    places = places,
                    selectedTimeTag = LoadState.Success(FAKE_TIME_TAG),
                )
            }

            // when
            filterActionHandler(FilterAction.OnPlaceLoad)
            advanceUntilIdle()

            // then
            val actual = uiState.value.places
            assertThat(actual).isEqualTo(ListLoadState.Success(expected))
        }
}
