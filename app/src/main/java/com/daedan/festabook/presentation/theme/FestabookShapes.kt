package com.daedan.festabook.presentation.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

data class FestabookShapes(
    val radius1: CornerBasedShape = RoundedCornerShape(6.dp),
    val radius2: CornerBasedShape = RoundedCornerShape(10.dp),
    val radius3: CornerBasedShape = RoundedCornerShape(16.dp),
    val radius4: CornerBasedShape = RoundedCornerShape(20.dp),
    val radius5: CornerBasedShape = RoundedCornerShape(24.dp),
    val radiusFull: CornerBasedShape = RoundedCornerShape(999.dp),
)

val LocalShapes = staticCompositionLocalOf { FestabookShapes() }

val festabookShapes: FestabookShapes
    @Composable
    @ReadOnlyComposable
    get() = LocalShapes.current
