package com.daedan.festabook.presentation.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.daedan.festabook.R
import com.daedan.festabook.databinding.FragmentNewsBinding
import com.daedan.festabook.di.fragment.FragmentKey
import com.daedan.festabook.presentation.common.BaseFragment
import com.daedan.festabook.presentation.main.MainViewModel
import com.daedan.festabook.presentation.news.adapter.NewsPagerAdapter
import com.daedan.festabook.presentation.news.component.NewsScreen
import com.daedan.festabook.presentation.theme.FestabookTheme
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<Fragment>(),
)
@FragmentKey(NewsFragment::class)
@Inject
class NewsFragment : BaseFragment<FragmentNewsBinding>() {
    override val layoutId: Int = R.layout.fragment_news

    @Inject
    override lateinit var defaultViewModelProviderFactory: ViewModelProvider.Factory

    private val newsPagerAdapter by lazy {
        NewsPagerAdapter(this)
    }
    private val newsViewModel: NewsViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FestabookTheme {
                    NewsScreen(mainViewModel = mainViewModel, newsViewModel = newsViewModel)
                }
            }
        }

//    override fun onViewCreated(
//        view: View,
//        savedInstanceState: Bundle?,
//    ) {
//        super.onViewCreated(view, savedInstanceState)
//        binding.lifecycleOwner = viewLifecycleOwner
//        setupNewsTabLayout()
//        mainViewModel.noticeIdToExpand.observe(viewLifecycleOwner) {
//            binding.vpNews.currentItem = NOTICE_TAB_INDEX
//        }
//    }
//
//    private fun setupNewsTabLayout() {
//        binding.vpNews.adapter = newsPagerAdapter
//        TabLayoutMediator(binding.tlNews, binding.vpNews) { tab, position ->
//            val tabNameRes = NewsTab.entries[position].tabNameRes
//            tab.text = getString(tabNameRes)
//        }.attach()
//    }
//
//    companion object {
//        private const val NOTICE_TAB_INDEX: Int = 0
//    }
}
