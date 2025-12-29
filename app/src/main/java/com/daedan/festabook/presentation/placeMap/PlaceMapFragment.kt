package com.daedan.festabook.presentation.placeMap

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.daedan.festabook.presentation.common.OnMenuItemReClickListener
import com.daedan.festabook.presentation.common.showErrorSnackBar
import com.daedan.festabook.presentation.common.toPx
import com.daedan.festabook.presentation.placeDetail.PlaceDetailActivity
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.component.PlaceMapScreen
import com.daedan.festabook.presentation.placeMap.logging.CurrentLocationChecked
import com.daedan.festabook.presentation.placeMap.logging.PlaceBackToSchoolClick
import com.daedan.festabook.presentation.placeMap.logging.PlaceCategoryClick
import com.daedan.festabook.presentation.placeMap.logging.PlaceFragmentEnter
import com.daedan.festabook.presentation.placeMap.logging.PlaceItemClick
import com.daedan.festabook.presentation.placeMap.logging.PlaceMapButtonReClick
import com.daedan.festabook.presentation.placeMap.logging.PlaceMarkerClick
import com.daedan.festabook.presentation.placeMap.logging.PlacePreviewClick
import com.daedan.festabook.presentation.placeMap.logging.PlaceTimeTagSelected
import com.daedan.festabook.presentation.placeMap.mapManager.MapManager
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceListUiState
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiState
import com.daedan.festabook.presentation.placeMap.model.isSecondary
import com.daedan.festabook.presentation.placeMap.placeList.PlaceListViewModel
import com.daedan.festabook.presentation.placeMap.placeList.component.PlaceListBottomSheetValue
import com.daedan.festabook.presentation.placeMap.placeList.component.rememberPlaceListBottomSheetState
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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

    private val placeListViewModel: PlaceListViewModel by viewModels()

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
                val places by placeListViewModel.places.collectAsStateWithLifecycle()
                val selectedPlace by placeMapViewModel.selectedPlace.collectAsStateWithLifecycle()
                val timeTags by placeMapViewModel.timeTags.collectAsStateWithLifecycle()
                val selectedTimeTag by placeMapViewModel.selectedTimeTag.collectAsStateWithLifecycle()
                val initialCategories = PlaceCategoryUiModel.entries
                val isExceedMaxLength by placeMapViewModel.isExceededMaxLength.collectAsStateWithLifecycle()
                val timeTagChanged =
                    placeMapViewModel.selectedTimeTag.collectAsStateWithLifecycle(viewLifecycleOwner)
                var selectedCategoriesState by remember(timeTagChanged.value) {
                    mutableStateOf(
                        emptySet<PlaceCategoryUiModel>(),
                    )
                }

                val isPlacePreviewVisible by remember {
                    derivedStateOf {
                        selectedPlace is PlaceUiState.Success &&
                            !(selectedPlace as PlaceUiState.Success<PlaceDetailUiModel>).isSecondary
                    }
                }

                val isPlaceSecondaryPreviewVisible by remember {
                    derivedStateOf {
                        selectedPlace is PlaceUiState.Success &&
                            (selectedPlace as PlaceUiState.Success<PlaceDetailUiModel>).isSecondary
                    }
                }

                val scope = rememberCoroutineScope()
                val bottomSheetState = rememberPlaceListBottomSheetState()

                var mapManager by remember { mutableStateOf<MapManager?>(null) }

                LaunchedEffect(Unit) {
                    placeMapViewModel.placeGeographies.collect { placeGeographies ->
                        when (placeGeographies) {
                            is PlaceUiState.Loading -> Unit
                            is PlaceUiState.Success -> {
                                mapManager?.setupMarker(placeGeographies.value)
                                placeMapViewModel.selectedTimeTag.collect { selectedTimeTag ->
                                    when (selectedTimeTag) {
                                        is PlaceUiState.Success -> {
                                            mapManager?.filterMarkersByTimeTag(
                                                selectedTimeTag.value.timeTagId,
                                            )
                                        }

                                        is PlaceUiState.Empty -> {
                                            mapManager?.filterMarkersByTimeTag(TimeTag.EMTPY_TIME_TAG_ID)
                                        }

                                        else -> Unit
                                    }
                                }
                            }

                            is PlaceUiState.Error -> {
                                Timber.w(
                                    placeGeographies.throwable,
                                    "PlaceListFragment: ${placeGeographies.throwable.message}",
                                )
                                showErrorSnackBar(placeGeographies.throwable)
                            }

                            else -> Unit
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    placeMapViewModel.backToInitialPositionClicked.collect {
                        mapManager?.moveToPosition()
                    }
                }

                LaunchedEffect(Unit) {
                    placeMapViewModel.selectedCategories.collect { selectedCategories ->
                        if (selectedCategories.isEmpty()) {
                            mapManager?.clearFilter()
                        } else {
                            mapManager?.filterMarkersByCategories(selectedCategories)
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    placeMapViewModel.navigateToDetail.collect { selectedPlace ->
                        startPlaceDetailActivity(selectedPlace)
                    }
                }

                LaunchedEffect(Unit) {
                    placeListViewModel.places.first {
                        it is PlaceListUiState.Success || it is PlaceListUiState.Error
                    }
                    placeMapViewModel.selectedCategories.collect { selectedCategories ->
                        if (selectedCategories.isEmpty()) {
                            placeListViewModel.clearPlacesFilter()
                        } else {
                            placeListViewModel.updatePlacesByCategories(selectedCategories)
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    placeMapViewModel.onMenuItemReClick.collect {
                        mapManager?.moveToPosition()
                        if (!isPlacePreviewVisible && !isPlaceSecondaryPreviewVisible) return@collect
                        placeMapViewModel.onMapViewClick()
                        appGraph.defaultFirebaseLogger.log(
                            PlaceMapButtonReClick(
                                baseLogData = appGraph.defaultFirebaseLogger.getBaseLogData(),
                            ),
                        )
                    }
                }

                LaunchedEffect(Unit) {
                    placeMapViewModel.onMapViewClick
                        .filter { !isPlacePreviewVisible && !isPlaceSecondaryPreviewVisible }
                        .collect {
                            bottomSheetState.update(PlaceListBottomSheetValue.COLLAPSED)
                        }
                }

                LaunchedEffect(selectedPlace) {
                    // 스마트 캐스팅을 위해 로컬 변수에 할당
                    when (val place = selectedPlace) {
                        is PlaceUiState.Success -> {
                            mapManager?.selectMarker(place.value.place.id)

                            val currentTimeTag = placeMapViewModel.selectedTimeTag.value
                            val timeTagName =
                                if (currentTimeTag is PlaceUiState.Success) {
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

                        is PlaceUiState.Empty -> {
                            mapManager?.unselectMarker()
                        }

                        else -> Unit
                    }
                }

                FestabookTheme {
                    PlaceMapScreen(
                        places = places,
                        selectedPlaceUiState = selectedPlace,
                        timeTagsState = timeTags,
                        selectedTimeTagState = selectedTimeTag,
                        onMapReady = { map ->
                            scope.launch {
                                map.addOnLocationChangeListener {
                                    binding.logger.log(
                                        CurrentLocationChecked(
                                            baseLogData = binding.logger.getBaseLogData(),
                                        ),
                                    )
                                }
                                map.locationSource = locationSource
                                placeMapViewModel.initialMapSetting.collect { initialMapSetting ->
                                    if (initialMapSetting !is PlaceUiState.Success) return@collect
                                    if (mapManager == null) {
                                        val graph =
                                            createGraphFactory<MapManagerGraph.Factory>().create(
                                                map,
                                                initialMapSetting.value,
                                                placeMapViewModel,
                                                getInitialPadding(requireContext()),
                                            )
                                        mapManager = graph.mapManager
                                        mapManager?.setupBackToInitialPosition { isExceededMaxLength ->
                                            placeMapViewModel.setIsExceededMaxLength(
                                                isExceededMaxLength,
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        onTimeTagClick = { timeTag ->
                            placeMapViewModel.onDaySelected(timeTag)
                            binding.logger.log(
                                PlaceTimeTagSelected(
                                    baseLogData = binding.logger.getBaseLogData(),
                                    timeTagName = timeTag.name,
                                ),
                            )
                        },
                        onPlaceClick = { place ->
                            Timber.d("onPlaceClicked: $place")
                            startPlaceDetailActivity(place)
                            val selectedTimeTag = placeMapViewModel.selectedTimeTag.value
                            val timeTagName =
                                if (selectedTimeTag is PlaceUiState.Success) selectedTimeTag.value.name else "undefined"
                            appGraph.defaultFirebaseLogger.log(
                                PlaceItemClick(
                                    baseLogData = appGraph.defaultFirebaseLogger.getBaseLogData(),
                                    placeId = place.id,
                                    timeTagName = timeTagName,
                                    category = place.category.name,
                                ),
                            )
                        },
                        onPlaceLoad = {
                            placeMapViewModel.selectedTimeTag.collect { selectedTimeTag ->
                                when (selectedTimeTag) {
                                    is PlaceUiState.Success -> {
                                        placeListViewModel.updatePlacesByTimeTag(selectedTimeTag.value.timeTagId)
                                    }

                                    is PlaceUiState.Empty -> {
                                        placeListViewModel.updatePlacesByTimeTag(TimeTag.EMTPY_TIME_TAG_ID)
                                    }

                                    else -> Unit
                                }
                            }
                        },
                        isExceedMaxLength = isExceedMaxLength,
                        onPlaceLoadFinish = { places ->
                            preloadImages(
                                requireContext(),
                                places,
                            )
                        },
                        onPlaceListError = {
                            showErrorSnackBar(it.throwable)
                        },
                        onBackToInitialPositionClick = {
                            placeMapViewModel.onBackToInitialPositionClicked()
                            appGraph.defaultFirebaseLogger.log(
                                PlaceBackToSchoolClick(
                                    baseLogData = appGraph.defaultFirebaseLogger.getBaseLogData(),
                                ),
                            )
                        },
                        onCategoryClick = { selectedCategories ->
                            selectedCategoriesState = selectedCategories
                            placeMapViewModel.unselectPlace()
                            placeMapViewModel.setSelectedCategories(selectedCategories.toList())
                            appGraph.defaultFirebaseLogger.log(
                                PlaceCategoryClick(
                                    baseLogData = appGraph.defaultFirebaseLogger.getBaseLogData(),
                                    currentCategories = selectedCategories.joinToString(",") { it.toString() },
                                ),
                            )
                        },
                        onDisplayAllClick = { selectedCategories ->
                            selectedCategoriesState = selectedCategories
                            placeMapViewModel.unselectPlace()
                            placeMapViewModel.setSelectedCategories(initialCategories)
                        },
                        isPlacePreviewVisible = isPlacePreviewVisible,
                        isPlaceSecondaryPreviewVisible = isPlaceSecondaryPreviewVisible,
                        onMapDrag = {
                            placeMapViewModel.onMapViewClick()
                        },
                        selectedCategoriesState = selectedCategoriesState,
                        initialCategories = initialCategories,
                        bottomSheetState = bottomSheetState,
                        onBackPress = {
                            placeMapViewModel.unselectPlace()
                        },
                        onPlacePreviewClick = { selectedPlace ->
                            val selectedTimeTag = placeMapViewModel.selectedTimeTag.value
                            if (selectedPlace is PlaceUiState.Success &&
                                selectedTimeTag is PlaceUiState.Success
                            ) {
                                startPlaceDetailActivity(selectedPlace.value)
                                binding.logger.log(
                                    PlacePreviewClick(
                                        baseLogData = binding.logger.getBaseLogData(),
                                        placeName =
                                            selectedPlace.value.place.title
                                                ?: "undefined",
                                        timeTag = selectedTimeTag.value.name,
                                        category = selectedPlace.value.place.category.name,
                                    ),
                                )
                            }
                        },
                        onPlacePreviewError = {
                            showErrorSnackBar(it.throwable)
                        },
                    )
                }
            }
        }
    }

    private fun startPlaceDetailActivity(place: PlaceUiModel) {
        placeMapViewModel.selectPlace(place.id)
    }

    override fun onMenuItemReClick() {
        placeMapViewModel.unselectPlace()
        placeMapViewModel.onMenuItemReClick()
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
