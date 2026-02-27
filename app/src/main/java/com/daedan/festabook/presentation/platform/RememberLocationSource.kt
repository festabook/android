package com.daedan.festabook.presentation.platform

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.naver.maps.map.util.FusedLocationSource

private const val LOCATION_PERMISSION_REQUEST_CODE = 1234

@Composable
fun rememberLocationSource(): FusedLocationSource {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    return remember(activity) {
        FusedLocationSource(activity, LOCATION_PERMISSION_REQUEST_CODE)
    }
}
