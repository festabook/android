package com.daedan.festabook.presentation.placeMap.intent.event

import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.intent.state.LoadState

sealed interface SelectEvent : PlaceMapEvent {
    data class OnPlaceClick(
        val placeId: Long,
    ) : SelectEvent

    data class OnPlacePreviewClick(
        val place: LoadState<PlaceDetailUiModel>,
    ) : SelectEvent

    data object UnSelectPlace : SelectEvent

    data class ExceededMaxLength(
        val isExceededMaxLength: Boolean,
    ) : SelectEvent

    data class OnTimeTagClick(
        val timeTag: TimeTag,
    ) : SelectEvent

    data object OnBackPress : SelectEvent
}
