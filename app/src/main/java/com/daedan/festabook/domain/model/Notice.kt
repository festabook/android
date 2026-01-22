package com.daedan.festabook.domain.model

import kotlinx.datetime.LocalDateTime

data class Notice(
    val id: Long,
    val title: String,
    val content: String,
    val isPinned: Boolean,
    val createdAt: LocalDateTime,
)
