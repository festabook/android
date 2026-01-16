package com.daedan.festabook.presentation.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.R
import com.daedan.festabook.presentation.main.FestabookMainTab
import com.daedan.festabook.presentation.main.FestabookMainTab.Defaults.selectedColor
import com.daedan.festabook.presentation.main.FestabookMainTab.Defaults.unselectedColor
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.FestabookTypography
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun FestabookBottomNavigationBar(
    currentTab: FestabookMainTab?,
    onTabSelect: (FestabookMainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier,
    ) {
        Row(
            modifier =
                Modifier
                    .background(color = FestabookColor.white)
                    .fillMaxWidth()
                    .height(70.dp),
        ) {
            FestabookMainTab.entries.forEach { item ->
                when (item) {
                    FestabookMainTab.PLACE_MAP -> Spacer(modifier = Modifier.weight(1f))
                    else -> {
                        FestabookNavigationItem(
                            tab = item,
                            selected = item == currentTab,
                            onClick = onTabSelect,
                        )
                    }
                }
            }
        }
        PlaceMapNavigationItem(onClick = onTabSelect)
    }
}

@Composable
private fun RowScope.FestabookNavigationItem(
    onClick: (FestabookMainTab) -> Unit,
    tab: FestabookMainTab,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .weight(1f)
                .fillMaxHeight()
                .background(color = FestabookColor.white)
                .selectable(
                    selected = selected,
                    onClick = { onClick(tab) },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // 리플(Ripple) 완벽 제거
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(id = tab.iconResId),
                contentDescription = tab.contentDescription,
                tint = if (selected) selectedColor else unselectedColor,
            )
            Spacer(modifier = Modifier.height(festabookSpacing.paddingBody1))
            Text(
                text = stringResource(tab.labelResId),
                style = FestabookTypography.labelMedium,
                color = if (selected) selectedColor else unselectedColor,
            )
        }
    }
}

@Composable
private fun PlaceMapNavigationItem(
    onClick: (FestabookMainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier =
            modifier
                .offset(y = -festabookSpacing.paddingBody4)
                .clickable(
                    onClick = {
                        onClick(FestabookMainTab.PLACE_MAP)
                    },
                    indication = null,
                    interactionSource = null,
                ),
        painter = painterResource(id = R.drawable.btn_fab_manu),
        contentDescription = null,
    )
}

@Preview(showBackground = true)
@Composable
private fun FestabookBottomNavigationBarPreview() {
    FestabookTheme {
        var currentTabState by remember { mutableStateOf(FestabookMainTab.HOME) }
        Scaffold(
            bottomBar = {
                FestabookBottomNavigationBar(
                    currentTab = currentTabState,
                    onTabSelect = { currentTabState = it },
                )
            },
        ) { it }
    }
}
