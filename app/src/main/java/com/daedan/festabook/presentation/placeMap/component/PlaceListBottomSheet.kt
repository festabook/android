package com.daedan.festabook.presentation.placeMap.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.daedan.festabook.presentation.theme.FestabookColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PlaceListBottomSheet(
    peekHeight: Dp,
    halfExpandedRatio: Float,
    modifier: Modifier = Modifier,
    bottomSheetState: PlaceListBottomSheetState =
        rememberPlaceListBottomSheetState(
            PlaceListBottomSheetValue.HALF_EXPANDED,
        ),
    shape: Shape = PlaceListBottomSheetDefault.bottomSheetBackgroundShape,
    color: Color = PlaceListBottomSheetDefault.bottomSheetBackgroundColor,
    onStateUpdate: (PlaceListBottomSheetValue) -> Unit = {},
    onScroll: (Float) -> Unit = {},
    dragHandle: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    require(halfExpandedRatio in 0.0..1.0) { "halfExpandedRatio는 0과 1 사이여야 합니다." }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val currentOnStateUpdate by rememberUpdatedState(onStateUpdate)

    LaunchedEffect(bottomSheetState.settledValue) {
        currentOnStateUpdate(bottomSheetState.settledValue)
    }

    val nestedScrollConnection = placeListBottomSheetNestedScrollConnection(bottomSheetState)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)

                    // 실제 레이아웃 측정 시에만 앵커 설정
                    if (!isLookingAhead) {
                        val screenHeightPx = constraints.maxHeight.toFloat()
                        // 3가지 앵커 높이 정의 (DP)
                        val halfExpandedOffsetPx =
                            screenHeightPx - screenHeightPx * halfExpandedRatio
                        val collapsedOffsetPx = with(density) { screenHeightPx - peekHeight.toPx() }
                        val expandedOffsetPx = 0f // 화면 최상단

                        bottomSheetState.state.updateAnchors(
                            newAnchors =
                                DraggableAnchors {
                                    PlaceListBottomSheetValue.EXPANDED at expandedOffsetPx
                                    PlaceListBottomSheetValue.HALF_EXPANDED at halfExpandedOffsetPx
                                    PlaceListBottomSheetValue.COLLAPSED at collapsedOffsetPx
                                },
                            newTarget = bottomSheetState.currentValue,
                        )
                        // 스크롤 되었을 때 호출하는 콜백
                        scope.launch {
                            snapshotFlow { bottomSheetState.state.requireOffset() }
                                .collect { currentOffset ->
                                    onScroll(currentOffset)
                                }
                        }
                    }

                    layout(placeable.width, placeable.height) {
                        placeable.place(0, 0)
                    }
                }.nestedScroll(nestedScrollConnection)
                .offset {
                    IntOffset(
                        0,
                        if (bottomSheetState.offset.isNaN()) 0 else bottomSheetState.offset.roundToInt(),
                    )
                }.background(
                    color = color,
                    shape = shape,
                ).anchoredDraggable(
                    state = bottomSheetState.state,
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

    val bottomSheetBackgroundColor: Color
        @Composable
        get() = FestabookColor.white

    private val dragHandleVerticalPadding = 12.dp
    private val dragHandleWidth = 32.dp
    private val dragHandleHeight = 4.dp

    private val dragHandleCorner =
        RoundedCornerShape(
            percent = 50,
        )

    private val dragHandleColor
        @Composable
        get() = FestabookColor.gray400

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
private fun placeListBottomSheetNestedScrollConnection(placeListBottomSheetState: PlaceListBottomSheetState): NestedScrollConnection {
    return object : NestedScrollConnection {
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource,
        ): Offset =
            if (available.y < 0 && source == NestedScrollSource.UserInput) {
                placeListBottomSheetState.state.dispatchRawDelta(available.y).toOffset()
            } else {
                Offset.Zero
            }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset =
            if (source == NestedScrollSource.UserInput) {
                placeListBottomSheetState.state.dispatchRawDelta(available.y).toOffset()
            } else {
                Offset.Zero
            }

        override suspend fun onPostFling(
            consumed: Velocity,
            available: Velocity,
        ): Velocity {
            placeListBottomSheetState.settleImmediately(available)
            return available
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val toFling = available.y
            val currentOffset = placeListBottomSheetState.state.requireOffset()
            val minAnchor = placeListBottomSheetState.anchors.minPosition()
            return if (toFling < 0 && currentOffset > minAnchor) {
                placeListBottomSheetState.settleImmediately(available)
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
    }
}
