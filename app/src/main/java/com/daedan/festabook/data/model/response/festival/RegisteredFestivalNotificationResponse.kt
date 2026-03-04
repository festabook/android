package com.daedan.festabook.data.model.response.festival

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisteredFestivalNotificationResponse(
    @SerialName("festivalNotificationId")
    val festivalNotificationId: Long,
    @SerialName("festivalId")
    val festivalId: Long,
    @SerialName("organizationName")
    val organizationName: String,
    @SerialName("festivalName")
    val festivalName: String,
)
