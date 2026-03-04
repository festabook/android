package com.daedan.festabook.presentation.placeMap.navigation

import android.net.Uri
import android.os.Bundle
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.daedan.festabook.presentation.main.FestabookRoute
import com.daedan.festabook.presentation.main.MainTabRoute
import com.daedan.festabook.presentation.placeDetail.PlaceDetailViewModel
import com.daedan.festabook.presentation.placeDetail.component.PlaceDetailRoute
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

fun NavGraphBuilder.placeMapNavGraph(
    onBackToPreviousClick: () -> Unit,
    placeDetailViewModelFactory: PlaceDetailViewModel.Factory,
    onShowErrorSnackbar: (Throwable) -> Unit,
) {
    composable<MainTabRoute.PlaceMap> {
    }

    composable<FestabookRoute.PlaceDetail>(
        typeMap =
            mapOf(
                typeOf<PlaceDetailUiModel?>() to defaultNavType<PlaceDetailUiModel?>(),
                typeOf<PlaceUiModel?>() to defaultNavType<PlaceUiModel?>(),
            ),
        enterTransition = {
            slideInVertically(initialOffsetY = { it / 10 }) + fadeIn()
        },
        exitTransition = {
            slideOutVertically(targetOffsetY = { it / 10 }) + fadeOut()
        },
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<FestabookRoute.PlaceDetail>()
        val viewModel =
            viewModel<PlaceDetailViewModel>(
                factory =
                    PlaceDetailViewModel.factory(
                        placeDetailViewModelFactory,
                        route.placeUiModel,
                        route.placeDetailUiModel,
                    ),
            )
        PlaceDetailRoute(
            modifier =
                Modifier.graphicsLayer(
                    compositingStrategy = CompositingStrategy.Offscreen,
                    clip = true,
                ),
            viewModel = viewModel,
            onBackToPreviousClick = onBackToPreviousClick,
            onShowErrorSnackbar = onShowErrorSnackbar,
        )
    }
}

// TODO UIModel에서 Parcelable 제거 및 CMP에 맞게 안드로이드 의존성 제거

private inline fun <reified T> defaultNavType() =
    object : NavType<T>(isNullableAllowed = true) {
        override fun get(
            bundle: Bundle,
            key: String,
        ): T? = bundle.getString(key)?.let { Json.decodeFromString(it) }

        override fun parseValue(value: String): T =
            Json.decodeFromString(
                Uri.decode(value),
            )

        override fun put(
            bundle: Bundle,
            key: String,
            value: T,
        ) = bundle.putString(key, Json.encodeToString(value))

        override fun serializeAsValue(value: T): String = Uri.encode(Json.encodeToString(value))
    }
