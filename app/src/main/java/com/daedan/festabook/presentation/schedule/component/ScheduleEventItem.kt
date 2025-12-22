package com.daedan.festabook.presentation.schedule.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiModel
import com.daedan.festabook.presentation.schedule.model.ScheduleEventUiStatus
import com.daedan.festabook.presentation.theme.FestabookColor
import com.daedan.festabook.presentation.theme.festabookSpacing

@Composable
fun ScheduleEventItem(
    composition: LottieComposition?,
    progress: Float,
    scheduleEvent: ScheduleEventUiModel,
    modifier: Modifier = Modifier,
) {
    val props = lottieTimeLineCircleProps(scheduleEvent.status)
    val dynamicProperties = rememberScheduleEventDynamicProperties(props)

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        LottieAnimation(
            composition = composition,
            progress = { if (scheduleEvent.status == ScheduleEventUiStatus.COMPLETED) 0f else progress },
            modifier = Modifier.size(festabookSpacing.paddingBody4 * 4),
            dynamicProperties = dynamicProperties,
        )

        ScheduleEventCard(scheduleEvent = scheduleEvent)
    }
}

@Composable
@Preview
private fun ScheduleEventItemPreview() {
    ScheduleEventItem(
        composition = null,
        progress = 1f,
        scheduleEvent =
            ScheduleEventUiModel(
                id = 1,
                status = ScheduleEventUiStatus.ONGOING,
                startTime = "9:00",
                endTime = "18:00",
                title = "동아리 버스킹 공연",
                location = "운동장",
            ),
    )
}

@Composable
private fun rememberScheduleEventDynamicProperties(props: LottieTimeLineCircleProps) =
    rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = props.centerColor.toArgb(),
            *props.centerKeyPath.toTypedArray(),
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.OPACITY,
            value = (props.outerOpacity * 100).toInt(),
            *props.outerKeyPath.toTypedArray(),
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.OPACITY,
            value = (props.innerOpacity * 100).toInt(),
            *props.innerKeyPath.toTypedArray(),
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = props.outerColor.toArgb(),
            *props.outerKeyPath.toTypedArray(),
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = props.innerColor.toArgb(),
            *props.innerKeyPath.toTypedArray(),
        ),
    )

private fun lottieTimeLineCircleProps(status: ScheduleEventUiStatus): LottieTimeLineCircleProps =
    when (status) {
        ScheduleEventUiStatus.UPCOMING -> LottieTimeLineCircleAnimations.upcoming
        ScheduleEventUiStatus.ONGOING -> LottieTimeLineCircleAnimations.ongoing
        ScheduleEventUiStatus.COMPLETED -> LottieTimeLineCircleAnimations.completed
    }

object LottieTimeLineCircleAnimations {
    val upcoming =
        LottieTimeLineCircleProps(
            centerColor = FestabookColor.accentGreen,
            outerOpacity = 0f,
            innerOpacity = 1f,
            outerColor = FestabookColor.accentGreen,
            innerColor = FestabookColor.accentGreen,
        )
    val ongoing =
        LottieTimeLineCircleProps(
            centerColor = FestabookColor.accentBlue,
            outerOpacity = 1f,
            innerOpacity = 1f,
            outerColor = FestabookColor.accentBlue,
            innerColor = FestabookColor.accentBlue,
        )
    val completed =
        LottieTimeLineCircleProps(
            centerColor = FestabookColor.gray300,
            outerOpacity = 0f,
            innerOpacity = 0f,
            outerColor = FestabookColor.gray300,
            innerColor = FestabookColor.gray300,
        )
}

data class LottieTimeLineCircleProps(
    val centerColor: Color,
    val outerOpacity: Float,
    val innerOpacity: Float,
    val outerColor: Color,
    val innerColor: Color,
    val centerKeyPath: List<String> = listOf("centerCircle", "**", "Fill 1"),
    val outerKeyPath: List<String> = listOf("outerWave", "**", "Fill 1"),
    val innerKeyPath: List<String> = listOf("innerWave", "**", "Fill 1"),
)
