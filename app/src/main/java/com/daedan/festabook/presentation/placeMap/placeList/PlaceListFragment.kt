package com.daedan.festabook.presentation.placeMap.placeList

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
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
import com.daedan.festabook.databinding.FragmentPlaceListBinding
import com.daedan.festabook.di.appGraph
import com.daedan.festabook.di.fragment.FragmentKey
import com.daedan.festabook.presentation.common.BaseFragment
import com.daedan.festabook.presentation.common.OnMenuItemReClickListener
import com.daedan.festabook.presentation.placeDetail.PlaceDetailActivity
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.logging.PlaceBackToSchoolClick
import com.daedan.festabook.presentation.placeMap.logging.PlaceItemClick
import com.daedan.festabook.presentation.placeMap.logging.PlaceMapButtonReClick
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.placeMap.placeList.component.PlaceListBottomSheetState
import com.daedan.festabook.presentation.placeMap.placeList.component.PlaceListScreen
import com.daedan.festabook.presentation.placeMap.placeList.component.rememberAnchoredState
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber

@ContributesIntoMap(scope = AppScope::class, binding = binding<Fragment>())
@FragmentKey(PlaceListFragment::class)
@Inject
class PlaceListFragment(
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory,
) : BaseFragment<FragmentPlaceListBinding>(),
    OnPlaceClickListener,
    OnMenuItemReClickListener,
    OnMapReadyCallback {
    override val layoutId: Int = R.layout.fragment_place_list
    private val viewModel: PlaceMapViewModel by viewModels({ requireParentFragment() })
    private val childViewModel: PlaceListViewModel by viewModels()

    // 기존 Fragment와의 상호 운용성을 위한 임시 Flow입니다.
    // Fragment -> PlaceMapScreen으로 통합 시, 제거할 예정입니다.
    private val mapFlow: MutableStateFlow<NaverMap?> = MutableStateFlow(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            super.onCreateView(inflater, container, savedInstanceState)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val places by childViewModel.placesFlow.collectAsStateWithLifecycle()
                val isExceedMaxLength by viewModel.isExceededMaxLengthFlow.collectAsStateWithLifecycle()
                val bottomSheetState =
                    rememberAnchoredState(PlaceListBottomSheetState.HALF_EXPANDED)
                val map by mapFlow.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.onMapViewClickFlow.collect {
                        bottomSheetState.animateTo(
                            PlaceListBottomSheetState.COLLAPSED,
                        )
                    }
                }

                FestabookTheme {
                    PlaceListScreen(
                        placesUiState = places,
                        map = map,
                        onPlaceClick = { onPlaceClicked(it) },
                        bottomSheetState = bottomSheetState,
                        isExceedMaxLength = isExceedMaxLength,
                        onPlaceLoadFinish = { places ->
                            preloadImages(
                                requireContext(),
                                places,
                            )
                        },
                        onPlaceLoad = {
                            LaunchedEffect(Unit) {
                                viewModel.selectedTimeTagFlow.collect {
                                    childViewModel.updatePlacesByTimeTag(it.timeTagId)
                                }
                            }
                        },
                        onBackToInitialPositionClicked = {
                            viewModel.onBackToInitialPositionClicked()
                            appGraph.defaultFirebaseLogger.log(
                                PlaceBackToSchoolClick(
                                    baseLogData = appGraph.defaultFirebaseLogger.getBaseLogData(),
                                ),
                            )
                        },
                    )
                }
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setUpObserver()
    }

    override fun onPlaceClicked(place: PlaceUiModel) {
        Timber.d("onPlaceClicked: $place")
        startPlaceDetailActivity(place)
        appGraph.defaultFirebaseLogger.log(
            PlaceItemClick(
                baseLogData = appGraph.defaultFirebaseLogger.getBaseLogData(),
                placeId = place.id,
                timeTagName = viewModel.selectedTimeTag.value?.name ?: "undefinded",
                category = place.category.name,
            ),
        )
    }

    override fun onMenuItemReClick() {
        if (binding.root.isGone || !isResumed || view == null) return
        lifecycleScope.launch {
            viewModel.onMapViewClick()
        }
        appGraph.defaultFirebaseLogger.log(
            PlaceMapButtonReClick(
                baseLogData = appGraph.defaultFirebaseLogger.getBaseLogData(),
            ),
        )
    }

    override fun onMapReady(naverMap: NaverMap) {
        lifecycleScope.launch {
            mapFlow.value = naverMap
        }
    }

    private fun setUpObserver() {
        viewModel.navigateToDetail.observe(viewLifecycleOwner) { selectedPlace ->
            startPlaceDetailActivity(selectedPlace)
        }

        viewModel.selectedCategories.observe(viewLifecycleOwner) { selectedCategories ->
            if (selectedCategories.isEmpty()) {
                childViewModel.clearPlacesFilter()
            } else {
                childViewModel.updatePlacesByCategories(selectedCategories)
            }
        }
    }

    private fun startPlaceDetailActivity(place: PlaceUiModel) {
        viewModel.selectPlace(place.id)
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
}
