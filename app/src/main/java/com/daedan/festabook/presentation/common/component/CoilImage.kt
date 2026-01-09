package com.daedan.festabook.presentation.common.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.daedan.festabook.presentation.common.convertImageUrl

@Composable
fun CoilImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    builder: ImageRequest.Builder.() -> Unit = {},
) {
    AsyncImage(
        model =
            ImageRequest
                .Builder(LocalPlatformContext.current)
                .apply(builder)
                .data(url.convertImageUrl())
                .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        placeholder = ColorPainter(Color.LightGray),
        modifier = modifier,
    )
}

@Composable
@Preview
private fun CoilImagePreview() {
    CoilImage(
        url = "",
        contentDescription = "",
    )
}
