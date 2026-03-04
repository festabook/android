package com.daedan.festabook.presentation.placeMap.intent.handler

interface SideEffectHandler<EVENT> {
    suspend operator fun invoke(event: EVENT)
}
