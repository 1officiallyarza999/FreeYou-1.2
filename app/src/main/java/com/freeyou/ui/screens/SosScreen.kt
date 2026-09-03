package com.freeyou.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeyou.data.BlockRepo
import com.freeyou.ui.components.GlassCard
import com.freeyou.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun SosScreen(
    onNavigate: (String) -> Unit
) {
    val state by BlockRepo.state.collectAsState()
    var secondsLeft by remember { mutableStateOf(90) }
    val randomReason = remember { state.reasons.randomOrNull() }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    // Breathing phase: 4s inhale, 4s hold, 4s exhale, 4s hold -> 16s cycle
    val breathPhase = (90 - secondsLeft) % 16
    val (breathText, targetScale) = when (breathPhase) {
        in 0..3 -> Pair("שאיפה עמוקה מהאף...", 1.25f)
        in 4..7 -> Pair("החזק את האוויר בריאות...", 1.25f)
        in 8..11 -> Pair("נשיפה איטית ומלאה מהפה...", 0.75f)
        else -> Pair("מנוחה, שמור על רוגע...", 0.75f)
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(4000, easing = LinearEasing),
        label = "breathScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Ink)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SOS: 90 שניות שמשנות הכל",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "הדחף הוא כימיה זמנית במוח. חמצן מפרק אותו.",
                fontSize = 13.5.sp,
                color = AppColors.TextSecondary
            )
        }

        // Central breathing circle
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColors.Cyan.copy(alpha = 0.5f),
                            AppColors.Violet.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        radius = size.width * 0.5f * animatedScale
                    )
                )
                drawCircle(
                    color = AppColors.Cyan.copy(alpha = 0.8f),
                    radius = size.width * 0.45f * animatedScale,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$secondsLeft",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "שניות",
                    fontSize = 14.sp,
                    color = AppColors.Cyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = breathText,
                    fontSize = 13.sp,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Reason quote
        if (randomReason != null) {
            GlassCard(borderColor = AppColors.Amber.copy(alpha = 0.3f)) {
                Text(
                    text = "\"$randomReason\"",
                    color = AppColors.Amber,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Action buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    BlockRepo.recordUrgeOvercome()
                    onNavigate("home")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Lime),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "עברתי את הגל בהצלחה ✓",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = { onNavigate("mentor") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Violet),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "פתח שיחה מיידית עם המנטור 🎙️",
                    color = AppColors.VioletSoft,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
