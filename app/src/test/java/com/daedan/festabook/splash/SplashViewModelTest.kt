package com.daedan.festabook.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.daedan.festabook.data.datasource.local.FestivalLocalDataSource
import com.daedan.festabook.presentation.splash.SplashUiState
import com.daedan.festabook.presentation.splash.SplashViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var festivalLocalDataSource: FestivalLocalDataSource
    private lateinit var splashViewModel: SplashViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        festivalLocalDataSource = mockk(relaxed = true)
        splashViewModel = SplashViewModel(festivalLocalDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `앱 업데이트가 있다면 업데이트 다이얼로그를 표시한다`() =
        runTest {
            // given
            val updateResult = Result.success(true)

            // when
            splashViewModel.handleVersionCheckResult(updateResult)

            // then
            assertThat(splashViewModel.uiState.value).isEqualTo(SplashUiState.ShowUpdateDialog)
        }

    @Test
    fun `앱 업데이트 확인에 실패하면 네트워크 에러 다이얼로그를 표시한다`() =
        runTest {
            // given
            val updateResult = Result.failure<Boolean>(Exception("Network Error"))

            // when
            splashViewModel.handleVersionCheckResult(updateResult)

            // then
            assertThat(splashViewModel.uiState.value).isEqualTo(SplashUiState.ShowNetworkErrorDialog)
        }

    @Test
    fun `앱 업데이트가 없고 접속한 대학교가 있다면 MainActivity로 이동한다`() =
        runTest {
            // given
            every { festivalLocalDataSource.getFestivalId() } returns 1L
            val updateResult = Result.success(false)

            // when
            splashViewModel.handleVersionCheckResult(updateResult)

            // then
            assertThat(splashViewModel.uiState.value).isEqualTo(SplashUiState.NavigateToMain(1L))
            verify(exactly = 1) { festivalLocalDataSource.getFestivalId() }
        }

    @Test
    fun `앱 업데이트가 없고 접속한 대학교가 없다면 ExploreActivity로 이동한다`() =
        runTest {
            // given
            every { festivalLocalDataSource.getFestivalId() } returns null
            val updateResult = Result.success(false)

            // when
            splashViewModel.handleVersionCheckResult(updateResult)

            // then
            assertThat(splashViewModel.uiState.value).isEqualTo(SplashUiState.NavigateToExplore)
            verify(exactly = 1) { festivalLocalDataSource.getFestivalId() }
        }
}
