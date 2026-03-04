package com.daedan.festabook.presentation.explore.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.R
import com.daedan.festabook.presentation.theme.FestabookTheme

@Composable
fun ExploreBackHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        IconButton(
            onClick = onBackClick,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.explore_back),
                tint = Color.Unspecified,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreBackHeaderPreview() {
    FestabookTheme {
        ExploreBackHeader(onBackClick = {})
    }
}
