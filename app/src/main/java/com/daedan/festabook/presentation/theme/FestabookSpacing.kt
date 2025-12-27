package com.daedan.festabook.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class FestabookSpacing(
    val paddingScreenGutter: Dp = 16.dp,
    val paddingTitleHorizontal: Dp = 40.dp,
    val paddingBody1: Dp = 4.dp,
    val paddingBody2: Dp = 8.dp,
    val paddingBody3: Dp = 12.dp,
    val paddingBody4: Dp = 16.dp,
    val paddingBody5: Dp = 20.dp,
)

val LocalSpacing = staticCompositionLocalOf { FestabookSpacing() }

val festabookSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
