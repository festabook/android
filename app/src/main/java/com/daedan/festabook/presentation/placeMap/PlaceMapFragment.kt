package com.daedan.festabook.presentation.placeMap

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.R
import com.daedan.festabook.databinding.FragmentPlaceMapBinding
import com.daedan.festabook.di.fragment.FragmentKey
import com.daedan.festabook.di.mapManager.MapManagerGraph
import com.daedan.festabook.logging.logger
import com.daedan.festabook.presentation.common.BaseFragment
import com.daedan.festabook.presentation.common.OnMenuItemReClickListener
import com.daedan.festabook.presentation.common.showErrorSnackBar
import com.daedan.festabook.presentation.common.toPx
import com.daedan.festabook.presentation.placeMap.component.NaverMapContent
import com.daedan.festabook.presentation.placeMap.logging.CurrentLocationChecked
import com.daedan.festabook.presentation.placeMap.logging.PlaceFragmentEnter
import com.daedan.festabook.presentation.placeMap.logging.PlaceMarkerClick
import com.daedan.festabook.presentation.placeMap.logging.PlaceTimeTagSelected
import com.daedan.festabook.presentation.placeMap.mapManager.MapManager
import com.daedan.festabook.presentation.placeMap.model.PlaceListUiState
import com.daedan.festabook.presentation.placeMap.model.SelectedPlaceUiState
import com.daedan.festabook.presentation.placeMap.placeCategory.PlaceCategoryFragment
import com.daedan.festabook.presentation.placeMap.placeDetailPreview.PlaceDetailPreviewFragment
import com.daedan.festabook.presentation.placeMap.placeDetailPreview.PlaceDetailPreviewSecondaryFragment
import com.daedan.festabook.presentation.placeMap.placeList.PlaceListFragment
import com.daedan.festabook.presentation.placeMap.timeTagSpinner.component.TimeTagMenu
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metro.createGraphFactory
import timber.log.Timber

