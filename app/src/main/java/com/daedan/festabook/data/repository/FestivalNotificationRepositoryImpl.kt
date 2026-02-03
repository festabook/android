package com.daedan.festabook.data.repository

import com.daedan.festabook.data.datasource.local.DeviceLocalDataSource
import com.daedan.festabook.data.datasource.local.FestivalLocalDataSource
import com.daedan.festabook.data.datasource.local.FestivalNotificationLocalDataSource
import com.daedan.festabook.data.datasource.remote.festival.FestivalNotificationDataSource
import com.daedan.festabook.data.util.toResult
import com.daedan.festabook.domain.repository.FestivalNotificationRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class FestivalNotificationRepositoryImpl(
    private val festivalNotificationDataSource: FestivalNotificationDataSource,
    private val deviceLocalDataSource: DeviceLocalDataSource,
    private val festivalNotificationLocalDataSource: FestivalNotificationLocalDataSource,
    private val festivalLocalDataSource: FestivalLocalDataSource,
) : FestivalNotificationRepository {
    override suspend fun saveFestivalNotification(): Result<Unit> {
        val deviceId =
            deviceLocalDataSource.getDeviceId() ?: return Result.failure(
                IllegalArgumentException(NO_DEVICE_ID_EXCEPTION),
            )
        val festivalId =
            festivalLocalDataSource.getFestivalId() ?: return Result.failure(
                IllegalArgumentException(NO_FESTIVAL_ID_EXCEPTION),
            )

        return festivalNotificationDataSource
            .saveFestivalNotification(
                festivalId = festivalId,
                deviceId = deviceId,
            ).toResult()
            .mapCatching { response ->
                festivalNotificationLocalDataSource.saveFestivalNotificationId(
                    festivalId,
                    response.festivalNotificationId,
                )
            }
    }

    override suspend fun deleteFestivalNotification(): Result<Unit> {
        val festivalId =
            festivalLocalDataSource.getFestivalId()
                ?: return Result.failure(IllegalStateException(NO_FESTIVAL_ID_EXCEPTION))
        val festivalNotificationId =
            festivalNotificationLocalDataSource.getFestivalNotificationId(festivalId)
        return festivalNotificationDataSource
            .deleteFestivalNotification(festivalNotificationId)
            .toResult()
            .mapCatching {
                festivalNotificationLocalDataSource.deleteFestivalNotificationId(festivalId)
            }
    }

    override suspend fun syncFestivalNotificationIsAllow(): Result<Boolean> {
        val deviceId =
            deviceLocalDataSource.getDeviceId() ?: return Result.failure(
                IllegalArgumentException(NO_DEVICE_ID_EXCEPTION),
            )
        val festivalId =
            festivalLocalDataSource.getFestivalId() ?: return Result.failure(
                IllegalArgumentException(NO_FESTIVAL_ID_EXCEPTION),
            )

        return festivalNotificationDataSource
            .getFestivalNotification(deviceId)
            .toResult()
            .mapCatching { response ->
                val notificationId =
                    response.find { it.festivalId == festivalId }?.festivalNotificationId
                val isAllowed = notificationId != null
                festivalNotificationLocalDataSource.saveFestivalNotificationIsAllowed(
                    festivalId,
                    isAllowed,
                )
                if (isAllowed) {
                    festivalNotificationLocalDataSource.saveFestivalNotificationId(
                        festivalId,
                        notificationId,
                    )
                } else {
                    festivalNotificationLocalDataSource.deleteFestivalNotificationId(festivalId)
                }
                isAllowed
            }
    }

    override fun getFestivalNotificationIsAllow(): Boolean {
        val festivalId = festivalLocalDataSource.getFestivalId() ?: return false
        return festivalNotificationLocalDataSource.getFestivalNotificationIsAllowed(festivalId)
    }

    override fun setFestivalNotificationIsAllow(isAllowed: Boolean) {
        festivalLocalDataSource.getFestivalId()?.let { festivalId ->
            festivalNotificationLocalDataSource.saveFestivalNotificationIsAllowed(
                festivalId,
                isAllowed,
            )
        }
    }

    companion object {
        private val NO_FESTIVAL_ID_EXCEPTION =
            "${::FestivalNotificationRepositoryImpl.name}: FestivalId가 없습니다."
        private val NO_DEVICE_ID_EXCEPTION =
            "${::FestivalNotificationRepositoryImpl.name}: DeviceId가 없습니다."
    }
}
