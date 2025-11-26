package com.daedan.festabook.presentation.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

data class FestabookShapes(
    val radius1: CornerBasedShape = RoundedCornerShape(6.dp),
    val radius2: CornerBasedShape = RoundedCornerShape(10.dp),
    val radius3: CornerBasedShape = RoundedCornerShape(16.dp),
    val radius4: CornerBasedShape = RoundedCornerShape(20.dp),
    val radius5: CornerBasedShape = RoundedCornerShape(24.dp),
    val radiusFull: CornerBasedShape = RoundedCornerShape(999.dp),
)

val festabookShapes = FestabookShapes()

val FestabookShapesTheme =
    Shapes(
        extraSmall = festabookShapes.radius1,
        small = festabookShapes.radius2,
        medium = festabookShapes.radius3,
        large = festabookShapes.radius4,
        extraLarge = festabookShapes.radius5,
    )
