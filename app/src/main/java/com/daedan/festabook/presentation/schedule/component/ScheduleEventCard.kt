package com.daedan.festabook.presentation.schedule.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.R
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiModel
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiStatus
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun ScheduleEventCard(
    scheduleEvent: ScheduleEventUiModel,
    modifier: Modifier = Modifier,
) {
    val scheduleEventText =
        when (scheduleEvent.status) {
            ScheduleEventUiStatus.UPCOMING -> stringResource(R.string.schedule_status_upcoming)
            ScheduleEventUiStatus.ONGOING -> stringResource(R.string.schedule_status_ongoing)
            ScheduleEventUiStatus.COMPLETED -> stringResource(R.string.schedule_status_completed)
        }

    Column(
        modifier = modifier.padding(festabookSpacing.paddingBody4),
        verticalArrangement = Arrangement.spacedBy(festabookSpacing.paddingBody1),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(festabookSpacing.paddingBody1),
        ) {
            Text(
                text = scheduleEvent.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier.size(48.dp, 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = scheduleEventText,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(festabookSpacing.paddingBody1),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_clock),
                contentDescription = stringResource(R.string.content_description_iv_location),
            )
            Text(
                text =
                    stringResource(
                        R.string.format_date,
                        scheduleEvent.startTime,
                        scheduleEvent.endTime,
                    ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(festabookSpacing.paddingBody1),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location),
                contentDescription = stringResource(R.string.content_description_iv_location),
            )
            Text(text = scheduleEventText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ScheduleEventCardPreview() {
    ScheduleEventCard(
        scheduleEvent =
            ScheduleEventUiModel(
                id = 1,
                status = ScheduleEventUiStatus.ONGOING,
                startTime = "09:00",
                endTime = "18:00",
                title = "동아리 버스킹 공연",
                location = "운동장",
            ),
    )
}
