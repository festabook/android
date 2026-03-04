package com.daedan.festabook.presentation.common.component

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.daedan.festabook.presentation.theme.FestabookColor

@Composable
fun FestabookSwitch(
    enabled: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Switch(
        enabled = enabled,
        modifier = modifier.wrapContentSize(),
        checked = checked,
        onCheckedChange = onCheckedChange,
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
