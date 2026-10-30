package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.getScoreColor

@Composable
fun LonelinessScaleBar(
    score: Int,
    levelLabel: String,
    modifier: Modifier = Modifier
) {
    val scoreColor = getScoreColor(score)

    // Normalize score 0..100 to fraction 0f..1f
    val targetFraction = (score.coerceIn(0, 100) / 100f)

    var animatedTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(score) {
        animatedTarget = targetFraction
    }

    val animatedFraction by animateFloatAsState(
        targetValue = animatedTarget,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "scaleBarAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sosyal Denge Skalası",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$score / 100 ($levelLabel)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Segmented Horizontal Bar with Pointer
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            val totalWidth = maxWidth

            Column {
                // 4-Colored Segmented Bar Track (1-25 Düşük, 26-50 Orta, 51-75 Yüksek, 76-100 Şiddetli)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(5.dp)
                        )
                ) {
                    // Düşük (1-25): 25%
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .background(Color(0xFF059669))
                    )
                    // Orta (26-50): 25%
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .background(Color(0xFFD97706))
                    )
                    // Yüksek (51-75): 25%
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .background(Color(0xFFEA580C))
                    )
                    // Şiddetli (76-100): 25%
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .background(Color(0xFFDC2626))
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Labels below bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "0 Düşük",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "25",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "50",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "75",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "100 Şiddetli",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Indicator Needle / Pointer
            val indicatorXOffset = (totalWidth - 14.dp) * animatedFraction
            Box(
                modifier = Modifier
                    .offset(x = indicatorXOffset, y = (-3).dp)
                    .size(16.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(2.5.dp, scoreColor, CircleShape)
            )
        }
    }
}
