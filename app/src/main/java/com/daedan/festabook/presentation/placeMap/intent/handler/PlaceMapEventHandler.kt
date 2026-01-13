package com.daedan.festabook.presentation.placeMap.intent.handler

import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.component.PlaceListBottomSheetState
import com.daedan.festabook.presentation.placeMap.component.PlaceListBottomSheetValue
import com.daedan.festabook.presentation.placeMap.intent.action.SelectAction
import com.daedan.festabook.presentation.placeMap.intent.event.PlaceMapSideEffect
import com.daedan.festabook.presentation.placeMap.intent.state.MapManagerDelegate
import com.daedan.festabook.presentation.placeMap.logging.PlaceMapButtonReClick
import com.daedan.festabook.presentation.placeMap.mapManager.MapManager

class PlaceMapEventHandler(
    private val mapManagerDelegate: MapManagerDelegate,
    private val bottomSheetState: PlaceListBottomSheetState,
    private val viewModel: PlaceMapViewModel,
    private val logger: DefaultFirebaseLogger,
    // 안드로이드 종속적인 액션은 외부에서 주입
    // TODO Compose로 전환 시, 콜백이 아닌 Compose State 주입
    private val onPreloadImages: (PlaceMapSideEffect.PreloadImages) -> Unit,
    private val onStartPlaceDetail: (PlaceMapSideEffect.StartPlaceDetail) -> Unit,
    private val onShowErrorSnackBar: (PlaceMapSideEffect.ShowErrorSnackBar) -> Unit,
) : EventHandler<PlaceMapSideEffect> {
    private val mapManager: MapManager? get() = mapManagerDelegate.value

    override suspend operator fun invoke(event: PlaceMapSideEffect) {
        when (event) {
            is PlaceMapSideEffect.PreloadImages -> {
                onPreloadImages(event)
            }

            is PlaceMapSideEffect.MenuItemReClicked -> {
                mapManager?.moveToPosition()
                if (!event.isPreviewVisible) return
                viewModel.onPlaceMapAction(SelectAction.UnSelectPlace)
                logger.log(
                    PlaceMapButtonReClick(
                        baseLogData = logger.getBaseLogData(),
                    ),
                )
            }

            is PlaceMapSideEffect.StartPlaceDetail -> {
                onStartPlaceDetail(event)
            }

            is PlaceMapSideEffect.ShowErrorSnackBar -> {
                onShowErrorSnackBar(event)
            }

            is PlaceMapSideEffect.MapViewDrag -> {
                if (event.isPreviewVisible) return
                bottomSheetState.update(PlaceListBottomSheetValue.COLLAPSED)
            }
        }
    }
}
