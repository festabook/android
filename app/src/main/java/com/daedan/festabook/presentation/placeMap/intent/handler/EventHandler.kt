package com.daedan.festabook.presentation.placeMap.intent.handler

interface EventHandler<EVENT> {
    suspend operator fun invoke(event: EVENT)
}
