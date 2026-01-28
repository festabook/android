package com.daedan.festabook.presentation.common.component

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.daedan.festabook.R
import com.daedan.festabook.data.util.ApiResultException
import com.daedan.festabook.presentation.theme.FestabookColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@Composable
fun FestabookSnackbar(
    data: SnackbarData,
    modifier: Modifier = Modifier,
) {
    Snackbar(
        modifier = modifier,
        snackbarData = data,
        actionColor = FestabookColor.accentBlue,
    )
}

class SnackbarManager(
    val hostState: SnackbarHostState,
    val scope: CoroutineScope,
    private val actionLabel: String,
    private val errorMessages: Map<KClass<out ApiResultException>, String>,
    private val defaultErrorMessage: String,
) {
    fun show(message: String) {
        hostState.currentSnackbarData?.dismiss()
        scope.launch {
            hostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
                actionLabel = actionLabel,
            )
        }
    }

    fun showError(throwable: Throwable) {
        val message = errorMessages[throwable::class] ?: throwable.message ?: defaultErrorMessage
        show(message)
    }
}

@Composable
fun rememberAppSnackbarManager(
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope = rememberCoroutineScope(),
): SnackbarManager {
    val clientErrorMessage = stringResource(R.string.error_client_exception)
    val serverErrorMessage = stringResource(R.string.error_server_exception)
    val networkErrorMessage = stringResource(R.string.error_network_exception)
    val unknownErrorMessage = stringResource(R.string.error_unknown_exception)
    val actionLabel = stringResource(R.string.fail_snackbar_confirm)

    val errorMessages =
        remember {
            mapOf(
                ApiResultException.ClientException::class to clientErrorMessage,
                ApiResultException.ServerException::class to serverErrorMessage,
                ApiResultException.NetworkException::class to networkErrorMessage,
                ApiResultException.UnknownException::class to unknownErrorMessage,
            )
        }

    return remember(snackbarHostState, scope) {
        SnackbarManager(snackbarHostState, scope, actionLabel, errorMessages, unknownErrorMessage)
    }
}
