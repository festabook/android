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
import com.daedan.festabook.presentation.theme.FestabookTheme
import com.daedan.festabook.presentation.theme.festabookShapes
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun ScheduleEventCard(
    scheduleEvent: ScheduleEventUiModel,
    modifier: Modifier = Modifier,
) {
    val scheduleEventCardColors = scheduleEventCardColors(scheduleEvent.status)

    Column(
        modifier =
            modifier
                .cardBackground(
                    backgroundColor = MaterialTheme.colorScheme.background,
                    borderColor = scheduleEventCardColors.cardBorderColor,
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
                color = scheduleEventCardColors.titleColor,
                modifier = Modifier.weight(1f),
            )
            ScheduleEventLabel(scheduleEvent.status)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(festabookSpacing.paddingBody1),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_clock),
                contentDescription = stringResource(R.string.content_description_iv_location),
                tint = scheduleEventCardColors.contentColor,
            )
            Text(
                text =
                    stringResource(
                        R.string.format_date,
                        scheduleEvent.startTime,
                        scheduleEvent.endTime,
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = scheduleEventCardColors.contentColor,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(festabookSpacing.paddingBody1),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location),
                contentDescription = stringResource(R.string.content_description_iv_location),
                tint = scheduleEventCardColors.contentColor,
            )
            Text(
                text = scheduleEvent.location,
                style = MaterialTheme.typography.bodySmall,
                color = scheduleEventCardColors.contentColor,
            )
        }
    }
}

@Composable
private fun ScheduleEventLabel(scheduleEventUiStatus: ScheduleEventUiStatus) {
    val scheduleEventCardProps = scheduleEventCardColors(scheduleEventUiStatus)
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
            text = scheduleLabelText(scheduleEventUiStatus),
            style = MaterialTheme.typography.bodySmall,
            color = scheduleEventCardProps.labelTextColor,
        )
    }
}

@Composable
private fun scheduleLabelText(status: ScheduleEventUiStatus): String =
    when (status) {
        ScheduleEventUiStatus.UPCOMING -> stringResource(R.string.schedule_status_upcoming)
        ScheduleEventUiStatus.ONGOING -> stringResource(R.string.schedule_status_ongoing)
        ScheduleEventUiStatus.COMPLETED -> stringResource(R.string.schedule_status_completed)
    }

private fun scheduleEventCardColors(status: ScheduleEventUiStatus): ScheduleEventCardProps =
    when (status) {
        ScheduleEventUiStatus.UPCOMING -> ScheduleEventCardColors.upcoming
        ScheduleEventUiStatus.ONGOING -> ScheduleEventCardColors.ongoing
        ScheduleEventUiStatus.COMPLETED -> ScheduleEventCardColors.completed
    }

@Composable
@Preview(showBackground = true)
private fun OnGoingScheduleEventCardPreview() {
    FestabookTheme {
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
}

@Composable
@Preview(showBackground = true)
private fun UpComingScheduleEventCardPreview() {
    FestabookTheme {
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
}

@Composable
@Preview(showBackground = true)
private fun CompleteScheduleEventCardONGOINGPreview() {
    FestabookTheme {
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
}

object ScheduleEventCardColors {
    val upcoming =
        ScheduleEventCardProps(
            cardBorderColor = FestabookColor.accentGreen,
            titleColor = FestabookColor.black,
            contentColor = FestabookColor.gray500,
            labelTextColor = FestabookColor.black,
            labelBackgroundColor = FestabookColor.white,
            labelBorderColor = FestabookColor.black,
        )

    val ongoing =
        ScheduleEventCardProps(
            cardBorderColor = FestabookColor.accentBlue,
            titleColor = FestabookColor.black,
            contentColor = FestabookColor.gray500,
            labelTextColor = FestabookColor.white,
            labelBackgroundColor = FestabookColor.black,
            labelBorderColor = FestabookColor.black,
        )

    val completed =
        ScheduleEventCardProps(
            cardBorderColor = FestabookColor.gray400,
            titleColor = FestabookColor.gray400,
            contentColor = FestabookColor.gray400,
            labelTextColor = FestabookColor.gray400,
            labelBackgroundColor = FestabookColor.white,
            labelBorderColor = FestabookColor.white,
        )
}

data class ScheduleEventCardProps(
    val cardBorderColor: Color,
    val titleColor: Color,
    val contentColor: Color,
    val labelTextColor: Color,
    val labelBackgroundColor: Color,
    val labelBorderColor: Color,
)
