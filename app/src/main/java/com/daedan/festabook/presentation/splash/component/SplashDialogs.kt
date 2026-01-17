package com.daedan.festabook.presentation.splash.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.daedan.festabook.R
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.FestabookTypography
import com.daedan.festabook.presentation.theme.festabookShapes

@Composable
fun UpdateDialog(onConfirm: () -> Unit) {
    SplashInfoDialog(
        title = stringResource(id = R.string.update_notice_title),
        message = stringResource(id = R.string.update_notice_message),
        buttonText = stringResource(id = R.string.update_notice_confirm),
        iconResId = R.drawable.ic_alarm,
        confirmButtonColor = FestabookColor.accentBlue,
        onConfirm = onConfirm,
    )
}

@Composable
fun NetworkErrorDialog(onConfirm: () -> Unit) {
    SplashInfoDialog(
        title = stringResource(id = R.string.update_failed_title),
        message = stringResource(id = R.string.update_failed_message),
        buttonText = stringResource(id = R.string.update_failed_confirm),
        confirmButtonColor = FestabookColor.gray400,
        onConfirm = onConfirm,
    )
}

@Composable
private fun SplashInfoDialog(
    title: String,
    message: String,
    buttonText: String,
    @DrawableRes iconResId: Int? = null,
    confirmButtonColor: Color,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = {}) {
        Column(
            modifier =
                Modifier
                    .background(
                        color = FestabookColor.white,
                        shape = festabookShapes.radius4,
                    ).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            iconResId?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = title,
                style = FestabookTypography.displaySmall,
                textAlign = TextAlign.Center,
                color = FestabookColor.gray800,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = FestabookTypography.bodyMedium,
                textAlign = TextAlign.Center,
                color = FestabookColor.gray800,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onConfirm,
                modifier =
                    Modifier
                        .fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = confirmButtonColor,
                        contentColor = FestabookColor.white,
                    ),
                shape = festabookShapes.radiusFull,
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@Preview
@Composable
private fun UpdateDialogPreview() {
    FestabookTheme {
        UpdateDialog(onConfirm = {})
    }
}

@Preview
@Composable
private fun NetworkErrorDialogPreview() {
    FestabookTheme {
        NetworkErrorDialog(onConfirm = {})
    }
}
