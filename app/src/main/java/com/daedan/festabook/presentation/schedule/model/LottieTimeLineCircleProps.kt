package com.daedan.festabook.presentation.schedule.model

import androidx.compose.ui.graphics.Color

data class LottieTimeLineCircleProps(
    val centerColor: Color,
    val outerOpacity: Float,
    val innerOpacity: Float,
    val outerColor: Color,
    val innerColor: Color,
    val centerKeyPath: List<String> = listOf("centerCircle", "**", "Fill 1"),
    val outerKeyPath: List<String> = listOf("outerWave", "**", "Fill 1"),
    val innerKeyPath: List<String> = listOf("innerWave", "**", "Fill 1"),
)
