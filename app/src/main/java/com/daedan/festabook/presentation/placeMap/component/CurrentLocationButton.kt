package com.daedan.festabook.presentation.placeMap.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.widget.LocationButtonView

@Composable
fun CurrentLocationButton(
    modifier: Modifier = Modifier,
    map: NaverMap? = null,
) {
    AndroidView(
        modifier = modifier,
        factory = { context -> LocationButtonView(context) },
        update = { view -> view.map = map },
    )
}
