package com.daedan.festabook.presentation.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModelProvider
import com.daedan.festabook.R
import com.daedan.festabook.di.appGraph
import com.daedan.festabook.presentation.FestabookScreen
import com.daedan.festabook.presentation.NotificationPermissionManager
import com.daedan.festabook.presentation.NotificationPermissionRequester
import com.daedan.festabook.presentation.common.isGranted
import com.daedan.festabook.presentation.common.showNotificationDeniedSnackbar
import com.daedan.festabook.presentation.news.NewsViewModel
import com.daedan.festabook.presentation.placeDetail.PlaceDetailViewModel
import com.daedan.festabook.presentation.setting.SettingViewModel
import com.daedan.festabook.presentation.splash.AppVersionManager
import com.daedan.festabook.presentation.splash.SplashViewModel
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.naver.maps.map.util.FusedLocationSource
import dev.zacsweers.metro.Inject
import timber.log.Timber

class MainActivity :
    AppCompatActivity(),
    NotificationPermissionRequester {
    @Inject
    override lateinit var defaultViewModelProviderFactory: ViewModelProvider.Factory

    @Inject
    private lateinit var viewModelFactory: PlaceDetailViewModel.Factory

    @Inject
    private lateinit var notificationPermissionManagerFactory: NotificationPermissionManager.Factory

    @Inject
    private lateinit var appVersionManagerFactory: AppVersionManager.Factory

    private val mainViewModel: MainViewModel by viewModels()
    private val newsViewModel: NewsViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()

    private val splashViewModel: SplashViewModel by viewModels()

    private val notificationPermissionManager by lazy {
        notificationPermissionManagerFactory.create(
            requester = this,
            onPermissionGranted = { onPermissionGranted() },
            onPermissionDenied = { onPermissionDenied() },
        )
    }

    private val updateResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                splashViewModel.handleVersionCheckResult(Result.success(false))
            } else {
                splashViewModel.handleVersionCheckResult(Result.failure(Exception("Update failed")))
            }
        }

    private val locationSource by lazy {
        FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    private val appVersionManager by lazy { appVersionManagerFactory.create(updateResultLauncher) }

    override val permissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                Timber.d("Notification permission granted")
                onPermissionGranted()
            } else {
                Timber.d("Notification permission denied")
                showNotificationDeniedSnackbar(window.decorView.rootView, this)
                onPermissionDenied()
            }
        }

    override fun onPermissionGranted() {
        settingViewModel.saveNotificationId()
    }

    override fun onPermissionDenied() = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        appGraph.inject(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(Unit) {
                handleNavigation(intent)
            }

            FestabookTheme {
                FestabookScreen(
                    notificationPermissionManager = notificationPermissionManager,
                    logger = appGraph.defaultFirebaseLogger,
                    locationSource = locationSource,
                    placeDetailViewModelFactory = viewModelFactory,
                    appVersionManager = appVersionManager,
                    defaultViewModelFactory = defaultViewModelProviderFactory,
                )
            }
        }
        mainViewModel.registerDeviceAndFcmToken()
    }

    // TODO SnackBarHost로 변경
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        grantResults.forEachIndexed { index, result ->
            val text = permissions[index]
            when (text) {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                -> {
                    if (!result.isGranted()) {
                        showNotificationDeniedSnackbar(
                            window.decorView.rootView,
                            this,
                            getString(R.string.map_request_location_permission_message),
                        )
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun shouldShowPermissionRationale(permission: String): Boolean = shouldShowRequestPermissionRationale(permission)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNavigation(intent)
    }

    private fun handleNavigation(intent: Intent) {
        val noticeIdToExpand = intent.getLongExtra(KEY_NOTICE_ID_TO_EXPAND, INITIALIZED_ID)
        if (noticeIdToExpand != INITIALIZED_ID) newsViewModel.expandNotice(noticeIdToExpand)
        val canNavigateToNews = intent.getBooleanExtra(KEY_CAN_NAVIGATE_TO_NEWS, false)
        if (canNavigateToNews) {
            mainViewModel.navigateToNews()
        }
    }

    companion object {
        const val KEY_NOTICE_ID_TO_EXPAND = "noticeIdToExpand"
        const val KEY_CAN_NAVIGATE_TO_NEWS = "canNavigateToNews"
        const val LOCATION_PERMISSION_REQUEST_CODE = 1234

        private const val INITIALIZED_ID = -1L

        fun newIntent(context: Context) =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
    }
}
