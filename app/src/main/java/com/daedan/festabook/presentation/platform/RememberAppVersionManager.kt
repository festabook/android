package com.daedan.festabook.presentation.platform

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.daedan.festabook.presentation.splash.AppVersionManager

@Composable
fun rememberAppVersionManager(
    factory: AppVersionManager.Factory,
    onUpdateSuccess: () -> Unit,
    onUpdateFailure: () -> Unit,
): AppVersionManager {
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onUpdateSuccess()
            } else {
                onUpdateFailure()
            }
        }

    return remember(factory, launcher) {
        factory.create(launcher)
    }
}
