package com.daedan.festabook.presentation.placeMap.intent.handler

import kotlinx.coroutines.flow.StateFlow

interface EventHandler<ACTION, STATE> {
    val uiState: StateFlow<STATE>
    val onUpdateState: ((before: STATE) -> STATE) -> Unit

    suspend operator fun invoke(event: ACTION)
}
