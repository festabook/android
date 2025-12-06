package com.daedan.festabook.presentation.placeMap.placeList.component

import android.annotation.SuppressLint
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.daedan.festabook.presentation.theme.FestabookColor
import kotlin.math.roundToInt

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun PlaceListBottomSheet(
    peekHeight: Dp,
    halfExpandedRatio: Float,
    modifier: Modifier = Modifier,
    initialState: PlaceListBottomSheetState = PlaceListBottomSheetState.HALF_EXPANDED,
    shape: Shape = PlaceListBottomSheetDefault.bottomSheetBackgroundShape,
    color: Color = PlaceListBottomSheetDefault.bottomSheetBackgroundColor,
    onStateUpdate: (PlaceListBottomSheetState) -> Unit = {},
    dragHandle: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    require(halfExpandedRatio in 0.0..1.0) { "halfExpandedRatio는 0과 1 사이여야 합니다." }
    val density = LocalDensity.current
    val config = LocalConfiguration.current

    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    // 3가지 앵커 높이 정의 (DP)
    val halfExpandedHeightDp = config.screenHeightDp.dp * halfExpandedRatio

    // 앵커 위치 계산 (픽셀)
    val halfExpandedOffsetPx = with(density) { screenHeightPx - halfExpandedHeightDp.toPx() }
    val collapsedOffsetPx = with(density) { screenHeightPx - peekHeight.toPx() }
    val expandedOffsetPx = 0f // 화면 최상단

    // 앵커 생성
    val anchors =
        remember(screenHeightPx) {
            DraggableAnchors {
                PlaceListBottomSheetState.EXPANDED at expandedOffsetPx
                PlaceListBottomSheetState.HALF_EXPANDED at halfExpandedOffsetPx
                PlaceListBottomSheetState.COLLAPSED at collapsedOffsetPx
            }
        }
    val anchoredState = rememberAnchoredState(initialState, anchors)
    val nestedScrollConnection = placeListBottomSheetNestedScrollConnection(anchoredState)

    LaunchedEffect(anchoredState.settledValue) {
        onStateUpdate(anchoredState.settledValue)
    }

    Column(
        modifier =
            modifier
                .nestedScroll(nestedScrollConnection)
                .offset {
                    IntOffset(
                        0,
                        if (anchoredState.offset.isNaN()) 0 else anchoredState.offset.roundToInt(),
                    )
                }.background(
                    color = color,
                    shape = shape,
                ).anchoredDraggable(
                    state = anchoredState,
                    orientation = Orientation.Vertical,
                ),
    ) {
        PlaceListBottomSheetDefault.DefaultDragHandle()
        dragHandle()
        content()
    }
}

/**
 * PlaceListBottomSheet의 기본 스타일을 정의합니다.
 * 기본적인 DragHandle 컴포저블을 정의합니다.
 */
object PlaceListBottomSheetDefault {
    val bottomSheetBackgroundShape: Shape =
        RoundedCornerShape(
            topStart = 30.dp,
            topEnd = 30.dp,
        )

    val bottomSheetBackgroundColor: Color =
        FestabookColor.white

    private val dragHandleVerticalPadding = 12.dp
    private val dragHandleWidth = 32.dp
    private val dragHandleHeight = 4.dp

    private val dragHandleCorner =
        RoundedCornerShape(
            percent = 50,
        )

    private val dragHandleColor =
        FestabookColor.gray400

    @Composable
    fun DefaultDragHandle(modifier: Modifier = Modifier) {
        Box(
            modifier =
                modifier
                    .padding(vertical = dragHandleVerticalPadding)
                    .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(width = dragHandleWidth, height = dragHandleHeight)
                    .background(
                        color = dragHandleColor,
                        shape = dragHandleCorner,
                    ),
            )
        }
    }
}

/** NestedScroll을 위한 Connection 객체를 반환합니다.
 */
private fun placeListBottomSheetNestedScrollConnection(
    anchoredState: AnchoredDraggableState<PlaceListBottomSheetState>,
): NestedScrollConnection {
    return object : NestedScrollConnection {
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource,
        ): Offset =
            if (available.y < 0 && source == NestedScrollSource.UserInput) {
                anchoredState.dispatchRawDelta(available.y).toOffset()
            } else {
                Offset.Zero
            }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset =
            if (source == NestedScrollSource.UserInput) {
                anchoredState.dispatchRawDelta(available.y).toOffset()
            } else {
                Offset.Zero
            }

        override suspend fun onPostFling(
            consumed: Velocity,
            available: Velocity,
        ): Velocity {
            anchoredState.settleImmediately(available)
            return available
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val toFling = available.y
            val currentOffset = anchoredState.requireOffset()
            val minAnchor = anchoredState.anchors.minPosition()
            return if (toFling < 0 && currentOffset > minAnchor) {
                anchoredState.settleImmediately(available)
                available
            } else {
                Velocity.Zero
            }
        }

        private fun Float.toOffset() =
            Offset(
                x = 0f,
                y = this,
            )

        /**
         anchoredState의 기본 settle() 동작은 거리 기반으로 동작합니다.
         거리 기반 동작을, 상태 기반으로 동작하도록 변경하여, 미세한 드래그에도 바텀시트가 펼쳐지도록 합니다.
         */
        private suspend fun AnchoredDraggableState<PlaceListBottomSheetState>.settleImmediately(
            available: Velocity,
            animationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
        ) {
            val targetState =
                if (available.y < 0) {
                    when (anchoredState.currentValue) {
                        PlaceListBottomSheetState.EXPANDED -> anchoredState.currentValue
                        PlaceListBottomSheetState.HALF_EXPANDED -> PlaceListBottomSheetState.EXPANDED
                        PlaceListBottomSheetState.COLLAPSED -> PlaceListBottomSheetState.HALF_EXPANDED
                    }
                } else if (available.y > 0) {
                    when (anchoredState.currentValue) {
                        PlaceListBottomSheetState.EXPANDED -> PlaceListBottomSheetState.HALF_EXPANDED
                        PlaceListBottomSheetState.HALF_EXPANDED -> PlaceListBottomSheetState.COLLAPSED
                        PlaceListBottomSheetState.COLLAPSED -> anchoredState.currentValue
                    }
                } else {
                    anchoredState.currentValue
                }

            animateTo(
                targetValue = targetState,
                animationSpec = animationSpec,
            )
        }
    }
}

@Composable
private fun rememberAnchoredState(
    initialValue: PlaceListBottomSheetState,
    anchors: DraggableAnchors<PlaceListBottomSheetState>,
): AnchoredDraggableState<PlaceListBottomSheetState> =
    remember {
        AnchoredDraggableState(
            initialValue = initialValue,
            anchors = anchors,
        )
    }

enum class PlaceListBottomSheetState {
    EXPANDED,
    HALF_EXPANDED,
    COLLAPSED,
}
