package com.daedan.festabook.presentation.placeMap.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.naver.maps.map.NaverMap
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MapDelegate {
    var value: NaverMap? by mutableStateOf(null)
        private set

    fun initMap(map: NaverMap) {
        value = map
    }

    suspend fun await(timeout: Duration = 3.seconds): NaverMap =
        withTimeout(timeout) {
            snapshotFlow { value }
                .distinctUntilChanged()
                .filterNotNull()
                .first()
        }
}
