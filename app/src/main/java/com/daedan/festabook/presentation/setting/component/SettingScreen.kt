package com.daedan.festabook.presentation.setting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import com.daedan.festabook.domain.model.Festival
import com.daedan.festabook.domain.model.Organization
import com.daedan.festabook.presentation.common.component.FestabookTopAppBar
import com.daedan.festabook.presentation.home.adapter.FestivalUiState
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.FestabookTypography
import com.daedan.festabook.presentation.theme.festabookSpacing
import java.time.LocalDate

@Composable
fun SettingScreen(
    festivalUiState: FestivalUiState,
    isUniversitySubscribed: Boolean,
    appVersion: String,
    isSubscribeEnabled: Boolean,
    modifier: Modifier = Modifier,
    onSubscribeClick: (Boolean) -> Unit = {},
    onPolicyClick: () -> Unit = {},
    onContactUsClick: () -> Unit = {},
    onError: (FestivalUiState.Error) -> Unit = {},
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidthDp =
        remember {
            with(density) {
                windowInfo.containerSize.width.toDp()
            }
        }

    val currentOnError by rememberUpdatedState(onError)

    LaunchedEffect(festivalUiState) {
        when (festivalUiState) {
            is FestivalUiState.Error -> currentOnError(festivalUiState)
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            FestabookTopAppBar(
                title = stringResource(R.string.setting_title),
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(color = FestabookColor.white)
                    .padding(horizontal = festabookSpacing.paddingScreenGutter)
                    .padding(innerPadding),
        ) {
            when (festivalUiState) {
                is FestivalUiState.Success -> {
                    SubscriptionContent(
                        universityName = festivalUiState.organization.universityName,
                        isUniversitySubscribed = isUniversitySubscribed,
                        onSubscribeClick = onSubscribeClick,
                        isSubscribeEnabled = isSubscribeEnabled,
                    )
                }

                else -> Unit
            }

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
                modifier = Modifier.background(color = FestabookColor.white),
            )
        }
    }
}

@Composable
private fun SubscriptionContent(
    universityName: String,
    isUniversitySubscribed: Boolean,
    isSubscribeEnabled: Boolean,
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
                enabled = isSubscribeEnabled,
                modifier = Modifier.wrapContentSize(),
                checked = isUniversitySubscribed,
                onCheckedChange = onSubscribeClick,
                colors =
                    SwitchDefaults.colors().copy(
                        checkedBorderColor = Color.Transparent,
                        uncheckedBorderColor = Color.Transparent,
                        disabledCheckedTrackColor = FestabookColor.black,
                        disabledUncheckedTrackColor = FestabookColor.gray200,
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
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.setting_app_info_title),
            modifier = Modifier.padding(vertical = festabookSpacing.paddingBody3),
            style = FestabookTypography.bodyMedium,
        )

        AppVersionInfo(
            appVersion = appVersion,
        )

        AppInfoButton(
            text = stringResource(R.string.setting_service_policy),
            onClick = onPolicyClick,
        )
        AppInfoButton(
            text = stringResource(R.string.setting_contact_us),
            onClick = onContactUsClick,
        )
    }
}

@Composable
private fun AppVersionInfo(
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
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
}

@Composable
private fun AppInfoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidthDp =
        remember {
            with(density) {
                windowInfo.containerSize.width.toDp()
            }
        }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .requiredWidth(screenWidthDp)
                .clickable {
                    onClick()
                }.padding(
                    horizontal = festabookSpacing.paddingScreenGutter,
                    vertical = festabookSpacing.paddingBody3,
                ),
    ) {
        Text(
            text = text,
            style = FestabookTypography.titleMedium,
        )

        Icon(
            painter = painterResource(R.drawable.ic_arrow_forward_right),
            contentDescription = stringResource(R.string.move),
            tint = Color.Unspecified,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    FestabookTheme {
        var isSubscribed by remember { mutableStateOf(false) }
        SettingScreen(
            festivalUiState =
                FestivalUiState.Success(
                    Organization(
                        id = 1,
                        universityName = "성균관대학교 인문사회과학철학문학자연캠퍼스 인문사회과학철학문학자연캠퍼스",
                        festival =
                            Festival(
                                festivalName = "성균관대학교 축제축제축제축제축제축제축제축제축제축제축제축제",
                                festivalImages = listOf(),
                                startDate = LocalDate.of(2026, 1, 1),
                                endDate = LocalDate.of(2026, 2, 1),
                            ),
                    ),
                ),
            isUniversitySubscribed = isSubscribed,
            onSubscribeClick = { isSubscribed = !isSubscribed },
            appVersion = "v1.0.0",
            isSubscribeEnabled = true,
        )
    }
}
