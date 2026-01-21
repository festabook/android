package com.daedan.festabook.presentation.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.ImageResult
import com.daedan.festabook.R
import com.daedan.festabook.databinding.ActivityMainBinding
import com.daedan.festabook.di.appGraph
import com.daedan.festabook.presentation.NotificationPermissionManager
import com.daedan.festabook.presentation.NotificationPermissionRequester
import com.daedan.festabook.presentation.common.convertImageUrl
import com.daedan.festabook.presentation.common.isGranted
import com.daedan.festabook.presentation.common.showNotificationDeniedSnackbar
import com.daedan.festabook.presentation.common.showSnackBar
import com.daedan.festabook.presentation.common.showToast
import com.daedan.festabook.presentation.explore.ExploreActivity
import com.daedan.festabook.presentation.home.HomeFragment
import com.daedan.festabook.presentation.home.HomeViewModel
import com.daedan.festabook.presentation.main.component.MainScreen
import com.daedan.festabook.presentation.news.NewsViewModel
import com.daedan.festabook.presentation.placeMap.model.PlaceUiModel
import com.daedan.festabook.presentation.setting.SettingViewModel
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.naver.maps.map.util.FusedLocationSource
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class MainActivity :
    AppCompatActivity(),
    NotificationPermissionRequester {
    @Inject
    override lateinit var defaultViewModelProviderFactory: ViewModelProvider.Factory

    @Inject
    private lateinit var fragmentFactory: FragmentFactory

    @Inject
    private lateinit var notificationPermissionManagerFactory: NotificationPermissionManager.Factory
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var currentTabState: MutableState<FestabookMainTab>

    private val mainViewModel: MainViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val newsViewModel: NewsViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()

    private val notificationPermissionManager by lazy {
        notificationPermissionManagerFactory.create(
            requester = this,
            onPermissionGranted = { onPermissionGranted() },
            onPermissionDenied = { onPermissionDenied() },
        )
    }

    private val locationSource by lazy {
        FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
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
        setupFragmentFactory()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setupBinding()

        setContent {
            currentTabState = remember { mutableStateOf(FestabookMainTab.HOME) }
            LaunchedEffect(Unit) {
                handleNavigation(intent)
            }
            FestabookTheme {
                MainScreen(
                    notificationPermissionManager = notificationPermissionManager,
                    logger = appGraph.defaultFirebaseLogger,
                    locationSource = locationSource,
                    onNavigateToExplore = {
                        startActivity(ExploreActivity.newIntent(this))
                    },
                    onPreloadImages = { preloadImages(this, it.places) },
                )
//                FestabookBottomNavigationBar(
//                    currentTab = currentTabState.value,
//                    onTabSelect = { tab ->
//                        when (tab) {
//                            FestabookMainTab.HOME -> {
//                                currentTabState.value = FestabookMainTab.HOME
//                                switchFragment(HomeFragment::class.java, TAG_HOME_FRAGMENT)
//                            }
//
//                            FestabookMainTab.SCHEDULE -> {
//                                currentTabState.value = FestabookMainTab.SCHEDULE
//                                val fragment =
//                                    supportFragmentManager.findFragmentByTag(TAG_SCHEDULE_FRAGMENT)
//                                if (fragment is OnMenuItemReClickListener && !fragment.isHidden) fragment.onMenuItemReClick()
//                                switchFragment(
//                                    ScheduleFragment::class.java,
//                                    TAG_SCHEDULE_FRAGMENT,
//                                )
//                            }
//
//                            FestabookMainTab.PLACE_MAP -> {
//                                currentTabState.value = FestabookMainTab.PLACE_MAP
//                                val fragment =
//                                    supportFragmentManager.findFragmentByTag(TAG_PLACE_MAP_FRAGMENT)
//                                if (fragment is OnMenuItemReClickListener && !fragment.isHidden) fragment.onMenuItemReClick()
//                                switchFragment(PlaceMapFragment::class.java, TAG_PLACE_MAP_FRAGMENT)
//                            }
//
//                            FestabookMainTab.NEWS -> {
//                                currentTabState.value = FestabookMainTab.NEWS
//                                switchFragment(NewsFragment::class.java, TAG_NEWS_FRAGMENT)
//                            }
//
//                            FestabookMainTab.SETTING -> {
//                                currentTabState.value = FestabookMainTab.SETTING
//                                switchFragment(
//                                    SettingFragment::class.java,
//                                    TAG_SETTING_FRAGMENT,
//                                )
//                            }
//                        }
//                    },
//                )
            }
        }
        mainViewModel.registerDeviceAndFcmToken()
//        setupHomeFragment(savedInstanceState)
        setupObservers()
        onBackPress()
    }

    private fun setupBinding() {
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }

    private fun setupFragmentFactory() {
        supportFragmentManager.fragmentFactory = fragmentFactory
    }

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
                            binding.root,
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
        val canNavigateToNewsScreen =
            intent.getBooleanExtra(KEY_CAN_NAVIGATE_TO_NEWS, false)
        val noticeIdToExpand = intent.getLongExtra(KEY_NOTICE_ID_TO_EXPAND, INITIALIZED_ID)
        if (noticeIdToExpand != INITIALIZED_ID) newsViewModel.expandNotice(noticeIdToExpand)

        if (canNavigateToNewsScreen) {
            currentTabState.value = FestabookMainTab.NEWS
        }
    }

    private fun setupObservers() {
        mainViewModel.backPressEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { isDoublePress ->
                if (isDoublePress) finish() else showToast(getString(R.string.back_press_exit_message))
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.navigateToScheduleEvent.collect {
                    currentTabState.value = FestabookMainTab.SCHEDULE
                }
            }
        }

        mainViewModel.isFirstVisit.observe(this) { isFirstVisit ->
            if (isFirstVisit) {
                showAlarmDialog()
            }
        }
        settingViewModel.success.observe(this) {
            showSnackBar(getString(R.string.setting_notice_enabled))
        }
    }

    private fun setupHomeFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.commitNow {
                add<HomeFragment>(R.id.fcv_fragment_container)
            }
        }
    }

    private fun onBackPress() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    mainViewModel.onBackPressed()
                }
            },
        )
    }

    private fun switchFragment(
        fragment: Class<out Fragment>,
        tag: String,
    ) {
        supportFragmentManager.commit {
            supportFragmentManager.fragments.forEach { fragment -> hide(fragment) }

            val existing = supportFragmentManager.findFragmentByTag(tag)
            if (existing != null) {
                show(existing)
            } else {
                add(R.id.fcv_fragment_container, fragment, null, tag)
            }
            setReorderingAllowed(true)
        }
    }

    // OOM 주의 !! 추후 페이징 처리 및 chunk 단위로 나눠서 로드합니다
    private fun preloadImages(
        context: Context,
        places: List<PlaceUiModel?>,
        maxSize: Int = 20,
    ) {
        val imageLoader = context.imageLoader
        val deferredList = mutableListOf<Deferred<ImageResult?>>()

        lifecycleScope.launch(Dispatchers.IO) {
            places
                .take(maxSize)
                .filterNotNull()
                .forEach { place ->
                    val deferred =
                        async {
                            val request =
                                ImageRequest
                                    .Builder(context)
                                    .data(place.imageUrl.convertImageUrl())
                                    .build()

                            runCatching {
                                withTimeout(2000) {
                                    imageLoader.execute(request)
                                }
                            }.onFailure {
                                Timber.d("preload 실패")
                            }.getOrNull()
                        }
                    deferredList.add(deferred)
                }
            deferredList.awaitAll()
        }
    }

    private fun showAlarmDialog() {
        val dialog =
            MaterialAlertDialogBuilder(this, R.style.MainAlarmDialogTheme)
                .setView(R.layout.view_main_alert_dialog)
                .setPositiveButton(R.string.main_alarm_dialog_confirm_button) { _, _ ->
                    notificationPermissionManager.requestNotificationPermission(this)
                }.setNegativeButton(R.string.main_alarm_dialog_cancel_button) { dialog, _ ->
                    dialog.dismiss()
                }.create()
        dialog.show()
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
