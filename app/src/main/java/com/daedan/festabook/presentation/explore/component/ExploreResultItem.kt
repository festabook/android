package com.daedan.festabook.presentation.explore.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.presentation.explore.model.SearchResultUiModel
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.FestabookTypography

@Composable
fun ExploreResultItem(
    university: SearchResultUiModel,
    onItemClick: (SearchResultUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onItemClick(university) }
                .padding(vertical = 8.dp, horizontal = 4.dp),
    ) {
        Text(
            text = university.universityName,
            style = FestabookTypography.bodyLarge,
            color = FestabookColor.gray800,
        )
        Text(
            text = university.festivalName,
            style = FestabookTypography.bodySmall,
            color = FestabookColor.gray600,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreResultItemPreview() {
    FestabookTheme {
        ExploreResultItem(
            university = SearchResultUiModel(1, "서울시립대학교", "2024 대동제"),
            onItemClick = {},
        )
    }
}
