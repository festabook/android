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
import com.daedan.festabook.logging.logger
import com.daedan.festabook.logging.model.schedule.ScheduleMenuItemReClickLogData
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

//    override fun onViewCreated(
//        view: View,
//        savedInstanceState: Bundle?,
//    ) {
//        binding.vpSchedule.adapter = adapter
//        setupObservers()
//    }

    override fun onMenuItemReClick() {
        viewModel.loadAllDates()
//        viewModel.loadScheduleByDate()
        binding.logger.log(ScheduleMenuItemReClickLogData(binding.logger.getBaseLogData()))
    }

//    @SuppressLint("WrongConstant")
//    private fun setupScheduleTabLayout(initialCurrentDateIndex: Int) {
//        binding.vpSchedule.offscreenPageLimit = PRELOAD_PAGE_COUNT
//
//        TabLayoutMediator(binding.tlSchedule, binding.vpSchedule) { tab, position ->
//            setupScheduleTabView(tab, position)
//            binding.vpSchedule.setCurrentItem(initialCurrentDateIndex, false)
//        }.attach()
//    }
//
//    private fun setupScheduleTabView(
//        tab: TabLayout.Tab,
//        position: Int,
//    ) {
//        val itemScheduleTabBinding =
//            ItemScheduleTabBinding.inflate(
//                LayoutInflater.from(requireContext()),
//                binding.tlSchedule,
//                false,
//            )
//        tab.customView = itemScheduleTabBinding.root
//
//        itemScheduleTabBinding.tvScheduleTabItem.text =
//            viewModel.scheduleDatesUiState.value
//                .let {
//                    (it as? ScheduleDatesUiState.Success)?.dates?.get(position)?.date
//                        ?: EMPTY_DATE_TEXT
//                }
//    }

    private fun setupObservers() {
//        viewModel.scheduleDatesUiState.observe(viewLifecycleOwner) { scheduleDatesUiState ->
//
//            when (scheduleDatesUiState) {
//                is ScheduleDatesUiState.Loading -> {
//                    showLoadingView(isLoading = true)
//                }
//
//                is ScheduleDatesUiState.Success -> {
//                    showLoadingView(isLoading = false)
//                    setupScheduleTabLayout(scheduleDatesUiState.initialDatePosition)
//                    adapter.submitList(scheduleDatesUiState.dates)
//                }
//
//                is ScheduleDatesUiState.Error -> {
//                    showLoadingView(isLoading = false)
//                    Timber.w(
//                        scheduleDatesUiState.throwable,
//                        "${this::class.simpleName}: ${scheduleDatesUiState.throwable.message}",
//                    )
//                    showErrorSnackBar(scheduleDatesUiState.throwable)
//                }
//            }
//        }
    }

//    private fun showLoadingView(isLoading: Boolean) {
//        binding.lavScheduleLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
//        binding.vpSchedule.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
//    }
//
//    companion object {
//        private const val PRELOAD_PAGE_COUNT: Int = 2
//        private const val EMPTY_DATE_TEXT: String = ""
//    }
}
