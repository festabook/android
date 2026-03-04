package com.daedan.festabook.presentation.placeMap.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Velocity

class PlaceListBottomSheetState(
    val state: AnchoredDraggableState<PlaceListBottomSheetValue>,
) {
    val anchors get() = state.anchors
    val settledValue get() = state.settledValue

    val currentValue get() = state.currentValue
    val offset get() = state.offset

    suspend fun update(newState: PlaceListBottomSheetValue) {
        state.animateTo(newState)
    }

    /**
     anchoredState의 기본 settle() 동작은 거리 기반으로 동작합니다.
     거리 기반 동작을, 상태 기반으로 동작하도록 변경하여, 미세한 드래그에도 바텀시트가 펼쳐지도록 합니다.
     */
    suspend fun settleImmediately(
        available: Velocity,
        animationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
    ) {
        val targetState =
            if (available.y < 0) {
                when (state.currentValue) {
                    PlaceListBottomSheetValue.EXPANDED -> state.currentValue
                    PlaceListBottomSheetValue.HALF_EXPANDED -> PlaceListBottomSheetValue.EXPANDED
                    PlaceListBottomSheetValue.COLLAPSED -> PlaceListBottomSheetValue.HALF_EXPANDED
                }
            } else if (available.y > 0) {
                when (state.currentValue) {
                    PlaceListBottomSheetValue.EXPANDED -> PlaceListBottomSheetValue.HALF_EXPANDED
                    PlaceListBottomSheetValue.HALF_EXPANDED -> PlaceListBottomSheetValue.COLLAPSED
                    PlaceListBottomSheetValue.COLLAPSED -> state.currentValue
                }
            } else {
                state.currentValue
            }

        state.animateTo(
            targetValue = targetState,
            animationSpec = animationSpec,
        )
    }
}

enum class PlaceListBottomSheetValue {
    EXPANDED,
    HALF_EXPANDED,
    COLLAPSED,
}

@Composable
fun rememberPlaceListBottomSheetState(
    initialState: PlaceListBottomSheetValue = PlaceListBottomSheetValue.HALF_EXPANDED,
): PlaceListBottomSheetState {
    val anchoredState =
        remember {
            AnchoredDraggableState(initialValue = initialState)
        }

    return remember(anchoredState) {
        PlaceListBottomSheetState(anchoredState)
    }
}
