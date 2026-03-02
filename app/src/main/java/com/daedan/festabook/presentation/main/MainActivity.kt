package com.daedan.festabook.presentation.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.daedan.festabook.R
import com.daedan.festabook.di.appGraph
import com.daedan.festabook.presentation.FestabookScreen
import com.daedan.festabook.presentation.common.isGranted
import com.daedan.festabook.presentation.common.showNotificationDeniedSnackbar
import com.daedan.festabook.presentation.theme.FestabookTheme

class MainActivity : AppCompatActivity() {
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = appGraph.metroViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FestabookTheme {
                FestabookScreen()
            }
        }
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
                        // 이 부분은 레거시지만 NaverMap이 자동으로 권한 설정을 하기 때문에
                        // 마이그래이션 하려면 MainActivity에 전역 State를 뚫어야 할 것 같습니다
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

    companion object {
        fun newIntent(context: Context) =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
    }
}
