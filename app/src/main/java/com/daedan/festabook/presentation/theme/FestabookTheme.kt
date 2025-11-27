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
    CompositionLocalProvider(
        LocalSpacing provides spacing,
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            shapes = FestabookShapesTheme,
            typography = FestabookTypography,
            content = content,
        )
    }
}
