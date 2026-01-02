package com.daedan.festabook.placeMap

import com.daedan.festabook.domain.model.Coordinate
import com.daedan.festabook.domain.model.OrganizationGeography
import com.daedan.festabook.domain.model.Place
import com.daedan.festabook.domain.model.PlaceCategory
import com.daedan.festabook.domain.model.PlaceGeography
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.presentation.placeMap.model.CoordinateUiModel
import com.daedan.festabook.presentation.placeMap.model.InitialMapSettingUiModel

val FAKE_PLACES =
    listOf(
        Place(
            id = 1,
            imageUrl = null,
            category = PlaceCategory.FOOD_TRUCK,
            title = "테스트 1",
            description = "설명 1",
            location = "위치 1",
            timeTags =
                listOf(
                    TimeTag(
                        timeTagId = 1,
                        name = "테스트1",
                    ),
                    TimeTag(
                        timeTagId = 2,
                        name = "테스트2",
                    ),
                ),
        ),
        Place(
            id = 2,
            imageUrl = null,
            category = PlaceCategory.FOOD_TRUCK,
            title = "테스트 2",
            description = "설명 2",
            location = "위치 2",
            timeTags =
                listOf(
                    TimeTag(
                        timeTagId = 2,
                        name = "테스트2",
                    ),
                ),
        ),
    )

val FAKE_PLACES_CATEGORY_FIXTURE =
    listOf(
        Place(
            id = 1,
            imageUrl = null,
            category = PlaceCategory.FOOD_TRUCK,
            title = "테스트 1",
            description = "설명 1",
            location = "위치 1",
            timeTags =
                listOf(
                    TimeTag(
                        timeTagId = 1,
                        name = "테스트1",
                    ),
                    TimeTag(
                        timeTagId = 2,
                        name = "테스트2",
                    ),
                ),
        ),
        Place(
            id = 2,
            imageUrl = null,
            category = PlaceCategory.BAR,
            title = "테스트 2",
            description = "설명 2",
            location = "위치 2",
            timeTags =
                listOf(
                    TimeTag(
                        timeTagId = 2,
                        name = "테스트2",
                    ),
                ),
        ),
    )

val FAKE_PLACE_GEOGRAPHIES =
    listOf(
        PlaceGeography(
            id = 1,
            category = PlaceCategory.FOOD_TRUCK,
            Coordinate(
                latitude = 1.0,
                longitude = 1.0,
            ),
            "푸드트럭",
            timeTags =
                listOf(
                    TimeTag(
                        timeTagId = 1,
                        name = "테스트1",
                    ),
                ),
        ),
        PlaceGeography(
            id = 1,
            category = PlaceCategory.BOOTH,
            Coordinate(
                latitude = 1.0,
                longitude = 1.0,
            ),
            "부스",
            timeTags =
                listOf(
                    TimeTag(
                        timeTagId = 1,
                        name = "테스트1",
                    ),
                ),
        ),
        PlaceGeography(
            id = 1,
            category = PlaceCategory.BAR,
            Coordinate(
                latitude = 1.0,
                longitude = 1.0,
            ),
            "주점",
            timeTags =
                listOf(
                    TimeTag(
                        timeTagId = 1,
                        name = "테스트1",
                    ),
                ),
        ),
    )

val FAKE_ORGANIZATION_GEOGRAPHY =
    OrganizationGeography(
        zoom = 15,
        initialCenter =
            Coordinate(
                latitude = 1.0,
                longitude = 1.0,
            ),
        polygonHoleBoundary =
            listOf(
                Coordinate(
                    latitude = 1.0,
                    longitude = 1.0,
                ),
            ),
    )

val FAKE_TIME_TAG =
    TimeTag(
        timeTagId = 1,
        name = "테스트1",
    )

val FAKE_INITIAL_MAP_SETTING =
    InitialMapSettingUiModel(
        zoom = 10,
        initialCenter =
            CoordinateUiModel(
                latitude = 10.0,
                longitude = 10.0,
            ),
        border =
            listOf(
                CoordinateUiModel(
                    latitude = 10.0,
                    longitude = 10.0,
                ),
            ),
    )
