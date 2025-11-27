package com.daedan.festabook.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme =
    lightColorScheme(
        background = FestabookColor.white,
    )

@Composable
fun FestabookTheme(content: @Composable () -> Unit) {
    val spacing = FestabookSpacing()
    val shapes = FestabookShapes()
    CompositionLocalProvider(
        LocalSpacing provides spacing,
        LocalShapes provides shapes,
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = FestabookTypography,
            content = content,
        )
    }
}
