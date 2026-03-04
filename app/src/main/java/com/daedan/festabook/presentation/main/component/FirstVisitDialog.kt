package com.daedan.festabook.presentation.main.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.daedan.festabook.R
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.FestabookTypography
import com.daedan.festabook.presentation.theme.festabookShapes
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun FirstVisitDialog(
    onConfirm: () -> Unit,
    onDecline: () -> Unit = {},
) {
    var isVisible by remember { mutableStateOf(true) }
    if (isVisible) {
        FirstVisitInfoDialog(
            title = stringResource(id = R.string.main_alarm_dialog_title),
            message = stringResource(id = R.string.main_alarm_dialog_message),
            confirmButtonText = stringResource(id = R.string.main_alarm_dialog_confirm_button),
            declineButtonText = stringResource(id = R.string.main_alarm_dialog_cancel_button),
            iconResId = R.drawable.ic_alarm,
            confirmButtonColor = FestabookColor.accentBlue,
            declineButtonColor = FestabookColor.gray400,
            onConfirm = {
                onConfirm()
                isVisible = false
            },
            onDecline = onDecline,
        )
    }
}

@Composable
private fun FirstVisitInfoDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    declineButtonText: String,
    confirmButtonColor: Color,
    declineButtonColor: Color,
    onConfirm: () -> Unit,
    onDecline: () -> Unit,
    @DrawableRes iconResId: Int? = null,
) {
    Dialog(
        onDismissRequest = { onDecline() },
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
    ) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onDecline,
                    modifier =
                        Modifier
                            .wrapContentWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = FestabookColor.white,
                            contentColor = FestabookColor.white,
                        ),
                    shape = festabookShapes.radiusFull,
                    border = BorderStroke(width = 1.dp, declineButtonColor),
                    contentPadding = PaddingValues(festabookSpacing.paddingBody1),
                ) {
                    Text(
                        color = declineButtonColor,
                        text = declineButtonText,
                    )
                }
                Spacer(Modifier.padding(festabookSpacing.paddingBody1))
                Button(
                    onClick = onConfirm,
                    modifier =
                        Modifier
                            .wrapContentWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = confirmButtonColor,
                            contentColor = FestabookColor.white,
                        ),
                    shape = festabookShapes.radiusFull,
                    contentPadding = PaddingValues(festabookSpacing.paddingBody3),
                ) {
                    Text(text = confirmButtonText)
                }
            }
        }
    }
}

@Preview
@Composable
private fun UpdateDialogPreview() {
    FestabookTheme {
        FirstVisitDialog(
            onConfirm = {},
            onDecline = {},
        )
    }
}
