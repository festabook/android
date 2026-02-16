package com.daedan.festabook.data.model.response

import com.daedan.festabook.domain.model.University
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UniversityResponse(
    @SerialName("festivalId")
    val festivalId: Long,
    @SerialName("organizationName")
    val organizationName: String,
    @SerialName("festivalName")
    val festivalName: String,
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDate")
    val endDate: String,
)

fun UniversityResponse.toDomain() =
    University(
        festivalId = festivalId,
        universityName = organizationName,
        festivalName = festivalName,
        startDate = startDate,
        endDate = endDate,
    )
