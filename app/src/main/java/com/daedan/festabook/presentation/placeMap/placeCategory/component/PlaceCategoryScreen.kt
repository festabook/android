package com.daedan.festabook.presentation.placeMap.placeCategory.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.R
import com.daedan.festabook.presentation.placeMap.model.PlaceCategoryUiModel
import com.daedan.festabook.presentation.placeMap.model.getIconId
import com.daedan.festabook.presentation.placeMap.model.getTextId
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.FestabookTypography
import com.daedan.festabook.presentation.theme.festabookShapes
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun PlaceCategoryScreen(
    modifier: Modifier = Modifier,
    onDisplayAllClick: () -> Unit = {},
    onCategoryClick: (List<PlaceCategoryUiModel>) -> Unit = {},
    categories: List<PlaceCategoryUiModel> = PlaceCategoryUiModel.entries,
) {
    val scrollState = rememberScrollState()
    var selectedCategories by remember {
        mutableStateOf(emptySet<PlaceCategoryUiModel>())
    }

    Row(
        modifier =
            modifier
                .horizontalScroll(scrollState)
                .padding(
                    vertical = festabookSpacing.paddingBody2,
                    horizontal = festabookSpacing.paddingScreenGutter,
                ),
        horizontalArrangement = Arrangement.spacedBy(festabookSpacing.paddingBody2),
    ) {
        CategoryChip(
            text = stringResource(R.string.map_category_all),
            selected = selectedCategories.isEmpty(),
            onClick = {
                selectedCategories = emptySet()
                onDisplayAllClick()
            },
        )

        categories.forEach { category ->
            val text = stringResource(category.getTextId())
            CategoryChip(
                text = text,
                selected = selectedCategories.contains(category),
                icon = {
                    Icon(
                        painter = painterResource(category.getIconId()),
                        contentDescription = text,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
                onClick = {
                    selectedCategories =
                        if (selectedCategories.contains(category)) {
                            selectedCategories
                                .filter { it != category }
                                .toSet()
                        } else {
                            selectedCategories + setOf(category)
                        }
                    onCategoryClick(selectedCategories.toList())
                },
            )
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    FilterChip(
        selected = selected,
        onClick = {
            onClick()
        },
        modifier = modifier,
        label = {
            Text(
                text = text,
                style = FestabookTypography.bodyLarge,
            )
        },
        shape = festabookShapes.radiusFull,
        colors =
            FilterChipDefaults.filterChipColors(
                containerColor = FestabookColor.white,
                selectedContainerColor = FestabookColor.gray200,
                labelColor = FestabookColor.black,
                selectedLabelColor = FestabookColor.black,
            ),
        border =
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = FestabookColor.gray200,
                selectedBorderColor = FestabookColor.black,
                borderWidth = 2.dp,
                selectedBorderWidth = 2.dp,
            ),
        leadingIcon = icon,
    )
}

@Composable
@Preview(showBackground = true)
private fun CategoryChipPreview() {
    FestabookTheme {
        CategoryChip("전체")
    }
}

@Composable
@Preview(showBackground = true)
private fun PlaceCategoryScreenPreview() {
    FestabookTheme {
        PlaceCategoryScreen()
    }
}
