package com.daedan.festabook.presentation.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.R
import com.daedan.festabook.presentation.home.LineUpItemOfDayUiModel
import com.daedan.festabook.presentation.home.LineupItemUiModel
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTypography
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun HomeLineupItem(
    uiModel: LineUpItemOfDayUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // 날짜 + 배지 영역
        Column(
            modifier = Modifier.padding(horizontal = 16.dp).width(IntrinsicSize.Max)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "${uiModel.date.monthValue}.${uiModel.date.dayOfMonth}",
                    style = FestabookTypography.titleLarge,
                    color = FestabookColor.black,
                )

                if (uiModel.isDDay) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(FestabookColor.black)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.home_is_d_day),
                            style = FestabookTypography.labelSmall,
                            color = FestabookColor.white,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = FestabookColor.gray700,
                modifier = Modifier.fillMaxWidth(),
            )

        }



        Spacer(modifier = Modifier.height(8.dp))

        // 아티스트 가로 리스트
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(uiModel.lineupItems) { item ->
                HomeArtistItem(
                    artistName = item.name,
                    artistImageUrl = item.imageUrl,
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeLineupItemPreview() {
    HomeLineupItem(
        uiModel =
            LineUpItemOfDayUiModel(
                id = 1L,
                date = LocalDate.now(),
                isDDay = true,
                lineupItems =
                    listOf(
                        LineupItemUiModel(
                            id = 1,
                            name = "실리카겔",
                            imageUrl = "sample",
                            performanceAt = LocalDateTime.now(),
                        ),
                        LineupItemUiModel(
                            id = 2,
                            name = "한로로",
                            imageUrl = "sample",
                            performanceAt = LocalDateTime.now(),
                        ),
                        LineupItemUiModel(
                            id = 3,
                            name = "실리카겔",
                            imageUrl = "sample",
                            performanceAt = LocalDateTime.now(),
                        ),
                        LineupItemUiModel(
                            id = 4,
                            name = "한로로",
                            imageUrl = "sample",
                            performanceAt = LocalDateTime.now(),
                        ),
                        LineupItemUiModel(
                            id = 5,
                            name = "실리카겔",
                            imageUrl = "sample",
                            performanceAt = LocalDateTime.now(),
                        ),
                        LineupItemUiModel(
                            id = 6,
                            name = "한로로",
                            imageUrl = "sample",
                            performanceAt = LocalDateTime.now(),
                        ),
                    ),
            ),
    )
}
