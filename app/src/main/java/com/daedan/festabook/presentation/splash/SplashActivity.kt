package com.daedan.festabook.presentation.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daedan.festabook.FestaBookApp
import com.daedan.festabook.presentation.explore.ExploreActivity
import com.daedan.festabook.presentation.main.MainActivity
import com.daedan.festabook.presentation.splash.component.NetworkErrorDialog
import com.daedan.festabook.presentation.splash.component.UpdateDialog
import com.daedan.festabook.presentation.theme.FestabookTheme
import dev.zacsweers.metro.Inject

class SplashActivity : AppCompatActivity() {
    private val viewModel: SplashViewModel by viewModels()

    private val updateResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.handleVersionCheckResult(Result.success(false))
            } else {
                viewModel.handleVersionCheckResult(Result.failure(Exception("Update failed")))
            }
        }

    @Inject
    override lateinit var defaultViewModelProviderFactory: ViewModelProvider.Factory

    @Inject
    private lateinit var appVersionManagerFactory: AppVersionManager.Factory

    private val appVersionManager by lazy { appVersionManagerFactory.create(updateResultLauncher) }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as FestaBookApp).festaBookGraph.inject(this)

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value is SplashUiState.Loading
        }

        enableEdgeToEdge()

        setContent {
            FestabookTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    // 앱 실행 시 즉시 앱 버전 업데이트의 필요 유무 확인
                    val result = appVersionManager.getIsAppUpdateAvailable()
                    viewModel.handleVersionCheckResult(result)
                }

                LaunchedEffect(uiState) {
                    when (val state = uiState) {
                        is SplashUiState.NavigateToExplore -> {
                            startActivity(Intent(this@SplashActivity, ExploreActivity::class.java))
                            finish()
                        }

                        is SplashUiState.NavigateToMain -> {
                            val intent =
                                Intent(this@SplashActivity, MainActivity::class.java).apply {
                                    putExtra("festivalId", state.festivalId)
                                }
                            startActivity(intent)
                            finish()
                        }

                        else -> {}
                    }
                }

                when (uiState) {
                    is SplashUiState.ShowUpdateDialog -> {
                        UpdateDialog(
                            onConfirm = { appVersionManager.updateApp() },
                        )
                    }

                    is SplashUiState.ShowNetworkErrorDialog -> {
                        NetworkErrorDialog(
                            onConfirm = { finish() },
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}
