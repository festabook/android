package com.daedan.festabook.presentation.placeMap.placeDetailPreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.R
import com.daedan.festabook.databinding.FragmentPlaceDetailPreviewSecondaryBinding
import com.daedan.festabook.di.appGraph
import com.daedan.festabook.di.fragment.FragmentKey
import com.daedan.festabook.presentation.common.BaseFragment
import com.daedan.festabook.presentation.common.OnMenuItemReClickListener
import com.daedan.festabook.presentation.common.showErrorSnackBar
import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.logging.PlacePreviewClick
import com.daedan.festabook.presentation.placeMap.model.PlaceUiState
import com.daedan.festabook.presentation.placeMap.placeDetailPreview.component.PlaceDetailPreviewSecondaryScreen
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.festabookSpacing
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesIntoMap(scope = AppScope::class, binding = binding<Fragment>())
@FragmentKey(PlaceDetailPreviewSecondaryFragment::class)
@Inject
class PlaceDetailPreviewSecondaryFragment(
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory,
) : BaseFragment<FragmentPlaceDetailPreviewSecondaryBinding>(),
    OnMenuItemReClickListener {
    override val layoutId: Int = R.layout.fragment_place_detail_preview_secondary

    private val viewModel: PlaceMapViewModel by viewModels({ requireParentFragment() })
    private val backPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.unselectPlace()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val placeDetailUiState by viewModel.selectedPlace.collectAsStateWithLifecycle()
                val visible = placeDetailUiState is PlaceUiState.Success

                LaunchedEffect(placeDetailUiState) {
                    backPressedCallback.isEnabled = true
                }

                FestabookTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        PlaceDetailPreviewSecondaryScreen(
                            visible = visible,
                            placeUiState = placeDetailUiState,
                            modifier =
                                Modifier
                                    .padding(
                                        vertical = festabookSpacing.paddingBody4,
                                        horizontal = festabookSpacing.paddingScreenGutter,
                                    ),
                            onError = {
                                showErrorSnackBar(it.throwable)
                            },
                            onEmpty = {
                                backPressedCallback.isEnabled = false
                            },
                            onClick = { selectedPlace ->
                                val selectedTimeTag = viewModel.selectedTimeTag.value
                                if (selectedPlace !is PlaceUiState.Success ||
                                    selectedTimeTag !is PlaceUiState.Success
                                ) {
                                    return@PlaceDetailPreviewSecondaryScreen
                                }
                                appGraph.defaultFirebaseLogger.log(
                                    PlacePreviewClick(
                                        baseLogData = appGraph.defaultFirebaseLogger.getBaseLogData(),
                                        placeName = selectedPlace.value.place.title ?: "undefined",
                                        timeTag =
                                            selectedTimeTag.value.name,
                                        category = selectedPlace.value.place.category.name,
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    override fun onMenuItemReClick() {
        viewModel.unselectPlace()
    }
}
