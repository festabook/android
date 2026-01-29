package com.daedan.festabook.domain.repository

interface FestivalNotificationRepository {
    suspend fun saveFestivalNotification(): Result<Unit>

    suspend fun deleteFestivalNotification(): Result<Unit>

    suspend fun syncFestivalNotificationIsAllow(): Result<Boolean>

    fun getFestivalNotificationIsAllow(): Boolean

    fun setFestivalNotificationIsAllow(isAllowed: Boolean)
}
