package com.daedan.festabook.presentation.setting.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.Header
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.FestabookTypography
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun SettingScreen(
    universityName: String,
    isUniversitySubscribed: Boolean,
    appVersion: String,
    modifier: Modifier = Modifier,
    onSubscribeClick: (Boolean) -> Unit = {},
    onPolicyClick: () -> Unit = {},
    onContactUsClick: () -> Unit = {},
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidthDp =
        with(density) {
            windowInfo.containerSize.width.toDp()
        }

    Scaffold(
        topBar = {
            Header(
                title = stringResource(R.string.setting_title),
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(horizontal = festabookSpacing.paddingScreenGutter)
                    .padding(innerPadding),
        ) {
            SubscriptionContent(
                universityName = universityName,
                isUniversitySubscribed = isUniversitySubscribed,
                onSubscribeClick = onSubscribeClick,
            )

            HorizontalDivider(
                modifier =
                    Modifier
                        .requiredWidth(screenWidthDp)
                        .padding(vertical = 20.dp),
                color = FestabookColor.gray100,
                thickness = festabookSpacing.paddingBody2,
            )

            AppInfoContent(
                appVersion = appVersion,
                onPolicyClick = onPolicyClick,
                onContactUsClick = onContactUsClick,
            )
        }
    }
}

@Composable
private fun SubscriptionContent(
    universityName: String,
    isUniversitySubscribed: Boolean,
    onSubscribeClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.setting_notice_title),
            style = FestabookTypography.bodyMedium,
            modifier = Modifier.padding(top = 20.dp),
        )

        Row(
            modifier = Modifier.wrapContentSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.setting_current_university_notice),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = FestabookTypography.titleMedium,
                    modifier =
                        Modifier.padding(
                            top = festabookSpacing.paddingBody3,
                        ),
                )

                Text(
                    text = universityName,
                    style = FestabookTypography.bodyMedium,
                    modifier = Modifier.padding(vertical = festabookSpacing.paddingBody1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = FestabookColor.gray500,
                )
            }

            Switch(
                modifier = Modifier.wrapContentSize(),
                checked = isUniversitySubscribed,
                onCheckedChange = onSubscribeClick,
                colors =
                    SwitchDefaults.colors().copy(
                        checkedBorderColor = Color.Transparent,
                        uncheckedBorderColor = Color.Transparent,
                        disabledCheckedBorderColor = Color.Transparent,
                        disabledUncheckedBorderColor = Color.Transparent,
                        checkedTrackColor = FestabookColor.black,
                        uncheckedTrackColor = FestabookColor.gray200,
                    ),
            )
        }
    }
}

@Composable
private fun AppInfoContent(
    appVersion: String,
    onPolicyClick: () -> Unit,
    onContactUsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidthDp =
        with(density) {
            windowInfo.containerSize.width.toDp()
        }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.setting_app_info_title),
            modifier = Modifier.padding(vertical = festabookSpacing.paddingBody3),
            style = FestabookTypography.bodyMedium,
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = festabookSpacing.paddingBody3),
        ) {
            Text(
                text = stringResource(R.string.setting_app_version),
                style = FestabookTypography.titleMedium,
            )

            Text(
                text = appVersion,
                style = FestabookTypography.bodyMedium,
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .requiredWidth(screenWidthDp)
                    .clickable {
                        onPolicyClick()
                    }.padding(
                        horizontal = festabookSpacing.paddingScreenGutter,
                        vertical = festabookSpacing.paddingBody3,
                    ),
        ) {
            Text(
                text = stringResource(R.string.setting_service_policy),
                style = FestabookTypography.titleMedium,
            )

            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward_right),
                contentDescription = stringResource(R.string.move),
                tint = Color.Unspecified,
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .requiredWidth(screenWidthDp)
                    .clickable {
                        onContactUsClick()
                    }.padding(
                        horizontal = festabookSpacing.paddingScreenGutter,
                        vertical = festabookSpacing.paddingBody3,
                    ),
        ) {
            Text(
                text = stringResource(R.string.setting_contact_us),
                style = FestabookTypography.titleMedium,
            )

            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward_right),
                contentDescription = stringResource(R.string.move),
                tint = Color.Unspecified,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    FestabookTheme {
        var isSubscribed by remember { mutableStateOf(false) }
        SettingScreen(
            universityName = "성균관대학교 인문사회과학철학문학자연캠퍼스 인문사회과학철학문학자연캠퍼스",
            isUniversitySubscribed = isSubscribed,
            onSubscribeClick = { isSubscribed = !isSubscribed },
            appVersion = "v1.0.0",
        )
    }
}
