package com.daedan.festabook.presentation.placeMap.intent.event

import com.daedan.festabook.logging.DefaultFirebaseLogger
import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.component.PlaceListBottomSheetState
import com.daedan.festabook.presentation.placeMap.component.PlaceListBottomSheetValue
import com.daedan.festabook.presentation.placeMap.intent.action.SelectAction
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
    private val onPreloadImages: (PlaceMapEvent.PreloadImages) -> Unit,
    private val onStartPlaceDetail: (PlaceMapEvent.StartPlaceDetail) -> Unit,
    private val onShowErrorSnackBar: (PlaceMapEvent.ShowErrorSnackBar) -> Unit,
) {
    private val mapManager: MapManager? get() = mapManagerDelegate.value

    suspend operator fun invoke(event: PlaceMapEvent) {
        when (event) {
            is PlaceMapEvent.PreloadImages -> {
                onPreloadImages(event)
            }

            is PlaceMapEvent.MenuItemReClicked -> {
                mapManager?.moveToPosition()
                if (!event.isPreviewVisible) return
                viewModel.onPlaceMapAction(SelectAction.UnSelectPlace)
                logger.log(
                    PlaceMapButtonReClick(
                        baseLogData = logger.getBaseLogData(),
                    ),
                )
            }

            is PlaceMapEvent.StartPlaceDetail -> {
                onStartPlaceDetail(event)
            }

            is PlaceMapEvent.ShowErrorSnackBar -> {
                onShowErrorSnackBar(event)
            }

            is PlaceMapEvent.MapViewDrag -> {
                if (event.isPreviewVisible) return
                bottomSheetState.update(PlaceListBottomSheetValue.COLLAPSED)
            }
        }
    }
}
