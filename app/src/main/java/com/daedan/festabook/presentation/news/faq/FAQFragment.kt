package com.daedan.festabook.presentation.news.faq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.daedan.festabook.R
import com.daedan.festabook.databinding.FragmentFaqBinding
import com.daedan.festabook.di.appGraph
import com.daedan.festabook.presentation.common.BaseFragment
import com.daedan.festabook.presentation.news.NewsViewModel
import com.daedan.festabook.presentation.news.faq.component.FAQScreenContainer

class FAQFragment : BaseFragment<FragmentFaqBinding>() {
    override val layoutId: Int = R.layout.fragment_faq

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
                FAQScreenContainer(newsViewModel = viewModel)
            }
        }

    companion object {
        fun newInstance() = FAQFragment()
    }
}
