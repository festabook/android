package com.daedan.festabook.presentation.placeMap.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.getIconId
import com.daedan.festabook.presentation.placeMap.model.getLabelColor
import com.daedan.festabook.presentation.placeMap.model.getTextId
import com.daedan.festabook.presentation.theme.festabookShapes
import com.daedan.festabook.presentation.theme.festabookSpacing
import kotlin.math.roundToInt

@Composable
fun PlaceCategoryLabel(
    category: PlaceCategoryUiModel,
    modifier: Modifier = Modifier,
    iconColor: Color = category.getLabelColor(),
) {
    Card(
        shape = festabookShapes.radius1,
        colors =
            CardColors(
                containerColor = getBackgroundColor(iconColor),
                contentColor = Color.Unspecified,
                disabledContainerColor = getBackgroundColor(iconColor),
                disabledContentColor = Color.Unspecified,
            ),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(category.getIconId()),
                contentDescription = stringResource(category.getTextId()),
                tint = iconColor,
                modifier = Modifier.size(12.dp),
            )
            Text(
                modifier = Modifier.padding(start = festabookSpacing.paddingBody1),
                text = stringResource(category.getTextId()),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

private fun getBackgroundColor(color: Color): Color {
    // 10% 투명도를 가지게 변경
    val alpha = (MAX_ALPHA * ALPHA_RATIO).roundToInt()
    return color.copy(alpha = alpha / MAX_ALPHA.toFloat())
}

private const val MAX_ALPHA = 255
private const val ALPHA_RATIO = 0.10f

@Preview(showBackground = true)
@Composable
private fun PlaceCategoryLabelPreview() {
    val category = PlaceCategoryUiModel.FOOD_TRUCK
    PlaceCategoryLabel(
        category = category,
        iconColor = Color(0xFF00AB40),
    )
}
