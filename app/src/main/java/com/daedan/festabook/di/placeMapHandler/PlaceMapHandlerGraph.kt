package com.daedan.festabook.di.placeMapHandler

import com.daedan.festabook.presentation.placeMap.intent.handler.EventHandlerContext
import com.daedan.festabook.presentation.placeMap.intent.handler.FilterEventHandler
import com.daedan.festabook.presentation.placeMap.intent.handler.MapControlEventHandler
import com.daedan.festabook.presentation.placeMap.intent.handler.SelectEventHandler
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(PlaceMapViewModelScope::class)
interface PlaceMapHandlerGraph {
    val filterEventHandler: FilterEventHandler
    val selectEventHandler: SelectEventHandler
    val mapControlEventHandler: MapControlEventHandler

    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    interface Factory {
        fun create(
            @Provides context: EventHandlerContext,
        ): PlaceMapHandlerGraph
    }
}
