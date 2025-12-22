package com.daedan.festabook.presentation.home.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.presentation.common.component.CoilImage
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTypography

@Composable
fun HomeArtistItem(
    artistName: String,
    artistImageUrl: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(68.dp),
    ) {
        CoilImage(
            url = artistImageUrl,
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(HomeArtistItem.ArtistImage)
                    .border(1.dp, FestabookColor.gray300, HomeArtistItem.ArtistImage),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = artistName,
            style = FestabookTypography.labelLarge,
            color = FestabookColor.gray700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

object HomeArtistItem {
    val ArtistImage = RoundedCornerShape(
        topStartPercent = 50,
        topEndPercent = 50,
        bottomEndPercent = 50,
        bottomStartPercent = 5,
    )
}

@Preview
@Composable
private fun HomeArtistItemPreview() {
    HomeArtistItem(
        artistName = "실리카겔",
        artistImageUrl = "sample",
    )
}