package com.daedan.festabook.presentation.platform

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.daedan.festabook.presentation.NotificationPermissionManager
import timber.log.Timber

@Composable
fun rememberNotificationPermissionManager(
    factory: NotificationPermissionManager.Factory,
    onPermissionGrant: () -> Unit,
    onPermissionDeny: () -> Unit,
): NotificationPermissionManager {
    val context = LocalContext.current

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                Timber.d("Notification permission granted")
                onPermissionGrant()
            } else {
                Timber.d("Notification permission denied")
                onPermissionDeny()
            }
        }

    return remember(factory, permissionLauncher) {
        factory.create(
            launchPermission = { permission -> permissionLauncher.launch(permission) },
            shouldShowRationale = { permission ->
                (context as? ComponentActivity)
                    ?.shouldShowRequestPermissionRationale(permission) ?: false
            },
            onPermissionGranted = onPermissionGrant,
            onPermissionDenied = onPermissionDeny,
        )
    }
}
