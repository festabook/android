package com.daedan.festabook.presentation.common.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.request.ImageRequest
import com.daedan.festabook.BuildConfig
import com.daedan.festabook.presentation.theme.FestabookColor
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import com.skydoves.landscapist.zoomable.ZoomablePlugin
import com.skydoves.landscapist.zoomable.rememberZoomableState
import com.daedan.festabook.R

@Composable
fun FestabookImage(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    isZoomable: Boolean = false,
    enablePopUp: Boolean = false,
    builder: ImageRequest.Builder.() -> Unit = {},
) {
    val context = LocalContext.current
    val zoomableState = rememberZoomableState()
    val convertedUrl = imageUrl.convertImageUrl()

    var isPopUpOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.then(
            if (enablePopUp) Modifier.clickable { isPopUpOpen = true }
            else Modifier
        )
    ) {
        CoilImage(
            imageRequest = {
                ImageRequest.Builder(context)
                    .data(convertedUrl)
                    .apply(builder)
                    .build()

            },
            modifier = modifier.fillMaxSize(),
            imageOptions = ImageOptions(
                contentScale = contentScale,
                alignment = Alignment.Center,
                contentDescription = contentDescription
            ),
            component = rememberImageComponent {
                +ShimmerPlugin(
                    Shimmer.Flash(
                        baseColor = FestabookColor.gray100.copy(alpha = 0.5f),
                        highlightColor = FestabookColor.gray200.copy(alpha = 0.3f)),
                )
                if (isZoomable) {
                    +ZoomablePlugin(state = zoomableState)
                }
            },
            failure = {
                Image(
                    painter = painterResource(id = R.drawable.img_fallback),
                    contentDescription = "fallback_image",
                    modifier = Modifier.align(Alignment.Center),
                    contentScale = contentScale
                )
            }
        )
    }
    if (isPopUpOpen && enablePopUp) {
        FestabookImageZoomPopup(
            imageUrl = imageUrl,
            onDismiss = { isPopUpOpen = false }
        )
    }
}

@Composable
private fun FestabookImageZoomPopup(
    imageUrl: String?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FestabookColor.black.copy(alpha = 0.8f))
        ) {
            FestabookImage(
                imageUrl = imageUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                isZoomable = true,
                enablePopUp = false
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "close the popup",
                    tint = FestabookColor.white,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FestabookImageTestPreview() {
    FestabookImage(
        imageUrl = ""
    )
}
@Preview(showBackground = true)
@Composable
fun DiaplogPreview() {
    FestabookImageZoomPopup(
        imageUrl = ""
    ) { }
}

fun String?.convertImageUrl() = if (this != null && this.startsWith("/images/")) {
    BuildConfig.FESTABOOK_URL.removeSuffix("/api/") + this
} else {
    this
}