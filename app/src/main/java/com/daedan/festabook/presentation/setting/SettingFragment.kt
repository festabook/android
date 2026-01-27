package com.daedan.festabook.presentation.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.BuildConfig
import com.daedan.festabook.R
import com.daedan.festabook.databinding.FragmentSettingBinding
import com.daedan.festabook.di.fragment.FragmentKey
import com.daedan.festabook.presentation.NotificationPermissionManager
import com.daedan.festabook.presentation.NotificationPermissionRequester
import com.daedan.festabook.presentation.common.BaseFragment
import com.daedan.festabook.presentation.common.ObserveAsEvents
import com.daedan.festabook.presentation.common.showErrorSnackBar
import com.daedan.festabook.presentation.common.showNotificationDeniedSnackbar
import com.daedan.festabook.presentation.common.showSnackBar
import com.daedan.festabook.presentation.home.HomeViewModel
import com.daedan.festabook.presentation.setting.component.SettingScreen
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import timber.log.Timber

@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<Fragment>(),
)
@FragmentKey(SettingFragment::class)
@Inject
class SettingFragment(
    private val notificationPermissionManagerFactory: NotificationPermissionManager.Factory,
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory,
) : BaseFragment<FragmentSettingBinding>(),
    NotificationPermissionRequester {
    override val layoutId: Int = R.layout.fragment_setting
    private val settingViewModel: SettingViewModel by viewModels({ requireActivity() })
    private val homeViewModel: HomeViewModel by viewModels({ requireActivity() })

    private val notificationPermissionManager by lazy {
        notificationPermissionManagerFactory.create(
            requester = this,
            onPermissionGranted = { onPermissionGranted() },
            onPermissionDenied = { onPermissionDenied() },
        )
    }

    override val permissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                Timber.d("Notification permission granted")
                onPermissionGranted()
            } else {
                Timber.d("Notification permission denied")
                showNotificationDeniedSnackbar(requireView(), requireContext())
                onPermissionDenied()
            }
        }

    override fun onPermissionGranted() {
        settingViewModel.saveNotificationId()
    }

    override fun onPermissionDenied() = Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            super.onCreateView(inflater, container, savedInstanceState)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val festival by homeViewModel.festivalUiState.collectAsStateWithLifecycle()
                val isUniversitySubscribed by settingViewModel.isAllowed.collectAsStateWithLifecycle()
                val isSubscribedLoading by settingViewModel.isLoading.collectAsStateWithLifecycle()
                val context = LocalContext.current

                ObserveAsEvents(flow = settingViewModel.permissionCheckEvent) {
                    notificationPermissionManager.requestNotificationPermission(context)
                }

                ObserveAsEvents(flow = settingViewModel.success) {
                    requireActivity().showSnackBar(getString(R.string.setting_notice_enabled))
                }

                ObserveAsEvents(flow = settingViewModel.error) {
                    showErrorSnackBar(it)
                }

                SettingScreen(
                    festivalUiState = festival,
                    isUniversitySubscribed = isUniversitySubscribed,
                    appVersion = BuildConfig.VERSION_NAME,
                    isSubscribeEnabled = !isSubscribedLoading,
                    onSubscribeClick = {
                        settingViewModel.notificationAllowClick()
                    },
                    onPolicyClick = {
                        val intent = Intent(Intent.ACTION_VIEW, POLICY_URL.toUri())
                        startActivity(intent)
                    },
                    onContactUsClick = {
                        val intent = Intent(Intent.ACTION_VIEW, CONTACT_US_URL.toUri())
                        startActivity(intent)
                    },
                    onError = {
                        showErrorSnackBar(it.throwable)
                        Timber.w(
                            it.throwable,
                            "${this::class.simpleName}: ${it.throwable.message}",
                        )
                    },
                )
            }
        }

    override fun shouldShowPermissionRationale(permission: String): Boolean = shouldShowRequestPermissionRationale(permission)

    companion object {
        private const val POLICY_URL: String =
            "https://www.notion.so/244a540dc0b780638e56e31c4bdb3c9f"

        private const val CONTACT_US_URL =
            "https://forms.gle/XjqJFfQrTPgkZzGZ9"
    }
}
