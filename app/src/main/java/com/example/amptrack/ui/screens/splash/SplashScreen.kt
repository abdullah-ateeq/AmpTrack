package com.example.amptrack.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val lineProgress = remember { Animatable(0f) }
    val powerAlpha = remember { Animatable(0f) }
    val voltageAlpha = remember { Animatable(0f) }
    val currentIndicatorAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Sequence of lightweight animations totaling ~1.3s
        launch {
            lineProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        delay(250)
        launch {
            powerAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350)
            )
        }
        delay(150)
        launch {
            voltageAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350)
            )
        }
        delay(150)
        launch {
            currentIndicatorAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350)
            )
        }
        delay(100)
        launch {
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400)
            )
        }

        delay(650) // Brief hold before transitioning (~1.3 s total)
        onSplashFinished()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Electrical circuit graphic animation
            Box(
                modifier = Modifier
                    .size(width = 240.dp, height = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val centerY = canvasHeight / 2

                    // Circuit line drawn dynamically
                    val endX = canvasWidth * lineProgress.value
                    drawLine(
                        color = primaryColor,
                        start = Offset(0f, centerY),
                        end = Offset(endX, centerY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Power node on line
                    if (lineProgress.value > 0.35f) {
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = Offset(canvasWidth * 0.3f, centerY)
                        )
                    }

                    // Voltage node on line
                    if (lineProgress.value > 0.7f) {
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = Offset(canvasWidth * 0.7f, centerY)
                        )
                    }
                }

                // Electrical tags above and below
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Power value tag
                    Text(
                        text = "120 W",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .alpha(powerAlpha.value)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    // Current indicator (I = P/V)
                    Text(
                        text = "→ 10 A →",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.alpha(currentIndicatorAlpha.value)
                    )

                    // Voltage value tag
                    Text(
                        text = "12 V",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .alpha(voltageAlpha.value)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App title & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(titleAlpha.value)
            ) {
                Text(
                    text = "AMPTRACK",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Power to Current Calculator",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
