package com.daedan.festabook.presentation.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.R
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.FestabookTypography

@Composable
fun HomeHeader(
    schoolName: String,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.clickable { onExpandClick() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = schoolName,
                style = FestabookTypography.displayLarge,
                color = FestabookColor.black,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_dropdown),
                contentDescription = stringResource(id = R.string.home_navigate_to_explore_desc),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeHeaderPreview() {
    HomeHeader(
        schoolName = "가천대학교",
        onExpandClick = {},
    )
}
