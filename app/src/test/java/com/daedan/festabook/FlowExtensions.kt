package com.daedan.festabook

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
fun <T> TestScope.observeEvent(flow: Flow<T>): Deferred<T> {
    val event =
        backgroundScope.async {
            flow
                .timeout(3.seconds)
                .first()
        }
    advanceUntilIdle()
    return event
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
fun <T> TestScope.observeMultipleEvent(
    flow: Flow<T>,
    result: MutableList<T>,
) {
    backgroundScope.launch(UnconfinedTestDispatcher()) {
        flow
            .timeout(3.seconds)
            .collect {
                result.add(it)
            }
    }
}
