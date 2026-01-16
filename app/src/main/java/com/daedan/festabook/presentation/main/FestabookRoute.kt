package com.daedan.festabook.presentation.main

import kotlinx.serialization.Serializable

@Serializable
sealed interface FestabookRoute {
    @Serializable
    data object Splash : FestabookRoute

    // TODO: PlaceUiModel, PlaceDetailUiModel 생성자에 추가 후 UiModel에 @Serializable 어노테이션 필요
    @Serializable
    data object PlaceDetail : FestabookRoute

    @Serializable
    data object Explore : FestabookRoute
}

@Serializable
sealed interface FestabookMainRoute : FestabookRoute {
    @Serializable
    data object Home : FestabookMainRoute

    @Serializable
    data object Schedule : FestabookMainRoute

    @Serializable
    data object PlaceMap : FestabookMainRoute

    @Serializable
    data object News : FestabookMainRoute

    @Serializable
    data object Setting : FestabookMainRoute
}
