package com.daedan.festabook.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme
    @Composable
    get() =
        lightColorScheme(
            background = FestabookColor.white,
        )

@Composable
fun FestabookTheme(content: @Composable () -> Unit) {
    val spacing = FestabookSpacing()
    val shapes = FestabookShapes()
    val color = FestabookColorPalette()
    val typography = FestabookTypographies

    CompositionLocalProvider(
        LocalSpacing provides spacing,
        LocalShapes provides shapes,
        LocalColor provides color,
        LocalTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = FestabookTypography,
            content = content,
        )
    }
}
