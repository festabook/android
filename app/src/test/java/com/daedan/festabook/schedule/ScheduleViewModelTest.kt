package com.daedan.festabook.schedule

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.daedan.festabook.domain.repository.ScheduleRepository
import com.daedan.festabook.presentation.schedule.ScheduleEventsUiState
import com.daedan.festabook.presentation.schedule.ScheduleUiState
import com.daedan.festabook.presentation.schedule.ScheduleViewModel
import com.daedan.festabook.presentation.schedule.model.toUiModel
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
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private val dateId = 1L
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var scheduleViewModel: ScheduleViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        scheduleRepository = mockk()

        coEvery { scheduleRepository.fetchAllScheduleDates() } returns
            Result.success(FAKE_SCHEDULE_DATES)
        coEvery { scheduleRepository.fetchScheduleEventsById(dateId) } returns
            Result.success(FAKE_SCHEDULE_EVENTS)

        scheduleViewModel = ScheduleViewModel(scheduleRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ViewModel이 생성되면 해당 날짜를 불러온다`() =
        runTest {
            // given
            advanceUntilIdle()

            // when

            // then
            val stateResult = scheduleViewModel.scheduleUiState.value
            val expectedDate = FAKE_SCHEDULE_DATES.map { it.toUiModel() }

            assertAll(
                { coVerify { scheduleRepository.fetchAllScheduleDates() } },
                { coVerify { scheduleRepository.fetchScheduleEventsById(dateId) } },
                { assertTrue(stateResult is ScheduleUiState.Success) },
                { assertEquals(expectedDate, (stateResult as ScheduleUiState.Success).dates) },
            )
        }

    @Test
    fun `ViewModel이 생성되면 날짜에 해당하는 일정들을 불러온다`() =
        runTest {
            // given
            advanceUntilIdle()

            // when
            val state = scheduleViewModel.scheduleUiState.value

            // then
            val successState =
                state as? ScheduleUiState.Success ?: fail("ScheduleUiState.Success 가 아님: $state")

            val eventsState =
                successState.eventsUiStateByPosition[0] as? ScheduleEventsUiState.Success
                    ?: fail("ScheduleEventsUiState.Success 가 아님")

            assertAll(
                { coVerify { scheduleRepository.fetchAllScheduleDates() } },
                { coVerify { scheduleRepository.fetchScheduleEventsById(dateId) } },
                { assertEquals(FAKE_SCHEDULE_EVENTS_UI_MODELS, eventsState.events) },
            )
        }

    @Test
    fun `현재 진행중인 날짜의 인덱스를 불러올 수 있다`() =
        runTest {
            // given
            advanceUntilIdle()

            // when
            val state = scheduleViewModel.scheduleUiState.value

            // then
            val successState =
                state as? ScheduleUiState.Success ?: fail("ScheduleUiState.Success 가 아님: $state")

            assertAll(
                { coVerify { scheduleRepository.fetchAllScheduleDates() } },
                { coVerify { scheduleRepository.fetchScheduleEventsById(dateId) } },
                { assertEquals(0, successState.currentDatePosition) },
            )
        }

    @Test
    fun `현재 진행중인 일정의 인덱스를 불러올 수 있다`() =
        runTest {
            // given
            advanceUntilIdle()

            // when
            val state = scheduleViewModel.scheduleUiState.value

            // then
            val successState =
                state as? ScheduleUiState.Success ?: fail("ScheduleUiState.Success 가 아님: $state")
            val eventsState =
                successState.eventsUiStateByPosition[0] as? ScheduleEventsUiState.Success
                    ?: fail("ScheduleEventsUiState.Success 가 아님")

            assertAll(
                { coVerify { scheduleRepository.fetchAllScheduleDates() } },
                { coVerify { scheduleRepository.fetchScheduleEventsById(dateId) } },
                { assertEquals(0, eventsState.currentEventPosition) },
            )
        }
}
