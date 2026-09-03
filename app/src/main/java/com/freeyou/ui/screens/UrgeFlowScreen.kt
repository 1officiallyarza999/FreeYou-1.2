package com.freeyou.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.freeyou.ui.components.GlassCard
import com.freeyou.ui.theme.AppColors
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun UrgeFlowScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val state by BlockRepo.state.collectAsState()

    var step by remember { mutableStateOf(1) } // 1 = Calm, 2 = Intensity & Trigger, 3 = Surfing & Breathing, 4 = Replacement, 5 = Victory
    var urgeIntensity by remember { mutableStateOf(7) }
    var selectedTrigger by remember { mutableStateOf("עייפות") }
    var surfingSeconds by remember { mutableStateOf(90) }
    var selectedReplacement by remember { mutableStateOf("20 שכיבות סמיכה") }

    val triggersList = listOf("שעמום", "לחץ ועומס", "עייפות", "בדידות", "כעס / תסכול", "הרגל אוטומטי")
    val replacementsList = listOf(
        "20 שכיבות סמיכה עכשיו" to "💪",
        "30 סקוואטים מהירים" to "🦵",
        "שטיפת פנים במים קפואים" to "❄️",
        "כוס מים גדולה ונשימות" to "💧",
        "הליכה של 5 דקות בחוץ" to "🚶",
        "רישום 3 שורות ביומן" to "✍️",
        "שיחה קצרה עם חבר/משפחה" to "📞",
        "משימת עסק זריזה של 5 דק'" to "💼"
    )

    // Text to Speech
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale("he", "IL")
                ttsInstance?.setSpeechRate(0.92f)
                ttsInstance?.setPitch(if (state.mentorMode == "warrior") 0.85f else 0.95f)
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

    // Breathing phase: 4s inhale, 4s hold, 4s exhale, 4s hold
    val breathPhase = (90 - surfingSeconds) % 16
    val (breathText, targetScale) = when (breathPhase) {
        in 0..3 -> Pair("שאיפה עמוקה מהאף...", 1.22f)
        in 4..7 -> Pair("החזק את האוויר בריאות...", 1.22f)
        in 8..11 -> Pair("נשיפה איטית ומלאה מהפה...", 0.78f)
        else -> Pair("רוגע מוחלט, שמור על צלילות...", 0.78f)
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(4000, easing = LinearEasing),
        label = "urgeBreathScale"
    )

    // Surfing countdown timer in step 3
    LaunchedEffect(step) {
        if (step == 3) {
            surfingSeconds = 90
            tts?.speak("אני איתך. אל תילחם בדחף. תנשום עמוק ותן לגל לעבור. הדחף הוא רק כימיה זמנית שתתפוגג.", TextToSpeech.QUEUE_FLUSH, null, "urge_mentor")
            while (surfingSeconds > 0 && step == 3) {
                delay(1000)
                surfingSeconds--
            }
            if (surfingSeconds == 0) {
                step = 4
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Ink)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        AnimatedContent(
            targetState = step,
            label = "urge_flow_step"
        ) { currentStep ->
            when (currentStep) {
                // STEP 1: Instant Calm
                1 -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(30.dp))
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(AppColors.Cyan.copy(alpha = 0.15f))
                                .border(1.5.dp, AppColors.Cyan.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🛑", fontSize = 42.sp)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "ההרגל הישן נעצר כאן.",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = AppColors.TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "אתה לא עבד של דחף חולף. המוח שלך מנסה לברוח מרגש או מעייפות.\nבוא נפרק את זה ביחד — צעד אחרי צעד.",
                            fontSize = 15.sp,
                            color = AppColors.TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }

                    GlassCard(
                        borderColor = AppColors.Amber.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "היצר הטוב שלך מזכיר לך:",
                            color = AppColors.Amber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"${state.reasons.firstOrNull() ?: "אני גבר שבונה חיים ולא בורח למסכים"}\"",
                            color = AppColors.TextPrimary,
                            fontSize = 14.5.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { step = 2 },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Cyan),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(vertical = 15.dp)
                        ) {
                            Text(
                                text = "המשך לפירוק הדחף ←",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { onNavigate("home") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.BorderGlass)
                        ) {
                            Text(text = "חזור למסך הבית", color = AppColors.TextSecondary)
                        }
                    }
                }

                // STEP 2: Intensity & Trigger
                2 -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "מה קורה בתוכך עכשיו?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "זיהוי הטריגר והעוצמה מפרק 50% מהדחף באופן מיידי.",
                            fontSize = 13.5.sp,
                            color = AppColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Intensity Selector (1-10)
                    item {
                        GlassCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "עוצמת הדחף עכשיו:",
                                    color = AppColors.TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$urgeIntensity / 10",
                                    color = if (urgeIntensity >= 8) AppColors.Rose else if (urgeIntensity >= 5) AppColors.Amber else AppColors.Lime,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Slider(
                                value = urgeIntensity.toFloat(),
                                onValueChange = { urgeIntensity = it.toInt() },
                                valueRange = 1f..10f,
                                steps = 8,
                                colors = SliderDefaults.colors(
                                    thumbColor = AppColors.Amber,
                                    activeTrackColor = AppColors.Amber
                                )
                            )
                        }
                    }

                    // Triggers Grid
                    item {
                        GlassCard {
                            Text(
                                text = "מה הפעיל את הדחף רגע לפני?",
                                color = AppColors.TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            triggersList.chunked(2).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { trg ->
                                        val isSelected = selectedTrigger == trg
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) AppColors.Violet.copy(alpha = 0.25f)
                                                    else AppColors.CardElevated
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) AppColors.VioletSoft else AppColors.BorderGlass,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable { selectedTrigger = trg }
                                                .padding(vertical = 11.dp, horizontal = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = trg,
                                                color = if (isSelected) Color.White else AppColors.TextSecondary,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { step = 3 },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Violet),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(vertical = 15.dp)
                        ) {
                            Text(
                                text = "התחל גלישת דחף (Urge Surfing) ←",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // STEP 3: Urge Surfing & Breathing
                3 -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "גלישת דחף • 90 שניות",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "הדחף מגיע לשיא ויורד. תנשום עם העיגול.",
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                    }

                    // Breathing visual orb
                    Box(
                        modifier = Modifier.size(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        AppColors.Cyan.copy(alpha = 0.45f),
                                        AppColors.Violet.copy(alpha = 0.25f),
                                        Color.Transparent
                                    ),
                                    radius = size.width * 0.5f * animatedScale
                                )
                            )
                            drawCircle(
                                color = AppColors.Cyan.copy(alpha = 0.8f),
                                radius = size.width * 0.44f * animatedScale,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$surfingSeconds",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                text = "שניות",
                                fontSize = 13.sp,
                                color = AppColors.Cyan,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = breathText,
                                fontSize = 13.sp,
                                color = AppColors.TextPrimary,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { step = 4 },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Lime),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text(
                                text = "הגל נרגע — בחר פעולה מחליפה ✓",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { onNavigate("mentor") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.VioletSoft)
                        ) {
                            Text(text = "עבור לשיחה קולית עם המנטור 🎙️", color = AppColors.VioletSoft)
                        }
                    }
                }

                // STEP 4: Replacement Action
                4 -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "פעולה מחליפה (Replacement)",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "האנרגיה של הדחף חייבת לעבור לפעולה חיובית מידית.",
                            fontSize = 13.5.sp,
                            color = AppColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    replacementsList.forEach { (action, icon) ->
                        item {
                            val isSelected = selectedReplacement == action
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) AppColors.Lime.copy(alpha = 0.15f)
                                        else AppColors.CardSurface
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) AppColors.Lime else AppColors.BorderGlass,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedReplacement = action }
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = icon, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = action,
                                        color = if (isSelected) Color.White else AppColors.TextSecondary,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                BlockRepo.recordUrgeWithDetails(
                                    trigger = selectedTrigger,
                                    intensity = urgeIntensity,
                                    replacementAction = selectedReplacement
                                )
                                step = 5
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Lime),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(vertical = 15.dp)
                        ) {
                            Text(
                                text = "סיימתי בהצלחה — רשום ניצחון 🏆",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // STEP 5: Victory Screen
                5 -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(AppColors.Lime.copy(alpha = 0.2f))
                            .border(2.dp, AppColors.Lime, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👑", fontSize = 48.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "עוד גל נוצח.",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = AppColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "כל פעם שאתה בוחר לא לברוח, החיווט במוח משתנה לטובתך.\nמדד החופש שלך עלה.",
                        fontSize = 15.sp,
                        color = AppColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = { onNavigate("home") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Lime),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 15.dp)
                    ) {
                        Text(
                            text = "חזור ללוח הבקרה הראשי",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
