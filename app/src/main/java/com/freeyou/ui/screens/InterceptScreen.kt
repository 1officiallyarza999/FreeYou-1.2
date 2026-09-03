package com.freeyou.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeyou.data.BlockRepo
import com.freeyou.data.MentorContext
import com.freeyou.data.MentorLines
import com.freeyou.ui.theme.AppColors
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

@Composable
fun InterceptScreen(
    target: String,
    count: Int,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val state by BlockRepo.state.collectAsState()
    val days = BlockRepo.daysClean()
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    val mentorCtx = remember {
        MentorContext(reasons = state.reasons, days = days, attempt = count, hour = hour)
    }
    val spokenLine = remember { MentorLines.interceptLine(mentorCtx) }
    val personalReason = remember { MentorLines.ownWords(mentorCtx) }

    var lockSeconds by remember { mutableStateOf(6) }
    var isUnlocked by remember { mutableStateOf(false) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale("he", "IL")
                ttsInstance?.setSpeechRate(0.92f)
                ttsInstance?.setPitch(0.9f)
                ttsInstance?.speak(spokenLine, TextToSpeech.QUEUE_FLUSH, null, "intercept_voice")
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

    LaunchedEffect(Unit) {
        while (lockSeconds > 0) {
            delay(1000)
            lockSeconds--
        }
        isUnlocked = true
    }

    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Ink)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // FreeYou Hero Badge with glowing breathing halo
            Box(
                modifier = Modifier.size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AppColors.Cyan.copy(alpha = 0.5f),
                                AppColors.Violet.copy(alpha = 0.35f),
                                AppColors.Rose.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            radius = size.width * 0.52f * scale
                        )
                    )
                }
                com.freeyou.ui.components.FreeYouHeroBadge(sizeDp = 130.dp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (count <= 1) "ההרגל הישן נעצר כאן" else "ניסיון שני היום • עצור ונשום",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (target.isNotBlank()) {
                Text(
                    text = "נעצרה גישה אל: $target",
                    fontSize = 13.sp,
                    color = AppColors.Rose,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = spokenLine,
                fontSize = 16.sp,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            if (personalReason != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Amber.copy(alpha = 0.1f))
                        .border(1.dp, AppColors.Amber.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "\"$personalReason\"",
                        color = AppColors.Amber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            if (!isUnlocked) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.CardSurface)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "תנשום. נעול ל-$lockSeconds שניות...",
                        color = AppColors.TextTertiary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = {
                        BlockRepo.recordUrgeOvercome()
                        onNavigate("home")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Amber),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 15.dp)
                ) {
                    Text(
                        text = "אני חוזר. תודה.",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val nextRoute = if (count <= 1) "journal" else "mission"
                val nextLabel = if (count <= 1) "יומנו של גבר — כותב עכשיו" else "משימת גוף — יוצא עכשיו"

                OutlinedButton(
                    onClick = { onNavigate(nextRoute) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.BorderGlass),
                    contentPadding = PaddingValues(vertical = 15.dp)
                ) {
                    Text(
                        text = nextLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
