package com.daedan.festabook.presentation.placeMap.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.naver.maps.map.widget.LogoView

@Composable
fun NaverMapLogo(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val logoView = remember { LogoView(context) }
    AndroidView(
        factory = { logoView },
        modifier = modifier,
    )
}
