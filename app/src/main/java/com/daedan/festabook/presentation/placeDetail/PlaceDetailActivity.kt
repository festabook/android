package com.daedan.festabook.presentation.placeDetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.di.appGraph
import com.daedan.festabook.presentation.common.getObject
import com.daedan.festabook.presentation.placeDetail.component.PlaceDetailScreen
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.theme.FestabookColor
import dev.zacsweers.metro.Inject
import timber.log.Timber

class PlaceDetailActivity : ComponentActivity() {
    @Inject
    private lateinit var viewModelFactory: PlaceDetailViewModel.Factory

    private lateinit var viewModel: PlaceDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        appGraph.inject(this)
        super.onCreate(savedInstanceState)

        val placeUiObject = intent?.getObject<PlaceUiModel>(KEY_PLACE_UI_MODEL)
        val placeDetailObject = intent?.getObject<PlaceDetailUiModel>(KEY_PLACE_DETAIL_UI_MODEL)
        if (placeUiObject == null && placeDetailObject == null) {
            finish()
            return
        }

        viewModel =
            ViewModelProvider(
                this,
                PlaceDetailViewModel.factory(
                    viewModelFactory,
                    placeUiObject,
                    placeDetailObject,
                ),
            )[PlaceDetailViewModel::class.java]

        setContent {
            enableEdgeToEdge(
                statusBarStyle =
                    SystemBarStyle.light(
                        scrim = FestabookColor.white.toArgb(),
                        darkScrim = FestabookColor.white.toArgb(),
                    ),
            )
            val placeDetailUiState by viewModel.placeDetail.collectAsStateWithLifecycle()
            PlaceDetailScreen(
                uiState = placeDetailUiState,
                onBackToPreviousClick = { finish() },
            )
        }

        Timber.d("detailActivity : ${viewModel.placeDetail.value}")
    }

    companion object {
        private const val KEY_PLACE_UI_MODEL = "placeUiModel"
        private const val KEY_PLACE_DETAIL_UI_MODEL = "placeDetailUiModel"

        fun newIntent(
            context: Context,
            placeDetail: PlaceDetailUiModel,
        ) = Intent(context, PlaceDetailActivity::class.java).apply {
            putExtra(KEY_PLACE_DETAIL_UI_MODEL, placeDetail)
        }
    }
}
