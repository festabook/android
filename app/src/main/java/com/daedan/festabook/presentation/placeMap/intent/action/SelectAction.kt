package com.daedan.festabook.presentation.placeMap.intent.action

import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState

sealed interface SelectAction : PlaceMapAction {
    data class OnPlaceClick(
        val placeId: Long,
    ) : SelectAction

    data class OnPlacePreviewClick(
        val place: LoadState<PlaceDetailUiModel>,
    ) : SelectAction

    data object UnSelectPlace : SelectAction

    data class ExceededMaxLength(
        val isExceededMaxLength: Boolean,
    ) : SelectAction

    data class OnTimeTagClick(
        val timeTag: TimeTag,
    ) : SelectAction

    data object OnBackPress : SelectAction
}
