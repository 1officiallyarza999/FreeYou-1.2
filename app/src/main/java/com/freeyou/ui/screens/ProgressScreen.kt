package com.freeyou.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeyou.data.BlockRepo
import com.freeyou.ui.components.ConsistencyMeter
import com.freeyou.ui.components.FreedomScoreGauge
import com.freeyou.ui.components.GlassCard
import com.freeyou.ui.theme.AppColors

@Composable
fun ProgressScreen(
    onNavigate: (String) -> Unit
) {
    val state by BlockRepo.state.collectAsState()
    val days = BlockRepo.daysClean()
    val freedomScore = BlockRepo.freedomScore()
    val (cleanIn30, total30) = BlockRepo.consistencyRatio()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "מדדי התקדמות ועוצמה",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = "הנתונים מוכיחים: המוח שלך משתקם ונבנה מחדש",
                        fontSize = 13.5.sp,
                        color = AppColors.TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.CardElevated)
                        .border(1.dp, AppColors.BorderGlass, RoundedCornerShape(12.dp))
                        .clickable { onNavigate("home") }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "חזור ←", color = AppColors.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Freedom Score Visual Gauge
        item {
            GlassCard(borderColor = AppColors.VioletSoft.copy(alpha = 0.35f)) {
                FreedomScoreGauge(score = freedomScore, days = days)
            }
        }

        // 30 Days Consistency Meter
        item {
            ConsistencyMeter(cleanDays = cleanIn30, totalDays = total30)
        }

        // Executive Metrics Matrix
        item {
            GlassCard {
                Text(
                    text = "מאזן הישגים מצטבר",
                    color = AppColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricRow(
                        icon = "🛡️",
                        title = "דחפים שרוכבו בהצלחה",
                        value = "${state.urges}",
                        color = AppColors.Lime
                    )
                    HorizontalDivider(color = AppColors.BorderGlass)
                    MetricRow(
                        icon = "⏱️",
                        title = "זמן מסך שנחסך",
                        value = "${state.screenTimeSavedHours} שעות",
                        color = AppColors.Cyan
                    )
                    HorizontalDivider(color = AppColors.BorderGlass)
                    MetricRow(
                        icon = "🧠",
                        title = "שעות פוקוס עמוק השבוע",
                        value = "${state.focusHoursWeek} שעות",
                        color = AppColors.VioletSoft
                    )
                    HorizontalDivider(color = AppColors.BorderGlass)
                    MetricRow(
                        icon = "🎯",
                        title = "משימות עמודי תווך שבוצעו",
                        value = "${state.lessons.size + state.completedPillarMissions.size}",
                        color = AppColors.Amber
                    )
                }
            }
        }

        // Triggers Breakdown Analytics
        item {
            GlassCard(borderColor = AppColors.BorderGlass) {
                Text(
                    text = "ניתוח טריגרים שכיחים",
                    color = AppColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "הבנת המקור מחליפה אשמה באסטרטגיה מדויקת",
                    color = AppColors.TextTertiary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                val triggers = listOf(
                    Triple("עייפות ושעות לילה", 42, AppColors.Amber),
                    Triple("שעמום וחיפוש גירוי", 28, AppColors.Cyan),
                    Triple("לחץ בעבודה / לימודים", 18, AppColors.VioletSoft),
                    Triple("בדידות ותסכול", 12, AppColors.Rose)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    triggers.forEach { (name, pct, color) ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = name, color = AppColors.TextSecondary, fontSize = 13.sp)
                                Text(text = "$pct%", color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            LinearProgressIndicator(
                                progress = { pct / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = color,
                                trackColor = AppColors.BorderGlass
                            )
                        }
                    }
                }
            }
        }

        // Clean Mind Milestones Card
        item {
            GlassCard(borderColor = AppColors.Amber.copy(alpha = 0.3f)) {
                Text(
                    text = "אבני דרך נוירולוגיות",
                    color = AppColors.Amber,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val milestones = listOf(
                    Triple("3 ימים", "שבירת תלות הדופמין המיידית", days >= 3),
                    Triple("7 ימים", "עלייה טבעית באנרגיה ובטסטוסטרון", days >= 7),
                    Triple("14 ימים", "שיפור משמעותי בריכוז ובביטחון החברתי", days >= 14),
                    Triple("30 ימים", "חיווט מחדש של קולטני הדופמין (Neuroplasticity)", days >= 30),
                    Triple("90 ימים", "חופש מוחלט ובניית זהות חדשה ויציבה", days >= 90)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    milestones.forEach { (period, desc, isAchieved) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isAchieved) AppColors.Lime.copy(alpha = 0.08f) else Color.Transparent)
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(if (isAchieved) AppColors.Lime else AppColors.CardElevated)
                                    .border(1.dp, if (isAchieved) AppColors.Lime else AppColors.BorderGlass, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAchieved) {
                                    Text(text = "✓", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = period,
                                    color = if (isAchieved) AppColors.Lime else AppColors.TextTertiary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = desc,
                                    color = if (isAchieved) AppColors.TextPrimary else AppColors.TextTertiary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    icon: String,
    title: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, color = AppColors.TextSecondary, fontSize = 14.sp)
        }
        Text(text = value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
