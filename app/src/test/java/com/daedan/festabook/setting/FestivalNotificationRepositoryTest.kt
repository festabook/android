package com.daedan.festabook.setting

import com.daedan.festabook.data.datasource.local.DeviceLocalDataSource
import com.daedan.festabook.data.datasource.local.FestivalLocalDataSource
import com.daedan.festabook.data.datasource.local.FestivalNotificationLocalDataSource
import com.daedan.festabook.data.datasource.remote.ApiResult
import com.daedan.festabook.data.datasource.remote.festival.FestivalNotificationDataSource
import com.daedan.festabook.data.model.response.festival.FestivalNotificationResponse
import com.daedan.festabook.data.model.response.festival.RegisteredFestivalNotificationResponse
import com.daedan.festabook.data.repository.FestivalNotificationRepositoryImpl
import com.daedan.festabook.domain.repository.FestivalNotificationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FestivalNotificationRepositoryTest {
    private lateinit var festivalNotificationRepository: FestivalNotificationRepository
    private val festivalNotificationDataSource: FestivalNotificationDataSource =
        mockk(
            relaxed = true,
        )
    private val deviceLocalDataSource: DeviceLocalDataSource =
        mockk(
            relaxed = true,
        )
    private val festivalNotificationLocalDataSource: FestivalNotificationLocalDataSource =
        mockk(
            relaxed = true,
        )
    private val festivalLocalDataSource: FestivalLocalDataSource =
        mockk(
            relaxed = true,
        )

    @BeforeEach
    fun setup() {
        coEvery {
            deviceLocalDataSource.getDeviceId()
        } returns 1
        coEvery {
            festivalLocalDataSource.getFestivalId()
        } returns 1

        festivalNotificationRepository =
            FestivalNotificationRepositoryImpl(
                festivalNotificationDataSource,
                deviceLocalDataSource,
                festivalNotificationLocalDataSource,
                festivalLocalDataSource,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Notification ID를 저장할 수 있다 `() =
        runTest {
            // given
            coEvery {
                festivalNotificationDataSource.saveFestivalNotification(any(), any())
            } returns
                ApiResult.Success(
                    FestivalNotificationResponse(10),
                )

            // when
            festivalNotificationRepository.saveFestivalNotification()

            // then
            coVerify(exactly = 1) {
                festivalNotificationDataSource.saveFestivalNotification(1, 1)
                festivalNotificationLocalDataSource.saveFestivalNotificationId(1, 10)
            }
        }

    @Test
    fun `Notification ID를 삭제할 수 있다`() =
        runTest {
            // given
            coEvery {
                festivalNotificationLocalDataSource.getFestivalNotificationId(1)
            } returns 10
            coEvery {
                festivalNotificationDataSource.deleteFestivalNotification(10)
            } returns ApiResult.Success(Unit)

            // when
            festivalNotificationRepository.deleteFestivalNotification()

            // then
            coVerify(exactly = 1) {
                festivalNotificationDataSource.deleteFestivalNotification(10)
                festivalNotificationLocalDataSource.deleteFestivalNotificationId(1)
            }
        }

    @Test
    fun `서버에서 Notification ID 삭제에 실패하면 로컬에 ID를 삭제하지 않는다`() =
        runTest {
            // given
            coEvery {
                festivalNotificationLocalDataSource.getFestivalNotificationId(1)
            } returns 10
            coEvery {
                festivalNotificationDataSource.deleteFestivalNotification(10)
            } returns ApiResult.ServerError(500, "", "")

            // when
            festivalNotificationRepository.deleteFestivalNotification()

            // then
            coVerify(exactly = 0) {
                festivalNotificationLocalDataSource.deleteFestivalNotificationId(1)
            }
        }

    @Test
    fun `서버에서 Notification ID 저장에 실패하면 로컬에 ID를 저장하지 않는다`() =
        runTest {
            // given
            coEvery {
                festivalNotificationDataSource.saveFestivalNotification(any(), any())
            } returns
                ApiResult.ServerError(500, "", "")

            // when
            festivalNotificationRepository.saveFestivalNotification()

            // then
            coVerify(exactly = 0) {
                festivalNotificationLocalDataSource.saveFestivalNotificationId(1, 10)
            }
        }

    @Test
    fun `서버에 알람이 등록되어있지 않다면 로컬에 Notification 정보를 삭제할 수 있다`() =
        runTest {
            // given
            coEvery {
                festivalNotificationLocalDataSource.getFestivalNotificationId(1)
            } returns 1

            coEvery {
                festivalNotificationDataSource.getFestivalNotification(1)
            } returns ApiResult.Success(listOf())

            // when
            val result = festivalNotificationRepository.syncFestivalNotificationIsAllow()

            // then
            assertThat(result.getOrNull()).isFalse()
            coVerify(exactly = 1) {
                festivalNotificationLocalDataSource.saveFestivalNotificationIsAllowed(1, false)
                festivalNotificationLocalDataSource.deleteFestivalNotificationId(1)
            }
        }

    @Test
    fun `서버에 알람이 등록되어 있다면 로컬에 Notification 정보를 저장할 수 있다`() =
        runTest {
            // given
            coEvery {
                festivalNotificationLocalDataSource.getFestivalNotificationId(1)
            } returns -1

            coEvery {
                festivalNotificationDataSource.getFestivalNotification(1)
            } returns
                ApiResult.Success(
                    listOf(
                        RegisteredFestivalNotificationResponse(
                            festivalNotificationId = 10,
                            festivalId = 1,
                            organizationName = "test",
                            festivalName = "test",
                        ),
                    ),
                )

            // when
            val result = festivalNotificationRepository.syncFestivalNotificationIsAllow()

            // then
            assertThat(result.getOrNull()).isTrue()
            coVerify(exactly = 1) {
                festivalNotificationLocalDataSource.saveFestivalNotificationIsAllowed(1, true)
                festivalNotificationLocalDataSource.saveFestivalNotificationId(1, 10)
            }
        }
}
