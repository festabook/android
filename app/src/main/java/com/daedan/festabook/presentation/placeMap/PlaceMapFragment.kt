package com.daedan.festabook.presentation.placeMap

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
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
import com.daedan.festabook.di.mapManager.MapManagerGraph
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.logging.logger
import com.daedan.festabook.presentation.common.BaseFragment
import com.daedan.festabook.presentation.common.ObserveAsEvents
import com.daedan.festabook.presentation.common.OnMenuItemReClickListener
import com.daedan.festabook.presentation.common.showErrorSnackBar
import com.daedan.festabook.presentation.common.toPx
import com.daedan.festabook.presentation.placeDetail.PlaceDetailActivity
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.component.MapState
import com.daedan.festabook.presentation.placeMap.component.PlaceListBottomSheetValue
import com.daedan.festabook.presentation.placeMap.component.PlaceMapScreen
import com.daedan.festabook.presentation.placeMap.component.rememberPlaceListBottomSheetState
import com.daedan.festabook.presentation.placeMap.logging.CurrentLocationChecked
import com.daedan.festabook.presentation.placeMap.logging.PlaceFragmentEnter
import com.daedan.festabook.presentation.placeMap.logging.PlaceMapButtonReClick
import com.daedan.festabook.presentation.placeMap.logging.PlaceMarkerClick
import com.daedan.festabook.presentation.placeMap.mapManager.MapManager
import com.daedan.festabook.presentation.placeMap.model.LoadState
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.placeMap.viewmodel.PlaceMapAction
import com.daedan.festabook.presentation.placeMap.viewmodel.PlaceMapEvent
import com.daedan.festabook.presentation.placeMap.viewmodel.PlaceMapViewModel
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.naver.maps.map.util.FusedLocationSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
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
                var mapManager by remember { mutableStateOf<MapManager?>(null) }
                val bottomSheetState = rememberPlaceListBottomSheetState()
                val mapState = remember { MapState() }

                ObserveAsEvents(flow = placeMapViewModel.uiEvent) { event ->
                    when (event) {
                        is PlaceMapEvent.InitMap -> {
                            val naverMap = mapState.await()
                            naverMap.addOnLocationChangeListener {
                                binding.logger.log(
                                    CurrentLocationChecked(
                                        baseLogData = binding.logger.getBaseLogData(),
                                    ),
                                )
                            }
                            naverMap.locationSource = locationSource
                        }

                        is PlaceMapEvent.InitMapManager -> {
                            val naverMap = mapState.await()
                            if (mapManager == null) {
                                val graph =
                                    createGraphFactory<MapManagerGraph.Factory>().create(
                                        naverMap,
                                        event.initialMapSetting,
                                        placeMapViewModel,
                                        getInitialPadding(requireContext()),
                                    )
                                mapManager = graph.mapManager
                                mapManager?.setupBackToInitialPosition { isExceededMaxLength ->
                                    placeMapViewModel.onPlaceMapAction(
                                        PlaceMapAction.ExceededMaxLength(isExceededMaxLength),
                                    )
                                }
                            }
                        }

                        is PlaceMapEvent.PreloadImages -> {
                            preloadImages(
                                requireContext(),
                                event.places,
                            )
                        }

                        is PlaceMapEvent.BackToInitialPosition -> {
                            mapManager?.moveToPosition()
                        }

                        is PlaceMapEvent.MenuItemReClicked -> {
                            mapManager?.moveToPosition()
                            if (!event.isPreviewVisible) return@ObserveAsEvents
                            placeMapViewModel.onPlaceMapAction(PlaceMapAction.UnSelectPlace)
                            appGraph.defaultFirebaseLogger.log(
                                PlaceMapButtonReClick(
                                    baseLogData = appGraph.defaultFirebaseLogger.getBaseLogData(),
                                ),
                            )
                        }

                        is PlaceMapEvent.StartPlaceDetail -> {
                            startPlaceDetailActivity(event.placeDetail.value)
                        }

                        is PlaceMapEvent.ShowErrorSnackBar -> {
                            showErrorSnackBar(event.error.throwable)
                        }

                        is PlaceMapEvent.SetMarkerByTimeTag -> {
                            if (event.isInitial) {
                                mapManager?.setupMarker(event.placeGeographies)
                            }

                            when (val selectedTimeTag = event.selectedTimeTag) {
                                is LoadState.Success -> {
                                    mapManager?.filterMarkersByTimeTag(
                                        selectedTimeTag.value.timeTagId,
                                    )
                                }

                                is LoadState.Empty -> {
                                    mapManager?.filterMarkersByTimeTag(TimeTag.EMTPY_TIME_TAG_ID)
                                }

                                else -> Unit
                            }
                        }

                        is PlaceMapEvent.FilterMapByCategory -> {
                            val selectedCategories = event.selectedCategories
                            if (selectedCategories.isEmpty()) {
                                mapManager?.clearFilter()
                            } else {
                                mapManager?.filterMarkersByCategories(selectedCategories)
                            }
                        }

                        is PlaceMapEvent.MapViewDrag -> {
                            if (event.isPreviewVisible) return@ObserveAsEvents
                            bottomSheetState.update(PlaceListBottomSheetValue.COLLAPSED)
                        }

                        is PlaceMapEvent.SelectMarker -> {
                            when (val place = event.placeDetail) {
                                is LoadState.Success -> {
                                    mapManager?.selectMarker(place.value.place.id)

                                    val currentTimeTag = uiState.selectedTimeTag
                                    val timeTagName =
                                        if (currentTimeTag is LoadState.Success) {
                                            currentTimeTag.value.name
                                        } else {
                                            "undefined"
                                        }
                                    binding.logger.log(
                                        PlaceMarkerClick(
                                            baseLogData = binding.logger.getBaseLogData(),
                                            placeId = place.value.place.id,
                                            timeTagName = timeTagName,
                                            category = place.value.place.category.name,
                                        ),
                                    )
                                }

                                else -> Unit
                            }
                        }

                        is PlaceMapEvent.UnselectMarker -> {
                            mapManager?.unselectMarker()
                        }
                    }
                }

                FestabookTheme {
                    PlaceMapScreen(
                        uiState = uiState,
                        onAction = { placeMapViewModel.onPlaceMapAction(it) },
                        bottomSheetState = bottomSheetState,
                        mapState = mapState,
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
                    requireContext(),
                    R.drawable.img_fallback,
                )?.asImage()

        lifecycleScope.launch(Dispatchers.IO) {
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

        private fun getInitialPadding(context: Context): Int = 254.toPx(context)
    }
}
