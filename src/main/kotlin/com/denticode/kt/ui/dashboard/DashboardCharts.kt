package com.denticode.kt.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.ui.theme.AppTypography
import kotlin.math.min

@Composable
fun RevenueLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    chartHeight: androidx.compose.ui.unit.Dp = 120.dp,
    lineColor: Color = Color(0xFF34C759),
    fillTop: Color = Color(0xFF34C759).copy(alpha = 0.18f),
) {
    if (values.isEmpty()) return
    val maxV = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    Canvas(modifier = modifier.fillMaxWidth().height(chartHeight)) {
        val w = size.width
        val h = size.height
        val padL = 4f
        val padR = 8f
        val padT = 12f
        val padB = 8f
        val innerW = w - padL - padR
        val innerH = h - padT - padB
        val step = if (values.size <= 1) 0f else innerW / (values.size - 1)
        val pts =
            values.mapIndexed { i, v ->
                val x = padL + i * step
                val y = padT + innerH * (1f - v / maxV)
                Offset(x, y)
            }
        val line = Path().apply {
            pts.forEachIndexed { i, p ->
                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
        }
        val fillPath =
            Path().apply {
                addPath(line)
                lineTo(pts.last().x, h - padB)
                lineTo(pts.first().x, h - padB)
                close()
            }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(listOf(fillTop, Color.Transparent), startY = padT, endY = h - padB),
        )
        drawPath(
            path = line,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )
        pts.forEach { p ->
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = p)
            drawCircle(color = lineColor, radius = 3.dp.toPx(), center = p)
        }
    }
}

@Composable
fun AppointmentStatusDonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 160.dp,
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = min(this.size.width, this.size.height) * 0.14f
            val r = min(this.size.width, this.size.height) / 2f - stroke
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            var start = -90f
            slices.forEach { slice ->
                val sweep = 360f * (slice.value / total)
                drawArc(
                    color = slice.color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - r, center.y - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = stroke),
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Citas", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("por estado", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun DonutLegend(slices: List<DonutSlice>, modifier: Modifier = Modifier) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        slices.forEach { s ->
            val pct = ((s.value / total) * 100f).toInt()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Canvas(Modifier.size(10.dp)) {
                    drawCircle(color = s.color)
                }
                Text(
                    "${s.label} · $pct%",
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
