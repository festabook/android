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

    private val newsViewModel: NewsViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FestabookTheme {
                    NewsScreen(newsViewModel = newsViewModel)
                }
            }
        }
}
