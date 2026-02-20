package com.odevs.photodiary.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun DoodleBorderBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 2.dp.toPx()
            val width = size.width
            val height = size.height
            val waveAmplitude = 5f
            val lineCount = 2
            val SoftOrange = Color(0xFFFDA769)
            val colors = List(2) { SoftOrange }

            repeat(lineCount) { i ->
                val color = colors[i % colors.size]
                val offset = i * 7f // egymástól eltolt vonalak
                val stroke = Stroke(width = strokeWidth)

                drawPath(createWavyBorderPath(width, height, waveAmplitude + i, offset), color, style = stroke)
            }
        }

        content()
    }
}

fun createWavyBorderPath(width: Float, height: Float, amplitude: Float, offset: Float): Path {
    val path = Path()

    val steps = 60
    val dx = width / steps

    // Felső oldal
    for (i in 0..steps) {
        val x = i * dx
        val y = offset + amplitude * sin((i.toFloat() / steps) * 2 * PI).toFloat()
        if (i == 0) path.moveTo(x, y)
        else path.lineTo(x, y)
    }

    // Jobb oldal
    val dy = height / steps
    for (i in 0..steps) {
        val y = i * dy
        val x = width - offset + amplitude * sin((i.toFloat() / steps) * 2 * PI).toFloat()
        path.lineTo(x, y)
    }

    // Alsó oldal
    for (i in steps downTo 0) {
        val x = i * dx
        val y = height - offset + amplitude * sin((i.toFloat() / steps) * 2 * PI).toFloat()
        path.lineTo(x, y)
    }

    // Bal oldal
    for (i in steps downTo 0) {
        val y = i * dy
        val x = offset + amplitude * sin((i.toFloat() / steps) * 2 * PI).toFloat()
        path.lineTo(x, y)
    }

    path.close()
    return path
}
