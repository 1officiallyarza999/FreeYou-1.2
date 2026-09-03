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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeyou.data.BlockRepo
import com.freeyou.ui.components.*
import com.freeyou.ui.theme.AppColors
import java.util.Calendar

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit
) {
    val state by BlockRepo.state.collectAsState()
    val days = BlockRepo.daysClean()
    val freedomScore = BlockRepo.freedomScore()
    val (cleanIn30, total30) = BlockRepo.consistencyRatio()

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "בוקר טוב, אלוף ☀️"
        in 12..16 -> "צהריים טובים, גבר ⚡"
        in 17..21 -> "ערב טוב, לוחם 🛡️"
        else -> "לילה של שקט ושליטה 🌙"
    }

    var selectedHeroTab by remember { mutableStateOf(0) } // 0 = Freedom Score, 1 = Streak Days

    val dailyCoreMission = remember(days) {
        when (days % 6) {
            0 -> Triple("עסקים וקריירה", "שלח הצעת ערך או פנה ללקוח פוטנציאלי אחד בלי לדחות.", "💼")
            1 -> Triple("גוף וברזל", "40 שכיבות סמיכה + 50 סקוואטים ומקלחת קרה להמרצת הדם.", "⚡")
            2 -> Triple("תודעה ומיינד", "15 דקות ללא מסכים כלל — נשימות עמוקות וסדר בחדר.", "🧠")
            3 -> Triple("משפחה", "שיחה עמוקה של 10 דקות עם אדם קרוב ללא טלפון בהישג יד.", "🛡️")
            4 -> Triple("מערכות יחסים", "תרגל הקשבה נקייה ומחמאה כנה למישהו בסביבה שלך.", "🤝")
            else -> Triple("ייעוד ומשמעות", "כתוב 3 שורות על הגבר שאתה רוצה להיות בעוד 3 שנים.", "🎯")
        }
    }
    val isDailyMissionDone = state.completedPillarMissions.contains("daily_core_${days % 6}")

    val randomReason = remember(state.reasons) {
        state.reasons.randomOrNull() ?: "אני בונה חיים שאני גאה בהם בכל רגע."
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 110.dp)
    ) {
        // FreeYou Brand Header & Shield Status
        item {
            FreeYouBrandHeader(
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (state.autoAdult) AppColors.Lime.copy(alpha = 0.12f)
                                else AppColors.Rose.copy(alpha = 0.12f)
                            )
                            .border(
                                1.dp,
                                if (state.autoAdult) AppColors.Lime.copy(alpha = 0.4f)
                                else AppColors.Rose.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onNavigate("shield") }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (state.autoAdult) AppColors.Lime else AppColors.Rose)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.autoAdult) "חומת מגן פעילה" else "מגן כבוי",
                                color = if (state.autoAdult) AppColors.Lime else AppColors.Rose,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }

        // Greeting & Motivational Motto
        item {
            Column {
                Text(
                    text = greeting,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Stop escaping. Start building.",
                    fontSize = 12.sp,
                    color = AppColors.Cyan,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp
                )
            }
        }

        // Hero Switcher: Freedom Score & Streak
        item {
            GlassCard(
                borderColor = AppColors.BorderGlass,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Tab switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.CardElevated)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedHeroTab == 0) AppColors.Violet else Color.Transparent)
                            .clickable { selectedHeroTab = 0 }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "מדד החופש ($freedomScore)",
                            color = if (selectedHeroTab == 0) Color.White else AppColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedHeroTab == 1) AppColors.AmberGlow else Color.Transparent)
                            .clickable { selectedHeroTab = 1 }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ימים נקיים ($days)",
                            color = if (selectedHeroTab == 1) Color.Black else AppColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedHeroTab == 0) {
                    FreedomScoreGauge(score = freedomScore, days = days)
                } else {
                    LanternVisual(days = days)
                }
            }
        }

        // Centerpiece Urgent Action: "יש לי דחף עכשיו"
        item {
            UrgePulsingButton(
                onClick = { onNavigate("urge_flow") }
            )
        }

        // Consistency: "28 מתוך 30 ימים"
        item {
            ConsistencyMeter(cleanDays = cleanIn30, totalDays = total30)
        }

        // 4 Key Statistics Glass Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    title = "${state.screenTimeSavedHours}h",
                    subtitle = "זמן מסך נחסך",
                    color = AppColors.Cyan
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    title = "${state.focusHoursWeek}h",
                    subtitle = "שעות פוקוס",
                    color = AppColors.VioletSoft
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    title = "${state.urges}",
                    subtitle = "דחפים שרוכבו",
                    color = AppColors.Lime
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    title = "${state.missions}",
                    subtitle = "משימות שבוצעו",
                    color = AppColors.Amber
                )
            }
        }

        // Today's Core Mission Card
        item {
            GlassCard(
                borderColor = if (isDailyMissionDone) AppColors.Lime.copy(alpha = 0.4f) else AppColors.Violet.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = dailyCoreMission.third, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "משימת היום: ${dailyCoreMission.first}",
                                color = AppColors.VioletSoft,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isDailyMissionDone) "הושלמה בהצלחה ✓" else "ממתינה לביצוע שלך",
                                color = if (isDailyMissionDone) AppColors.Lime else AppColors.TextTertiary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Checkbox(
                        checked = isDailyMissionDone,
                        onCheckedChange = {
                            BlockRepo.togglePillarMission("daily_core_${days % 6}")
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AppColors.Lime,
                            uncheckedColor = AppColors.BorderGlassBright
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = dailyCoreMission.second,
                    color = AppColors.TextPrimary,
                    fontSize = 14.5.sp,
                    lineHeight = 21.sp
                )
            }
        }

        // Quick Navigation Tiles Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    modifier = Modifier.weight(1f),
                    title = "היצר הטוב שלך",
                    subtitle = "מנטור קולי ומצבי אישיות",
                    icon = "🎙️",
                    accent = AppColors.VioletSoft,
                    onClick = { onNavigate("mentor") }
                )
                QuickActionTile(
                    modifier = Modifier.weight(1f),
                    title = "יומנו של גבר",
                    subtitle = "רישום כנות, רגש וטריגרים",
                    icon = "✍️",
                    accent = AppColors.Amber,
                    onClick = { onNavigate("journal") }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    modifier = Modifier.weight(1f),
                    title = "בניית חיים",
                    subtitle = "6 עמודי התווך לגבר",
                    icon = "🏗️",
                    accent = AppColors.BlueElectric,
                    onClick = { onNavigate("grow") }
                )
                QuickActionTile(
                    modifier = Modifier.weight(1f),
                    title = "התקדמות",
                    subtitle = "מדדים, עקביות וטריגרים",
                    icon = "📊",
                    accent = AppColors.Lime,
                    onClick = { onNavigate("progress") }
                )
            }
        }

        // My Why Card
        item {
            GlassCard(
                borderColor = AppColors.Amber.copy(alpha = 0.25f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "עוגן המשמעות שלי (My Why)",
                        color = AppColors.Amber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "עריכה ב'אני' ←",
                        color = AppColors.TextTertiary,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { onNavigate("me") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"$randomReason\"",
                    color = AppColors.TextPrimary,
                    fontSize = 14.5.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    color: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.CardSurface)
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = AppColors.TextTertiary,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: String,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.CardSurface)
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = AppColors.TextPrimary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = AppColors.TextTertiary,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }
    }
}
