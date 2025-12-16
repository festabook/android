package com.daedan.festabook.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class FestabookColorPalette(
    val black: Color = Color(0xFF1B1B1B),
    val gray800: Color = Color(0xFF393939),
    val gray700: Color = Color(0xFF555555),
    val gray600: Color = Color(0xFF717171),
    val gray500: Color = Color(0xFF8E8E8E),
    val gray400: Color = Color(0xFFAAAAAA),
    val gray300: Color = Color(0xFFC6C6C6),
    val gray200: Color = Color(0xFFE3E3E3),
    val gray100: Color = Color(0xFFF7F7F7),
    val white: Color = Color(0xFFFAFAFA),
    val accentBlue: Color = Color(0xFF0094FF),
    val accentGreen: Color = Color(0xFF00AB40),
    val error: Color = Color(0xFFFF4B3E),
)

val LocalColor = staticCompositionLocalOf { FestabookColorPalette() }

val FestabookColor: FestabookColorPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalColor.current
