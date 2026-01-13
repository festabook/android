package com.daedan.festabook.presentation.placeMap.intent.state

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
suspend inline fun <reified R> StateFlow<PlaceMapUiState>.await(
    timeout: Duration = 3.seconds,
    onTimeout: (Throwable) -> Unit = {},
    crossinline selector: (PlaceMapUiState) -> Any?,
): R =
    try {
        withTimeout(timeout) {
            this@await
                .map { selector(it) }
                .distinctUntilChanged()
                .filterIsInstance<R>()
                .first()
        }
    } catch (e: TimeoutCancellationException) {
        onTimeout(e)
        throw e
    }
