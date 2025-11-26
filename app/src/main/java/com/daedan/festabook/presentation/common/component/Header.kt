package com.daedan.festabook.presentation.common.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.presentation.theme.FestabookTypography

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
                .padding(top = 40.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
    )
}

@Composable
@Preview(showBackground = true)
private fun HeaderPreview() {
    Header(title = "FestaBook")
}
