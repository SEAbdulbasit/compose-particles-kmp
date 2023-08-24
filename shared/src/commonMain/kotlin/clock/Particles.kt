package clock

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ParticleHeartBeat(
    clockConfig: ClockConfig,
    type: ParticleObject.Type
) {

    val particleObject = remember { mutableStateOf<ParticleObject?>(null) }
    val paint = Paint()

    val cubicBezier = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
    val infiniteTransition = rememberInfiniteTransition()

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = cubicBezier
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val drawScope = this
        val size = this.size
        if (particleObject.value == null) {
            particleObject.value = ParticleObject(type, clockConfig)
        }
        particleObject.value?.apply {
            drawScope.drawIntoCanvas {
                drawOnCanvas(paint, it)
            }
            val progressValue = 1 - progress
            animate(
                progressValue,
                size
            )
        }
    }

}


private fun ParticleObject.animate(
    progress: Float,
    size: Size
) {

    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = min(centerX, centerY)
    val random = Random
    val modifier = max(0.2f, progress) * 4f
    val xUpdate = modifier * cos(animationParams.currentAngle)
    val yUpdate = modifier * sin(animationParams.currentAngle)
    val newX = animationParams.locationX.value + xUpdate
    val newY = animationParams.locationY.value + yUpdate

    val positionInsideCircle =
        hypot(newY - centerY, newX - centerX)
    val currentPositionIsInsideCircle = positionInsideCircle < radius * type.maxLengthModifier
    val currentLengthByRadius = positionInsideCircle / (radius * type.maxLengthModifier)
    when {
        currentLengthByRadius - type.minLengthModifier <= 0f -> {
            animationParams.alpha = 0f
        }

        animationParams.alpha == 0f -> {
            animationParams.alpha = random.nextFloat()

        }

        else -> {
            val fadeOutRange = this.type.maxLengthModifier
            animationParams.alpha =
                (if (currentLengthByRadius < fadeOutRange) animationParams.alpha else ((1f - currentLengthByRadius) / (1f - fadeOutRange))).coerceIn(
                    0f,
                    1f
                )
        }
    }
    if (!currentPositionIsInsideCircle) {
        randomize(random, size)
        animationParams.alpha = 0f
    } else {
        animationParams.locationX = Dp(newX)
        animationParams.locationY = Dp(newY)
    }
}

private fun ParticleObject.drawOnCanvas(paint: Paint, canvas: Canvas) {
    canvas.apply {
        paint.color = animationParams.currentColor
        //paint.alpha = animationParams.alpha
        val centerW = animationParams.locationX.value
        val centerH = animationParams.locationY.value
        if (animationParams.isFilled) {
            paint.style = PaintingStyle.Fill
        } else {
            paint.style = PaintingStyle.Stroke

        }
        drawCircle(
            center = Offset(centerW, centerH),
            radius = animationParams.particleSize.value / 2f,
            paint = paint
        )
    }
}


private fun ParticleObject.randomize(
    random: Random,
    pxSize: Size
) {
    val calendar = Clock.System.now()
    val today: LocalDateTime = calendar.toLocalDateTime(TimeZone.currentSystemDefault())

    val currentMinuteCount = today.minute
    val currentHour = ((today.hour) % 24).toDouble() / 12.0
    val currentMinute = (currentMinuteCount).toDouble() / 60.0
    val currentMinuteRadians = (PI / -2.0) + currentMinute * 2.0 * PI
    val oneHourRadian = (currentMinute * 2.0 * PI) / 12.0
    val currentHourRadians =
        (PI / -2.0) + (currentHour) * 2.0 * PI
    val currentHourMinuteRadians = (oneHourRadian * currentMinute) + currentHourRadians
    val randomAngleOffset =
        randomFloat(type.startAngleOffsetRadians, type.endAngleOffsetRadians, random)
    val randomizedAngle = when (type) {
        ParticleObject.Type.Hour -> currentHourMinuteRadians.toFloat()
        ParticleObject.Type.Minute -> currentMinuteRadians.toFloat()
        ParticleObject.Type.Background -> (currentHourMinuteRadians + randomAngleOffset).toFloat()
    }
    val centerX = (pxSize.width / 2) + randomFloat(-10f, 10f, random)
    val centerY = (pxSize.height / 2) + randomFloat(-10f, 10f, random)
    val radius = min(centerX, centerY)
    val randomLength =
        randomFloat(type.minLengthModifier * radius, this.type.maxLengthModifier * radius, random)
    val x = randomLength * cos(randomizedAngle)
    val y = randomLength * sin(randomizedAngle)
    val color = when (type) {
        ParticleObject.Type.Background -> clockConfig.colorPalette.mainColors.random()
        ParticleObject.Type.Hour -> clockConfig.colorPalette.handleColor
        ParticleObject.Type.Minute -> clockConfig.colorPalette.handleColor
    }
    animationParams = ParticleObject.AnimationParams(
        isFilled = clockConfig.random.nextFloat() < 0.7f,
        alpha = (random.nextFloat()).coerceAtLeast(0f),
        locationX = Dp(centerX + x),
        locationY = Dp(centerY + y),
        particleSize = Dp(randomFloat(type.minSize.value, type.maxSize.value, random)),
        currentAngle = randomizedAngle.toFloat(),
        progressModifier = randomFloat(1f, 2f, random),
        currentColor = color
    )
}