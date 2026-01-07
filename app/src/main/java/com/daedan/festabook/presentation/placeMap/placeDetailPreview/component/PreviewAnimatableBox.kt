package com.daedan.festabook.presentation.placeMap.placeDetailPreview.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daedan.festabook.presentation.common.component.cardBackground
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.festabookShapes
import kotlinx.coroutines.launch

@Composable
fun PreviewAnimatableBox(
    visible: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = FestabookColor.white,
    borderColor: Color = FestabookColor.gray200,
    shape: Shape = festabookShapes.radius5,
    borderStroke: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val offsetY = remember { Animatable(120f) }
    val alpha = remember { Animatable(0.3f) }

    LaunchedEffect(visible) {
        if (visible) {
            launch {
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(300),
                )
            }
            launch {
                alpha.animateTo(1f, animationSpec = tween(300))
            }
        } else {
            // 나갈 때 애니메이션 (위에서 아래로 + 페이드아웃)
            launch { offsetY.snapTo(120f) }
            launch { alpha.snapTo(0.3f) }
        }
    }

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    translationY = offsetY.value
                    this.alpha = alpha.value
                }.cardBackground(
                    backgroundColor = backgroundColor,
                    borderColor = borderColor,
                    shape = shape,
                    borderStroke = borderStroke,
                ),
    ) {
        content()
    }
}
