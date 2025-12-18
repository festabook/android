package com.daedan.festabook.presentation.schedule

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
import com.daedan.festabook.databinding.FragmentScheduleBinding
import com.daedan.festabook.di.fragment.FragmentKey
import com.daedan.festabook.presentation.common.BaseFragment
import com.daedan.festabook.presentation.common.OnMenuItemReClickListener
import com.daedan.festabook.presentation.schedule.component.ScheduleScreen
import com.daedan.festabook.presentation.theme.FestabookTheme
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<Fragment>(),
)
@FragmentKey(ScheduleFragment::class)
@Inject
class ScheduleFragment :
    BaseFragment<FragmentScheduleBinding>(),
    OnMenuItemReClickListener {
    override val layoutId: Int = R.layout.fragment_schedule

    @Inject
    override lateinit var defaultViewModelProviderFactory: ViewModelProvider.Factory

    private val viewModel: ScheduleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FestabookTheme {
                    ScheduleScreen(scheduleViewModel = viewModel)
                }
            }
        }

    override fun onMenuItemReClick() {
//        viewModel.loadAllDates(ScheduleUiState.InitialLoading)
//        viewModel.loadScheduleByDate()
//        binding.logger.log(ScheduleMenuItemReClickLogData(binding.logger.getBaseLogData()))
    }
}
