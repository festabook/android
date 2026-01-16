package com.daedan.festabook.presentation.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.daedan.festabook.R
import com.daedan.festabook.presentation.theme.FestabookColor

enum class FestabookMainTab(
    @DrawableRes val iconResId: Int,
    @StringRes val labelResId: Int,
    val contentDescription: String,
    val route: FestabookMainRoute,
) {
    HOME(
        iconResId = R.drawable.ic_home,
        labelResId = R.string.menu_home_title,
        contentDescription = "홈",
        route = FestabookMainRoute.Home,
    ),
    SCHEDULE(
        iconResId = R.drawable.ic_schedule,
        labelResId = R.string.menu_schedule_title,
        contentDescription = "일정",
        route = FestabookMainRoute.Schedule,
    ),
    PLACE_MAP(
        iconResId = R.drawable.ic_map,
        labelResId = R.string.menu_map_title,
        contentDescription = "지도",
        route = FestabookMainRoute.PlaceMap,
    ),
    NEWS(
        iconResId = R.drawable.ic_news,
        labelResId = R.string.menu_news_title,
        contentDescription = "뉴스",
        route = FestabookMainRoute.News,
    ),
    SETTING(
        iconResId = R.drawable.ic_setting,
        labelResId = R.string.menu_setting_title,
        contentDescription = "설정",
        route = FestabookMainRoute.Setting,
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
