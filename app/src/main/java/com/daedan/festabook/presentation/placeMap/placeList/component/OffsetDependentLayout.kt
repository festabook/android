package com.daedan.festabook.presentation.placeMap.placeList.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import kotlin.math.roundToInt

@Composable
fun OffsetDependentLayout(
    offset: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        content = content,
        modifier = modifier,
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(constraints)

        // 부모의 크기를 결정
        layout(placeable.width, placeable.height + offset.roundToInt()) {
            // offset만큼 배치
            val finalYPosition = offset.roundToInt() - placeable.height
            placeable.placeRelative(x = 0, y = finalYPosition)
        }
    }
}
