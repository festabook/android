package com.daedan.festabook.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme =
    lightColorScheme(
        background = FestabookColor.white,
    )

@Composable
fun FestabookTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        shapes = FestabookShapesTheme,
        typography = FestabookTypography,
        content = content,
    )
}