@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<Fragment>(),
)
@FragmentKey(PlaceMapFragment::class)
@Inject
class PlaceMapFragment(
    placeListFragment: PlaceListFragment,
    placeDetailPreviewFragment: PlaceDetailPreviewFragment,
    placeCategoryFragment: PlaceCategoryFragment,
    placeDetailPreviewSecondaryFragment: PlaceDetailPreviewSecondaryFragment,
) : BaseFragment<FragmentPlaceMapBinding>(),
    OnMenuItemReClickListener {
    override val layoutId: Int = R.layout.fragment_place_map

    @Inject
    override lateinit var defaultViewModelProviderFactory: ViewModelProvider.Factory

    private lateinit var naverMap: NaverMap

    private val placeListFragment by lazy { getIfExists(placeListFragment) }
    private val placeDetailPreviewFragment by lazy { getIfExists(placeDetailPreviewFragment) }
    private val placeCategoryFragment by lazy { getIfExists(placeCategoryFragment) }
    private val placeDetailPreviewSecondaryFragment by lazy {
        getIfExists(
            placeDetailPreviewSecondaryFragment,
        )
    }
    private val locationSource by lazy {
        FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }
    private var mapManager: MapManager? = null

    private val viewModel: PlaceMapViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            childFragmentManager.commit {
                addWithSimpleTag(R.id.fcv_place_list_container, placeListFragment)
                addWithSimpleTag(R.id.fcv_map_container, placeDetailPreviewFragment)
                addWithSimpleTag(R.id.fcv_place_category_container, placeCategoryFragment)
                addWithSimpleTag(R.id.fcv_map_container, placeDetailPreviewSecondaryFragment)
                hide(placeDetailPreviewFragment)
                hide(placeDetailPreviewSecondaryFragment)
            }
        }

        setupComposeView()

        binding.logger.log(
            PlaceFragmentEnter(
                baseLogData = binding.logger.getBaseLogData(),
            ),
        )
    }

    override fun onMenuItemReClick() {
        val childFragments =
            listOf(
                placeListFragment,
                placeDetailPreviewFragment,
                placeCategoryFragment,
            )
        childFragments.forEach { fragment ->
            (fragment as? OnMenuItemReClickListener)?.onMenuItemReClick()
        }
        mapManager?.moveToPosition()
    }

    private fun setupComposeView() {
        binding.cvPlaceMap.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FestabookTheme {
                    NaverMapContent(
                        modifier = Modifier.fillMaxSize(),
                        onMapDrag = { viewModel.onMapViewClick() },
                        onMapReady = { setupMap(it) },
                    ) {
                        // TODO 흩어져있는 ComposeView 통합, 추후 PlaceMapScreen 사용
                    }
                }
            }
        }
        binding.cvTimeTagSpinner.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val timeTags by viewModel.timeTags.collectAsStateWithLifecycle()
                val title by viewModel.selectedTimeTagFlow.collectAsStateWithLifecycle()
                FestabookTheme {
                    if (timeTags.isNotEmpty()) {
                        TimeTagMenu(
                            title = title.name,
                            timeTags = timeTags,
                            onTimeTagClick = { timeTag ->
                                viewModel.onDaySelected(timeTag)
                                binding.logger.log(
                                    PlaceTimeTagSelected(
                                        baseLogData = binding.logger.getBaseLogData(),
                                        timeTagName = timeTag.name,
                                    ),
                                )
                            },
                            modifier =
                                Modifier
                                    .background(
                                        FestabookColor.white,
                                    ).padding(horizontal = 24.dp),
                        )
                    }
                }
            }
        }
    }

    private fun setupMap(map: NaverMap) {
        naverMap = map
        naverMap.addOnLocationChangeListener {
            binding.logger.log(
                CurrentLocationChecked(
                    baseLogData = binding.logger.getBaseLogData(),
                ),
            )
        }
        (placeListFragment as? OnMapReadyCallback)?.onMapReady(naverMap)
        naverMap.locationSource = locationSource
        setUpObserver()
    }

    private fun setUpObserver() {
        viewModel.placeGeographies.observe(viewLifecycleOwner) { placeGeographies ->
            when (placeGeographies) {
                is PlaceListUiState.Loading -> Unit
                is PlaceListUiState.Success -> {
                    mapManager?.setupMarker(placeGeographies.value)
                    viewModel.selectedTimeTag.observe(viewLifecycleOwner) { selectedTimeTag ->
                        mapManager?.filterMarkersByTimeTag(selectedTimeTag.timeTagId)
                    }
                }

                is PlaceListUiState.Error -> {
                    Timber.w(
                        placeGeographies.throwable,
                        "PlaceListFragment: ${placeGeographies.throwable.message}",
                    )
                    showErrorSnackBar(placeGeographies.throwable)
                }

                else -> Unit
            }
        }

        viewModel.initialMapSetting.observe(viewLifecycleOwner) { initialMapSetting ->
            if (initialMapSetting !is PlaceListUiState.Success) return@observe
            if (mapManager == null) {
                val graph =
                    createGraphFactory<MapManagerGraph.Factory>().create(
                        naverMap,
                        initialMapSetting.value,
                        viewModel,
                        getInitialPadding(requireContext()),
                    )
                mapManager = graph.mapManager
                mapManager?.setupBackToInitialPosition { isExceededMaxLength ->
                    viewModel.setIsExceededMaxLength(isExceededMaxLength)
                }
            }
        }

        viewModel.backToInitialPositionClicked.observe(viewLifecycleOwner) {
            mapManager?.moveToPosition()
        }

        viewModel.selectedCategories.observe(viewLifecycleOwner) { selectedCategories ->
            mapManager?.filterMarkersByCategories(selectedCategories)
        }

        viewModel.selectedPlace.observe(viewLifecycleOwner) { selectedPlace ->
            childFragmentManager.commit {
                setReorderingAllowed(true)

                when (selectedPlace) {
                    is SelectedPlaceUiState.Success -> {
                        mapManager?.selectMarker(selectedPlace.value.place.id)
                        if (selectedPlace.isSecondary) {
                            hide(placeListFragment)
                            hide(placeDetailPreviewFragment)
                            show(placeDetailPreviewSecondaryFragment)
                        } else {
                            hide(placeListFragment)
                            hide(placeDetailPreviewSecondaryFragment)
                            show(placeDetailPreviewFragment)
                        }
                        binding.logger.log(
                            PlaceMarkerClick(
                                baseLogData = binding.logger.getBaseLogData(),
                                placeId = selectedPlace.value.place.id,
                                timeTagName = viewModel.selectedTimeTag.value?.name ?: "undefined",
                                category = selectedPlace.value.place.category.name,
                            ),
                        )
                    }

                    is SelectedPlaceUiState.Empty -> {
                        mapManager?.unselectMarker()
                        hide(placeDetailPreviewFragment)
                        hide(placeDetailPreviewSecondaryFragment)
                        show(placeListFragment)
                    }

                    else -> Unit
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Fragment> getIfExists(fragment: T): T =
        childFragmentManager.findFragmentByTag(fragment::class.simpleName) as? T ?: fragment

    private fun FragmentTransaction.addWithSimpleTag(
        containerViewId: Int,
        fragment: Fragment,
    ) {
        add(containerViewId, fragment, fragment::class.simpleName)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1234

        private fun getInitialPadding(context: Context): Int = 254.toPx(context)
    }
}
