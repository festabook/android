package com.daedan.festabook.presentation.platform

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer

object DeepLinkKeys {
    const val KEY_NOTICE_ID_TO_EXPAND = "noticeIdToExpand"
    const val KEY_CAN_NAVIGATE_TO_NEWS = "canNavigateToNews"
    const val INITIALIZED_ID = -1L
}

@Composable
fun RememberDeepLinkHandler(onDeepLink: (Intent) -> Unit) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val currentOnDeepLink by rememberUpdatedState(onDeepLink)

    LaunchedEffect(Unit) {
        currentOnDeepLink(activity.intent)
    }

    DisposableEffect(activity) {
        val listener =
            Consumer<Intent> { intent ->
                currentOnDeepLink(intent)
            }
        activity.addOnNewIntentListener(listener)
        onDispose { activity.removeOnNewIntentListener(listener) }
    }
}
