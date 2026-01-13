package com.daedan.festabook.presentation.placeMap.intent.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.daedan.festabook.presentation.placeMap.mapManager.MapManager

class MapManagerDelegate {
    var value: MapManager? by mutableStateOf(null)
        private set

    fun init(manager: MapManager) {
        value = manager
    }
}
