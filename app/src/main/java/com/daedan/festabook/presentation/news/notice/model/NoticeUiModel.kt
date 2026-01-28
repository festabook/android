package com.daedan.festabook.presentation.news.notice.model

import android.os.Parcelable
import com.daedan.festabook.domain.model.Notice
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.time.format.DateTimeFormatter

@Parcelize
@Serializable
data class NoticeUiModel(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val isPinned: Boolean = false,
    val isExpanded: Boolean = false,
) : Parcelable {
    val formattedCreatedAt: String
        get() =
            runCatching {
                formatter.format(createdAt.toJavaLocalDateTime())
            }.getOrDefault("")

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
    }
}

fun Notice.toUiModel() =
    NoticeUiModel(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        isPinned = isPinned,
    )
