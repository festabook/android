package com.daedan.festabook.presentation.placeMap.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.daedan.festabook.R
import com.daedan.festabook.domain.model.TimeTag
import com.daedan.festabook.presentation.common.component.cardBackground
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.festabookShapes
import com.daedan.festabook.presentation.theme.festabookSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTagMenu(
    title: String,
    timeTags: List<TimeTag>,
    modifier: Modifier = Modifier,
    onTimeTagClick: (TimeTag) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    var dropdownWidth by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        ExposedDropdownMenuBox(
            modifier =
                Modifier
                    .wrapContentSize()
                    .background(Color.Transparent),
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            TimeTagButton(
                title = title,
                onSizeDetermine = { dropdownWidth = it },
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = DpOffset(x = 0.dp, y = festabookSpacing.paddingBody2),
                modifier =
                    Modifier
                        .width(
                            with(density) { dropdownWidth.width.toDp() },
                        ).cardBackground(
                            backgroundColor = FestabookColor.white,
                            borderStroke = 2.dp,
                            borderColor = FestabookColor.gray300,
                            shape = festabookShapes.radius2,
                        ),
                shape = festabookShapes.radius2,
            ) {
                timeTags.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        onClick = {
                            scope.launch {
                                onTimeTagClick(item)
                                waitForRipple {
                                    expanded = false
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ExposedDropdownMenuBoxScope.TimeTagButton(
    title: String,
    onSizeDetermine: (IntSize) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .width(140.dp)
                .onGloballyPositioned { coordinates ->
                    onSizeDetermine(coordinates.size)
                }.menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable,
                    enabled = true,
                ).height(TopAppBarDefaults.MediumAppBarCollapsedHeight) // Festabook TopAppbar Size
                .background(Color.Transparent)
                .clickable(
                    onClick = {},
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_down),
            contentDescription = stringResource(R.string.chevron_down),
        )
    }
}

private suspend inline fun waitForRipple(
    timeMillis: Long = 100,
    after: () -> Unit = {},
) {
    delay(timeMillis)
    after()
}

@Composable
@Preview(showBackground = true)
private fun TimeTagMenuPreview() {
    val timeTags =
        listOf(
            TimeTag(1, "1일차 오전"),
            TimeTag(2, "오후"),
        )
    var title by remember { mutableStateOf("1일차 오전") }
    FestabookTheme {
        TimeTagMenu(
            title = title,
            timeTags = timeTags,
            modifier =
                Modifier
                    .background(FestabookColor.white)
                    .padding(horizontal = festabookSpacing.paddingScreenGutter),
            // Festabook Gutter
            onTimeTagClick = { },
        )
    }
}
