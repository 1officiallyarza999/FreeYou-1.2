package com.freeyou.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeyou.AlarmService
import com.freeyou.data.BlockRepo
import com.freeyou.ui.components.GlassCard
import com.freeyou.ui.theme.AppColors
import kotlinx.coroutines.delay
import java.util.Calendar

@SuppressLint("MissingPermission")
@Composable
fun MissionScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val state by BlockRepo.state.collectAsState()

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isNight = state.nightSafe && (hour >= 23 || hour < 5)

    var totalSeconds by remember { mutableStateOf(if (isNight) 12 * 60 else 20 * 60) }
    var distanceTraveledMeters by remember { mutableStateOf(0) }
    var startLocation by remember { mutableStateOf<Location?>(null) }

    var emergencyHoldSeconds by remember { mutableStateOf(0f) }
    var isHoldingEmergency by remember { mutableStateOf(false) }

    // Start Alarm service on launch
    DisposableEffect(Unit) {
        val alarmIntent = Intent(context, AlarmService::class.java).putExtra("night", isNight)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(alarmIntent)
        } else {
            context.startService(alarmIntent)
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                if (startLocation == null) {
                    startLocation = loc
                } else {
                    val d = startLocation?.distanceTo(loc) ?: 0f
                    distanceTraveledMeters = d.toInt()
                }
            }
            override fun onStatusChanged(p: String?, s: Int, b: Bundle?) {}
            override fun onProviderEnabled(p: String) {}
            override fun onProviderDisabled(p: String) {}
        }

        try {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 5f, listener)
            } else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 5f, listener)
            }
        } catch (_: Exception) {}

        onDispose {
            try {
                lm.removeUpdates(listener)
            } catch (_: Exception) {}
        }
    }

    // Timer countdown
    LaunchedEffect(Unit) {
        while (totalSeconds > 0) {
            delay(1000)
            totalSeconds--
        }
    }

    // Emergency hold ticker
    LaunchedEffect(isHoldingEmergency) {
        if (isHoldingEmergency) {
            while (isHoldingEmergency && emergencyHoldSeconds < 60f) {
                delay(100)
                emergencyHoldSeconds += 0.1f
            }
        } else {
            emergencyHoldSeconds = 0f
        }
    }

    fun stopAlarmAndComplete() {
        context.stopService(Intent(context, AlarmService::class.java))
        BlockRepo.recordMissionCompleted()
        onNavigate("home")
    }

    val isMissionAccomplished = if (isNight) totalSeconds == 0 else (distanceTraveledMeters >= 250 || totalSeconds == 0)

    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AppColors.Rose.copy(alpha = 0.2f))
                    .border(2.dp, AppColors.Rose, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🚨", fontSize = 36.sp)
            }
        }

        item {
            Text(
                text = "הדיבורים נגמרו. עכשיו הגוף.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isNight) "פרוטוקול לילה ביתי — שבירת הדחף בתנועה" else "צא מהבית והתרחק 250 מטר. הצלצול ייפסק ברגע שתתרחק.",
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // Timer Box
        item {
            GlassCard(
                borderColor = AppColors.Rose.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        color = AppColors.Rose,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (!isNight) {
                        Text(
                            text = "מרחק שהתרחקת: $distanceTraveledMeters / 250 מטר",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (distanceTraveledMeters >= 250) AppColors.Lime else AppColors.Amber
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (distanceTraveledMeters.toFloat() / 250f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = AppColors.Lime,
                            trackColor = AppColors.BorderGlass
                        )
                    }
                }
            }
        }

        // Instructions Card
        item {
            GlassCard {
                Text(
                    text = if (isNight) "הנחיות לילה (12 דקות):" else "מה לעשות עכשיו:",
                    color = AppColors.Amber,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (isNight) {
                    Text(text = "1. שטוף פנים במים קפואים במשך 30 שניות.", color = AppColors.TextSecondary, fontSize = 13.sp)
                    Text(text = "2. 40 שכיבות סמיכה או כפיפות בטן על הרצפה.", color = AppColors.TextSecondary, fontSize = 13.sp)
                    Text(text = "3. כוס מים גדולה ונשימות סרעפתיות.", color = AppColors.TextSecondary, fontSize = 13.sp)
                } else {
                    Text(text = "1. נעל נעליים וצא מהדלת מיד — בלי טלפון ביד.", color = AppColors.TextSecondary, fontSize = 13.sp)
                    Text(text = "2. הליכה מהירה או ריצה קלה ברחוב.", color = AppColors.TextSecondary, fontSize = 13.sp)
                    Text(text = "3. כשתתרחק 250 מטר מהבית — האזעקה תפסיק אוטומטית.", color = AppColors.TextSecondary, fontSize = 13.sp)
                }
            }
        }

        // Complete action button
        item {
            Button(
                onClick = { stopAlarmAndComplete() },
                enabled = isMissionAccomplished,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Lime,
                    disabledContainerColor = AppColors.CardElevated
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    text = if (isMissionAccomplished) "המשימה הושלמה — כבה אזעקה" else "המשימה עדיין פעילה...",
                    color = if (isMissionAccomplished) Color.Black else AppColors.TextTertiary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Emergency 60s bypass
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.CardSurface)
                    .border(1.dp, AppColors.BorderGlass, RoundedCornerShape(14.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isHoldingEmergency = true
                                tryAwaitRelease()
                                isHoldingEmergency = false
                            }
                        )
                    }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (emergencyHoldSeconds >= 60f) "שחרור חירום אושר ✓"
                        else if (isHoldingEmergency) "החזק עוד ${(60f - emergencyHoldSeconds).toInt()} שניות ברצף..."
                        else "שחרור חירום (החזק לחוץ 60 שניות רצוף)",
                        color = if (emergencyHoldSeconds >= 60f) AppColors.Lime else AppColors.TextTertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isHoldingEmergency) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (emergencyHoldSeconds / 60f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = AppColors.Rose,
                            trackColor = AppColors.BorderGlass
                        )
                    }
                }
            }

            if (emergencyHoldSeconds >= 60f) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { stopAlarmAndComplete() },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Rose),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("אשר שחרור חירום")
                }
            }
        }
    }
}
