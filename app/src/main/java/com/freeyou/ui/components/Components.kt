package com.freeyou.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeyou.ui.theme.AppColors

@Composable
fun MeshBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Ink)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.Violet.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.1f),
                    radius = size.width * 0.7f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.Amber.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.22f),
                    radius = size.width * 0.65f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.Rose.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.75f),
                    radius = size.width * 0.8f
                )
            )
        }
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppColors.GlassCardBg,
    borderColor: Color = AppColors.BorderGlass,
    cornerRadius: Dp = 22.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        AppColors.CardSurface.copy(alpha = 0.85f)
                    )
                )
            )
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .padding(18.dp),
        content = content
    )
}

@Composable
fun UrgePulsingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_sos")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sosScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sosGlow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing aura
        Box(
            modifier = Modifier
                .fillMaxWidth(scale)
                .height(68.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            AppColors.Rose.copy(alpha = glowAlpha),
                            AppColors.Amber.copy(alpha = glowAlpha * 0.8f),
                            AppColors.Rose.copy(alpha = glowAlpha)
                        )
                    )
                )
        )

        // Main button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF881337), // Deep velvet crimson
                            Color(0xFFE11D48), // Rose
                            Color(0xFFD97706)  // Amber glow
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF80A0),
                            Color(0xFFFFD166)
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚡", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "יש לי דחף עכשיו",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "עצור את ההרגל האוטומטי • 90 שניות הצלה",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun FreedomScoreGauge(
    score: Int,
    days: Int,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "scoreAnim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            // Background arc track
            drawArc(
                color = AppColors.CardElevated,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 14.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )

            // Dynamic progress arc
            val sweep = (260f * (animatedScore / 100f)).coerceIn(10f, 260f)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        AppColors.Cyan,
                        AppColors.Violet,
                        AppColors.Amber,
                        AppColors.Lime,
                        AppColors.Cyan
                    )
                ),
                startAngle = 140f,
                sweepAngle = sweep,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 14.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )

            // Inner subtle glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AppColors.Violet.copy(alpha = 0.25f),
                        AppColors.Cyan.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    radius = size.width * 0.42f
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "מדד החופש",
                fontSize = 12.sp,
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Text(
                text = "$animatedScore",
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.TextPrimary,
                letterSpacing = (-1).sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.Cyan.copy(alpha = 0.15f))
                    .border(1.dp, AppColors.Cyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (score >= 80) "עוצמה ושליטה גבוהה" else if (score >= 50) "בנייה יציבה" else "התחלת הדרך",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Cyan
                )
            }
        }
    }
}

@Composable
fun ConsistencyMeter(
    cleanDays: Int,
    totalDays: Int = 30,
    modifier: Modifier = Modifier
) {
    val percentage = ((cleanDays.toFloat() / totalDays) * 100).toInt()

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = AppColors.BorderGlass
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "עקביות 30 ימים אחרונים",
                    color = AppColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$cleanDays מתוך $totalDays ימים נקיים • יום בודד לא הורס תהליך",
                    color = AppColors.TextSecondary,
                    fontSize = 11.5.sp
                )
            }
            Text(
                text = "$percentage%",
                color = if (percentage >= 80) AppColors.Lime else AppColors.Amber,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 30 micro dots matrix
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 1..totalDays) {
                val isClean = i <= cleanDays
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .padding(horizontal = 1.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (isClean) AppColors.Lime.copy(alpha = 0.85f)
                            else AppColors.CardElevated
                        )
                )
            }
        }
    }
}

@Composable
fun TopBarHeader(
    isStrict: Boolean,
    isShieldActive: Boolean,
    onVoiceClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isShieldActive) AppColors.Lime else AppColors.Rose)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "FREEYOU",
                color = AppColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isStrict) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Rose.copy(alpha = 0.15f))
                        .border(1.dp, AppColors.Rose.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "קפדני 🔒",
                        color = AppColors.Rose,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isShieldActive) AppColors.Lime.copy(alpha = 0.12f)
                        else AppColors.Rose.copy(alpha = 0.12f)
                    )
                    .border(
                        1.dp,
                        if (isShieldActive) AppColors.Lime.copy(alpha = 0.35f)
                        else AppColors.Rose.copy(alpha = 0.35f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isShieldActive) "מגן פעיל" else "מגן כבוי",
                    color = if (isShieldActive) AppColors.Lime else AppColors.Rose,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppColors.Violet.copy(alpha = 0.25f))
                    .border(1.dp, AppColors.Violet.copy(alpha = 0.5f), CircleShape)
                    .clickable { onVoiceClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎙️", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun LanternVisual(
    days: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lantern_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AppColors.Amber.copy(alpha = glowAlpha),
                        AppColors.Rose.copy(alpha = glowAlpha * 0.4f),
                        Color.Transparent
                    ),
                    radius = size.width * 0.5f * pulseScale
                )
            )
            drawCircle(
                color = AppColors.BorderGlass,
                radius = size.width * 0.44f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🔥",
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$days",
                fontSize = 62.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.TextPrimary,
                letterSpacing = (-1).sp
            )
            Text(
                text = if (days == 1) "יום נקי וחופשי" else "ימים נקיים וחופשיים",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.Amber
            )
        }
    }
}

@Composable
fun AudioWaveformVisual(
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "waves")
    val wave1 by transition.animateFloat(
        initialValue = 12f,
        targetValue = 48f,
        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val wave2 by transition.animateFloat(
        initialValue = 20f,
        targetValue = 65f,
        animationSpec = infiniteRepeatable(tween(580, easing = LinearEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val wave3 by transition.animateFloat(
        initialValue = 8f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "w3"
    )
    val wave4 by transition.animateFloat(
        initialValue = 16f,
        targetValue = 55f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "w4"
    )

    Row(
        modifier = modifier
            .height(70.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bars = if (isListening) listOf(wave1, wave2, wave4, wave3, wave2, wave1, wave3) else listOf(12f, 16f, 18f, 22f, 18f, 16f, 12f)
        bars.forEach { h ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(h.dp)
                    .clip(CircleShape)
                    .background(if (isListening) AppColors.Amber else AppColors.Violet)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}
