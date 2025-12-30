package com.daedan.festabook.presentation.placeMap

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil3.ImageLoader
import coil3.asImage
import coil3.request.ImageRequest
import coil3.request.ImageResult
import com.daedan.festabook.R
import com.daedan.festabook.databinding.FragmentPlaceMapBinding
import com.daedan.festabook.di.appGraph
import com.daedan.festabook.di.fragment.FragmentKey
import com.daedan.festabook.logging.logger
import com.daedan.festabook.presentation.common.BaseFragment
import com.daedan.festabook.presentation.common.ObserveAsEvents
import com.daedan.festabook.presentation.common.OnMenuItemReClickListener
import com.daedan.festabook.presentation.common.showErrorSnackBar
import com.daedan.festabook.presentation.placeDetail.PlaceDetailActivity
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.component.PlaceMapScreen
import com.daedan.festabook.presentation.placeMap.component.rememberPlaceListBottomSheetState
import com.daedan.festabook.presentation.placeMap.logging.PlaceFragmentEnter
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.placeMap.viewmodel.MapControlEventHandler
import com.daedan.festabook.presentation.placeMap.viewmodel.MapDelegate
import com.daedan.festabook.presentation.placeMap.viewmodel.MapManagerDelegate
import com.daedan.festabook.presentation.placeMap.viewmodel.PlaceMapAction
import com.daedan.festabook.presentation.placeMap.viewmodel.PlaceMapEventHandler
import com.daedan.festabook.presentation.placeMap.viewmodel.PlaceMapViewModel
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.naver.maps.map.util.FusedLocationSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber

@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<Fragment>(),
)
@FragmentKey(PlaceMapFragment::class)
@Inject
class PlaceMapFragment(
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory,
) : BaseFragment<FragmentPlaceMapBinding>(),
    OnMenuItemReClickListener {
    override val layoutId: Int = R.layout.fragment_place_map

    private val locationSource by lazy {
        FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }
    private val placeMapViewModel: PlaceMapViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding.logger.log(
            PlaceFragmentEnter(
                baseLogData = binding.logger.getBaseLogData(),
            ),
        )
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val uiState by placeMapViewModel.uiState.collectAsStateWithLifecycle()
                val density = LocalDensity.current
                val bottomSheetState = rememberPlaceListBottomSheetState()
                val mapDelegate = remember { MapDelegate() }
                val mapManagerDelegate = remember { MapManagerDelegate() }
                val mapControlEventHandler =
                    remember {
                        MapControlEventHandler(
                            initialPadding = with(density) { 254.dp.toPx() }.toInt(),
                            logger = appGraph.defaultFirebaseLogger,
                            locationSource = locationSource,
                            viewModel = placeMapViewModel,
                            mapDelegate = mapDelegate,
                            mapManagerDelegate = mapManagerDelegate,
                        )
                    }
                val placeMapEventHandler =
                    remember {
                        PlaceMapEventHandler(
                            mapManagerDelegate = mapManagerDelegate,
                            bottomSheetState = bottomSheetState,
                            viewModel = placeMapViewModel,
                            logger = appGraph.defaultFirebaseLogger,
                            onStartPlaceDetail = { startPlaceDetailActivity(it.placeDetail.value) },
                            onPreloadImages = { preloadImages(requireContext(), it.places) },
                            onShowErrorSnackBar = { showErrorSnackBar(it.error.throwable) },
                        )
                    }

                ObserveAsEvents(flow = placeMapViewModel.mapControlUiEvent) { event ->
                    mapControlEventHandler(event)
                }

                ObserveAsEvents(flow = placeMapViewModel.placeMapUiEvent) { event ->
                    placeMapEventHandler(event)
                }

                FestabookTheme {
                    PlaceMapScreen(
                        uiState = uiState,
                        onAction = { placeMapViewModel.onPlaceMapAction(it) },
                        bottomSheetState = bottomSheetState,
                        mapDelegate = mapDelegate,
                    )
                }
            }
        }
    }

    override fun onMenuItemReClick() {
        placeMapViewModel.onPlaceMapAction(PlaceMapAction.UnSelectPlace)
        placeMapViewModel.onMenuItemReClicked()
    }

    private fun startPlaceDetailActivity(placeDetail: PlaceDetailUiModel) {
        Timber.d("start detail activity")
        val intent = PlaceDetailActivity.newIntent(requireContext(), placeDetail)
        startActivity(intent)
    }

    // OOM 주의 !! 추후 페이징 처리 및 chunk 단위로 나눠서 로드합니다
    private fun preloadImages(
        context: Context,
        places: List<PlaceUiModel?>,
        maxSize: Int = 20,
    ) {
        val imageLoader = ImageLoader(context)
        val deferredList = mutableListOf<Deferred<ImageResult?>>()
        val defaultImage =
            ContextCompat
                .getDrawable(
                    context,
                    R.drawable.img_fallback,
                )?.asImage()

        lifecycleScope.launch {
            places
                .take(maxSize)
                .filterNotNull()
                .forEach { place ->
                    val deferred =
                        async {
                            val request =
                                ImageRequest
                                    .Builder(context)
                                    .data(place.imageUrl)
                                    .error {
                                        defaultImage
                                    }.fallback {
                                        defaultImage
                                    }.build()

                            runCatching {
                                withTimeout(2000) {
                                    imageLoader.execute(request)
                                }
                            }.onFailure {
                                imageLoader.shutdown()
                            }.getOrNull()
                        }
                    deferredList.add(deferred)
                }
            deferredList.awaitAll()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1234
    }
}
