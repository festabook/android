package com.daedan.festabook.presentation.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.daedan.festabook.R
import com.daedan.festabook.presentation.theme.FestabookColor

enum class FestabookMainTab(
    @param:DrawableRes val iconResId: Int,
    @param:StringRes val labelResId: Int,
    val route: MainTabRoute,
) {
    HOME(
        iconResId = R.drawable.ic_home,
        labelResId = R.string.menu_home_title,
        route = MainTabRoute.Home,
    ),
    SCHEDULE(
        iconResId = R.drawable.ic_schedule,
        labelResId = R.string.menu_schedule_title,
        route = MainTabRoute.Schedule,
    ),
    PLACE_MAP(
        iconResId = R.drawable.ic_map,
        labelResId = R.string.menu_map_title,
        route = MainTabRoute.PlaceMap,
    ),
    NEWS(
        iconResId = R.drawable.ic_news,
        labelResId = R.string.menu_news_title,
        route = MainTabRoute.News,
    ),
    SETTING(
        iconResId = R.drawable.ic_setting,
        labelResId = R.string.menu_setting_title,
        route = MainTabRoute.Setting,
    ),
    ;

    companion object Defaults {
        @Composable
        fun find(predicate: @Composable (FestabookRoute) -> Boolean) =
            entries.find {
                predicate(it.route)
            }

        val selectedColor
            @Composable
            @ReadOnlyComposable
            get() = FestabookColor.black

        val unselectedColor
            @Composable
            @ReadOnlyComposable
            get() = FestabookColor.gray400
    }
}
