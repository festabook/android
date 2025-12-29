package com.daedan.festabook.presentation.placeMap.component

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.festabookShapes

@Composable
fun BackToPositionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    AssistChip(
        modifier = modifier,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        border =
            AssistChipDefaults.assistChipBorder(
                enabled = true,
                borderColor = FestabookColor.black,
                borderWidth = 1.dp,
            ),
        colors =
            AssistChipDefaults.assistChipColors(
                containerColor = FestabookColor.white,
            ),
        shape = festabookShapes.radiusFull,
    )
}

@Preview(showBackground = true)
@Composable
private fun BackToPositionButtonPreview() {
    FestabookTheme {
        BackToPositionButton(
            text = "학교로 돌아가기",
        )
    }
}
