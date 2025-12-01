package com.daedan.festabook.presentation.common.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.daedan.festabook.presentation.theme.FestabookTypography
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun Header(
    title: String,
    modifier: Modifier = Modifier,
    style: TextStyle = FestabookTypography.displayLarge,
) {
    Text(
        text = title,
        style = style,
        modifier =
            modifier
                .padding(
                    top = festabookSpacing.paddingTitleHorizontal,
                    bottom = festabookSpacing.paddingBody4,
                    start = festabookSpacing.paddingScreenGutter,
                    end = festabookSpacing.paddingScreenGutter,
                ).fillMaxWidth(),
    )
}

@Composable
@Preview(showBackground = true)
private fun HeaderPreview() {
    Header(title = "FestaBook")
}
