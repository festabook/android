package com.daedan.festabook.presentation.news.lost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.daedan.festabook.R
import com.daedan.festabook.databinding.FragmentLostItemBinding
import com.daedan.festabook.di.appGraph
import com.daedan.festabook.presentation.common.BaseFragment
import com.daedan.festabook.presentation.news.NewsViewModel
import com.daedan.festabook.presentation.news.lost.component.LostItemScreenContainer

class LostItemFragment : BaseFragment<FragmentLostItemBinding>() {
    override val layoutId: Int = R.layout.fragment_lost_item

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = appGraph.metroViewModelFactory

    private val viewModel: NewsViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LostItemScreenContainer(newsViewModel = viewModel)
            }
        }

    companion object {
        fun newInstance() = LostItemFragment()
    }
}
