package com.daedan.festabook.presentation.placeMap.placeDetailPreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.daedan.festabook.databinding.FragmentPlaceDetailPreviewBinding
import com.daedan.festabook.di.fragment.FragmentKey
import com.daedan.festabook.logging.logger
import com.daedan.festabook.presentation.common.BaseFragment
import com.daedan.festabook.presentation.common.OnMenuItemReClickListener
import com.daedan.festabook.presentation.common.showErrorSnackBar
import com.daedan.festabook.presentation.placeDetail.PlaceDetailActivity
import com.daedan.festabook.presentation.placeDetail.model.PlaceDetailUiModel
import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.logging.PlacePreviewClick
import com.daedan.festabook.presentation.placeMap.model.PlaceUiState
import com.daedan.festabook.presentation.placeMap.placeDetailPreview.component.PlaceDetailPreviewScreen
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.festabookSpacing
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesIntoMap(scope = AppScope::class, binding = binding<Fragment>())
@FragmentKey(PlaceDetailPreviewFragment::class)
@Inject
class PlaceDetailPreviewFragment(
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory,
) : BaseFragment<FragmentPlaceDetailPreviewBinding>(),
    OnMenuItemReClickListener {
    override val layoutId: Int = R.layout.fragment_place_detail_preview
    private val viewModel: PlaceMapViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            super.onCreateView(inflater, container, savedInstanceState)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FestabookTheme {
                    val placeDetailUiState by viewModel.selectedPlaceFlow.collectAsStateWithLifecycle()
                    val visible = placeDetailUiState is PlaceUiState.Success

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        PlaceDetailPreviewScreen(
                            placeUiState = placeDetailUiState,
                            visible = visible,
                            modifier =
                                Modifier
                                    .padding(
                                        vertical = festabookSpacing.paddingBody4,
                                        horizontal = festabookSpacing.paddingScreenGutter,
                                    ),
                            onClick = { selectedPlace ->
                                if (selectedPlace !is PlaceUiState.Success) return@PlaceDetailPreviewScreen
                                startPlaceDetailActivity(selectedPlace.value)
                                binding.logger.log(
                                    PlacePreviewClick(
                                        baseLogData = binding.logger.getBaseLogData(),
                                        placeName =
                                            selectedPlace.value.place.title
                                                ?: "undefined",
                                        timeTag =
                                            viewModel.selectedTimeTag.value?.name
                                                ?: "undefined",
                                        category = selectedPlace.value.place.category.name,
                                    ),
                                )
                            },
                            onError = { selectedPlace ->
                                showErrorSnackBar(selectedPlace.throwable)
                            },
                            onBackPress = {
                                viewModel.unselectPlace()
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

    private fun startPlaceDetailActivity(placeDetail: PlaceDetailUiModel) {
        startActivity(PlaceDetailActivity.newIntent(requireContext(), placeDetail))
    }
}
