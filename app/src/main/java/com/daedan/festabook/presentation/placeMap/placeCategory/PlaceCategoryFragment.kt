package com.daedan.festabook.presentation.placeMap.placeCategory

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.R
import com.daedan.festabook.databinding.FragmentPlaceCategoryBinding
import com.daedan.festabook.di.appGraph
import com.daedan.festabook.di.fragment.FragmentKey
import com.daedan.festabook.presentation.common.BaseFragment
import com.daedan.festabook.presentation.placeMap.PlaceMapViewModel
import com.daedan.festabook.presentation.placeMap.logging.PlaceCategoryClick
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.placeCategory.component.PlaceCategoryScreen
import com.daedan.festabook.presentation.theme.FestabookTheme
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesIntoMap(scope = AppScope::class, binding = binding<Fragment>())
@FragmentKey(PlaceCategoryFragment::class)
@Inject
class PlaceCategoryFragment : BaseFragment<FragmentPlaceCategoryBinding>() {
    override val layoutId: Int = R.layout.fragment_place_category

    @Inject
    override lateinit var defaultViewModelProviderFactory: ViewModelProvider.Factory
    private val viewModel: PlaceMapViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FestabookTheme {
                    val initialCategories = PlaceCategoryUiModel.entries
                    // StateFlow로 변경 시 asFlow 제거 예정
                    val timeTagChanged =
                        viewModel.selectedTimeTag
                            .asFlow()
                            .collectAsStateWithLifecycle(viewLifecycleOwner)
                    var selectedCategoriesState by remember(timeTagChanged.value) {
                        mutableStateOf(
                            emptySet<PlaceCategoryUiModel>(),
                        )
                    }

                    PlaceCategoryScreen(
                        initialCategories = initialCategories,
                        selectedCategories = selectedCategoriesState,
                        onCategoryClick = { selectedCategories ->
                            selectedCategoriesState = selectedCategories
                            viewModel.unselectPlace()
                            viewModel.setSelectedCategories(selectedCategories.toList())
                            appGraph.defaultFirebaseLogger.log(
                                PlaceCategoryClick(
                                    baseLogData = appGraph.defaultFirebaseLogger.getBaseLogData(),
                                    currentCategories = selectedCategories.joinToString(",") { it.toString() },
                                ),
                            )
                        },
                        onDisplayAllClick = { selectedCategories ->
                            selectedCategoriesState = selectedCategories
                            viewModel.unselectPlace()
                            viewModel.setSelectedCategories(initialCategories)
                        },
                    )
                }
            }
        }
}
