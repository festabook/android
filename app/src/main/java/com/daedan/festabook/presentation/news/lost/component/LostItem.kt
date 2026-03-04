package com.daedan.festabook.presentation.news.lost.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.CoilImage
import com.daedan.festabook.presentation.common.component.cardBackground
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.festabookShapes

@Composable
fun LostItem(
    url: String,
    modifier: Modifier = Modifier,
    onLostItemClick: () -> Unit = {},
) {
    Card(
        shape = festabookShapes.radius3,
        modifier =
            modifier
                .cardBackground()
                .aspectRatio(1f)
                .clickable(indication = null, interactionSource = null) { onLostItemClick() },
    ) {
        CoilImage(
            url = url,
            contentDescription = stringResource(R.string.lost_item),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
@Preview
private fun LostItemPreview() {
    FestabookTheme {
        LostItem(
            url = "https://i.imgur.com/Zblctu7.png",
            onLostItemClick = { },
        )
    }
}
