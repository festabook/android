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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daedan.festabook.R
import com.daedan.festabook.presentation.common.component.cardBackground
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiModel
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiStatus
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.festabookShapes
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun ScheduleEventCard(
    scheduleEvent: ScheduleEventUiModel,
    modifier: Modifier = Modifier,
) {
    val scheduleEventCardProps =
        when (scheduleEvent.status) {
            ScheduleEventUiStatus.UPCOMING -> {
                ScheduleEventCardProps(
                    cardBorderColor = FestabookColor.accentGreen,
                    titleColor = FestabookColor.black,
                    contentColor = FestabookColor.gray500,
                    labelText = stringResource(R.string.schedule_status_upcoming),
                    labelTextColor = FestabookColor.black,
                    labelBackgroundColor = FestabookColor.white,
                    labelBorderColor = FestabookColor.black,
                )
            }

            ScheduleEventUiStatus.ONGOING -> {
                ScheduleEventCardProps(
                    cardBorderColor = FestabookColor.accentBlue,
                    titleColor = FestabookColor.black,
                    contentColor = FestabookColor.gray500,
                    labelText = stringResource(R.string.schedule_status_ongoing),
                    labelTextColor = FestabookColor.white,
                    labelBackgroundColor = FestabookColor.black,
                    labelBorderColor = FestabookColor.black,
                )
            }

            ScheduleEventUiStatus.COMPLETED -> {
                ScheduleEventCardProps(
                    cardBorderColor = FestabookColor.gray400,
                    titleColor = FestabookColor.gray400,
                    contentColor = FestabookColor.gray400,
                    labelText = stringResource(R.string.schedule_status_completed),
                    labelTextColor = FestabookColor.gray400,
                    labelBackgroundColor = FestabookColor.white,
                    labelBorderColor = FestabookColor.white,
                )
            }
        }

    Column(
        modifier =
            modifier
                .cardBackground(
                    borderColor = scheduleEventCardProps.cardBorderColor,
                    shape = festabookShapes.radius2,
                ).padding(festabookSpacing.paddingBody4),
        verticalArrangement = Arrangement.spacedBy(festabookSpacing.paddingBody1),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(festabookSpacing.paddingBody1),
        ) {
            Text(
                text = scheduleEvent.title,
                style = MaterialTheme.typography.titleLarge,
                color = scheduleEventCardProps.titleColor,
                modifier = Modifier.weight(1f),
            )
            ScheduleEventLabel(scheduleEventCardProps)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(festabookSpacing.paddingBody1),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_clock),
                contentDescription = stringResource(R.string.content_description_iv_location),
                tint = scheduleEventCardProps.contentColor,
            )
            Text(
                text =
                    stringResource(
                        R.string.format_date,
                        scheduleEvent.startTime,
                        scheduleEvent.endTime,
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = scheduleEventCardProps.contentColor,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(festabookSpacing.paddingBody1),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location),
                contentDescription = stringResource(R.string.content_description_iv_location),
                tint = scheduleEventCardProps.contentColor,
            )
            Text(
                text = scheduleEvent.location,
                style = MaterialTheme.typography.bodySmall,
                color = scheduleEventCardProps.contentColor,
            )
        }
    }
}

@Composable
private fun ScheduleEventLabel(scheduleEventCardProps: ScheduleEventCardProps) {
    Box(
        modifier =
            Modifier
                .size(48.dp, 24.dp)
                .cardBackground(
                    backgroundColor = scheduleEventCardProps.labelBackgroundColor,
                    borderColor = scheduleEventCardProps.labelBorderColor,
                    shape = festabookShapes.radius1,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = scheduleEventCardProps.labelText,
            style = MaterialTheme.typography.bodySmall,
            color = scheduleEventCardProps.labelTextColor,
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun OnGoingScheduleEventCardPreview() {
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

@Composable
@Preview(showBackground = true)
private fun UpComingScheduleEventCardPreview() {
    ScheduleEventCard(
        scheduleEvent =
            ScheduleEventUiModel(
                id = 1,
                status = ScheduleEventUiStatus.UPCOMING,
                startTime = "09:00",
                endTime = "18:00",
                title = "동아리 버스킹 공연",
                location = "운동장",
            ),
    )
}

@Composable
@Preview(showBackground = true)
private fun CompleteScheduleEventCardONGOINGPreview() {
    ScheduleEventCard(
        scheduleEvent =
            ScheduleEventUiModel(
                id = 1,
                status = ScheduleEventUiStatus.COMPLETED,
                startTime = "09:00",
                endTime = "18:00",
                title = "동아리 버스킹 공연",
                location = "운동장",
            ),
    )
}

data class ScheduleEventCardProps(
    val cardBorderColor: Color,
    val titleColor: Color,
    val contentColor: Color,
    val labelText: String,
    val labelTextColor: Color,
    val labelBackgroundColor: Color,
    val labelBorderColor: Color,
)
