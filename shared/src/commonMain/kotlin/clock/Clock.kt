package clock

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ComposeClock() {
    Box(modifier = Modifier.fillMaxSize()) {
        val clockConfig = ClockConfig(Random)
        ClockBackground(clockConfig)

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            repeat(1000) {
                ParticleHeartBeat(
                    clockConfig,
                    ParticleObject.Type.Background
                )
            }

            repeat(100) {
                ParticleHeartBeat(
                    clockConfig,
                    ParticleObject.Type.Hour
                )
            }

            repeat(100) {
                ParticleHeartBeat(
                    clockConfig,
                    ParticleObject.Type.Minute
                )
            }

            ClockBackgroundBorder(clockConfig)
            ClockMinuteCircles(clockConfig)
            ClockSecondHand(clockConfig)
        }
    }
}

@Composable
private fun ClockBackground(clockConfig: ClockConfig) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val parentSize = this.size
        drawRect(
            size = parentSize,
            color = clockConfig.colorPalette.backgroundColor
        )
    }

}

@Composable
private fun ClockSecondHand(clockConfig: ClockConfig) {
    val infiniteTransition = rememberInfiniteTransition()
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000
            ),
            repeatMode = RepeatMode.Restart
        )
    )
    Canvas(Modifier.fillMaxSize()) {
        val parentSize = this.size
        val clockRadius =
            0.9f * min((parentSize.width / 2), (parentSize.height / 2))
        val paint = Paint().apply {
            style = PaintingStyle.Fill
            color = clockConfig.colorPalette.handleColor
        }
        val centerX = (parentSize.width / 2)
        val centerY = (parentSize.height / 2)
        val oneMinuteRadians = PI / 30

        val currentSecondInMillisecond = Clock.System.now().toEpochMilliseconds() % 1000
        val progression = (currentSecondInMillisecond / 1000.0)
        val animatedSecond =
            Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).second + progression
        val degree = -PI / 2 + (animatedSecond * oneMinuteRadians)
        val x = centerX + cos(degree) * clockRadius
        val y = centerY + sin(degree) * clockRadius

        paint.style = PaintingStyle.Fill
        val radius = 8f

        drawCircle(
            color = clockConfig.colorPalette.handleColor,
            radius = radius,
            center = Offset(x.toFloat(), y.toFloat()),
            style = Fill
        )
    }
}

@Composable
private fun ClockMinuteCircles(clockConfig: ClockConfig) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val parentSize = this.size
        val clockRadius = 0.95f * min((parentSize.width / 2), (parentSize.height / 2))

        val centerX = (parentSize.width / 2)
        val centerY = (parentSize.height / 2)
        val oneMinuteRadians = PI / 30
        0.rangeTo(59).forEach { minute ->
            val isHour = minute % 5 == 0
            val degree = -PI / 2 + (minute * oneMinuteRadians)
            val x = centerX + cos(degree) * clockRadius
            val y = centerY + sin(degree) * clockRadius
            var radius: Float
            if (isHour) {
                radius = 12f
            } else {
                radius = 6f
            }
            drawCircle(
                color = clockConfig.colorPalette.handleColor,
                radius = radius,
                center = Offset(x.toFloat(), y.toFloat()),
                style = if (isHour) Fill else Stroke()
            )
        }
    }
}

@Composable
private fun ClockBackgroundBorder(clockConfig: ClockConfig) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val parentSize = this.size
        val radius = min((parentSize.width / 2), (parentSize.height / 2)) * 0.9f
        drawCircle(
            center = Offset((parentSize.width / 2), (parentSize.height / 2)),
            radius = radius,
            color = clockConfig.colorPalette.borderColor,
            style = Stroke(
                width = 10f,
            )
        )
    }
}