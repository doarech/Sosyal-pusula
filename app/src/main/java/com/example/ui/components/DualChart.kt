package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CheckInEntity
import com.example.ui.theme.ScreenTimeColor
import com.example.ui.theme.getScoreColor

@Composable
fun DualChart(
    checkIns: List<CheckInEntity>,
    formatWeek: (String) -> String,
    modifier: Modifier = Modifier
) {
    val items = checkIns.take(7).reversed()
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFF97316),
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier.size(10.dp)
                ) {}
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Yalnızlık Skoru",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = ScreenTimeColor,
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier.size(10.dp)
                ) {}
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ekran Süresi (sa)",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant
                )
            }
        }

        // Custom Canvas Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val chartWidth = size.width
                val chartHeight = size.height - 24.dp.toPx() // bottom label space
                val count = items.size
                if (count == 0) return@Canvas

                val slotWidth = chartWidth / count.toFloat()
                val barWidth = (slotWidth * 0.22f).coerceIn(6f, 22f)
                val spacing = 3f

                val textPaint = android.graphics.Paint().apply {
                    color = onSurfaceVariant.toArgb()
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                // Horizontal guideline
                drawLine(
                    color = gridColor,
                    start = Offset(0f, chartHeight),
                    end = Offset(chartWidth, chartHeight),
                    strokeWidth = 1.dp.toPx()
                )

                items.forEachIndexed { i, entry ->
                    val centerX = i * slotWidth + slotWidth / 2f

                    // Loneliness bar (1 - 100 scale)
                    val scoreNorm = (entry.score.coerceIn(1, 100) / 100f)
                    val scoreBarHeight = (scoreNorm * (chartHeight - 10f)).coerceAtLeast(6f)
                    val scoreBarColor = getScoreColor(entry.score)

                    drawRoundRect(
                        color = scoreBarColor,
                        topLeft = Offset(centerX - barWidth - spacing / 2, chartHeight - scoreBarHeight),
                        size = Size(barWidth, scoreBarHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Screen time bar (0 - 10 hours max)
                    val screenNorm = (entry.screenHours / 10f).coerceIn(0f, 1f)
                    val screenBarHeight = (screenNorm * (chartHeight - 10f)).coerceAtLeast(6f)

                    drawRoundRect(
                        color = ScreenTimeColor.copy(alpha = 0.85f),
                        topLeft = Offset(centerX + spacing / 2, chartHeight - screenBarHeight),
                        size = Size(barWidth, screenBarHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Date label at bottom
                    val label = formatWeek(entry.weekString)
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        centerX,
                        size.height - 4.dp.toPx(),
                        textPaint
                    )
                }
            }
        }
    }
}
