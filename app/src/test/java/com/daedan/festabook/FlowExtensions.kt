package com.daedan.festabook

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> TestScope.observeEvent(flow: Flow<T>): Deferred<T> {
    val event =
        backgroundScope.async {
            flow.first()
        }
    advanceUntilIdle()
    return event
}
