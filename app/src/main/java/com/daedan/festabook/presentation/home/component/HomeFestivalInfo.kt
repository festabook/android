package com.daedan.festabook.presentation.home.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTypography

@Composable
fun HomeFestivalInfo(
    festivalName: String,
    festivalDate: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = festivalName,
            style = FestabookTypography.displayMedium,
            color = FestabookColor.black,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = festivalDate,
            style = FestabookTypography.bodyLarge,
            color = FestabookColor.gray500,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeFestivalInfoPreview() {
    HomeFestivalInfo(
        festivalName = "2025 가천 Water Festival\n: AQUA WAVE",
        festivalDate = "2025년 10월 15일 - 10월 17일",
    )
}
