package com.freeyou.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.freeyou.data.*
import com.freeyou.ui.components.GlassCard
import com.freeyou.ui.theme.AppColors

@Composable
fun GrowScreen(
    onNavigate: (String) -> Unit
) {
    val state by BlockRepo.state.collectAsState()
    var selectedPillarIndex by remember { mutableStateOf(0) }

    val pillars = listOf(
        Triple("עסקים וקריירה", BIZ_LESSONS, "💼"),
        Triple("גוף וברזל", BODY_LESSONS, "⚡"),
        Triple("תודעה ומיינד", MIND_LESSONS, "🧠"),
        Triple("משפחה", FAMILY_LESSONS, "🛡️"),
        Triple("מערכות יחסים", RELATIONSHIPS_LESSONS, "🤝"),
        Triple("ייעוד ומשמעות", PURPOSE_LESSONS, "🎯")
    )

    val currentPillar = pillars[selectedPillarIndex]
    val currentLessons = currentPillar.second

    val allLessonsCount = pillars.sumOf { it.second.size }
    val completedCount = state.lessons.size
    val progress = (completedCount.toFloat() / allLessonsCount).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
    ) {
        item {
            Text(
                text = "בניית חיים • 6 עמודי התווך",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.TextPrimary
            )
            Text(
                text = "Stop escaping. Start building. אנרגיה שנשמרת הופכת לעוצמה ממשית.",
                fontSize = 13.5.sp,
                color = AppColors.TextSecondary
            )
        }

        // Overall progress card
        item {
            GlassCard(
                borderColor = AppColors.Lime.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "התקדמות במסלול הגדילה",
                        color = AppColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$completedCount מתוך $allLessonsCount הושלמו",
                        color = AppColors.Lime,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AppColors.Lime,
                    trackColor = AppColors.BorderGlass
                )
            }
        }

        // 6 Pillars Horizontal Filter Bar
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(pillars) { index, pillar ->
                    val isSelected = selectedPillarIndex == index
                    val pillarDoneCount = pillar.second.indices.count { idx ->
                        state.lessons.contains("p_${index}_$idx")
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) AppColors.Violet
                                else AppColors.CardSurface
                            )
                            .border(
                                1.dp,
                                if (isSelected) AppColors.VioletSoft else AppColors.BorderGlass,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedPillarIndex = index }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = pillar.third, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${pillar.first} ($pillarDoneCount/${pillar.second.size})",
                                color = if (isSelected) Color.White else AppColors.TextSecondary,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Pillar Title Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(text = currentPillar.third, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentPillar.first,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            }
        }

        // Lessons List for selected Pillar
        itemsIndexed(currentLessons) { idx, lesson ->
            val lessonId = "p_${selectedPillarIndex}_$idx"
            val isDone = state.lessons.contains(lessonId)

            LessonCard(
                index = idx + 1,
                lesson = lesson,
                isDone = isDone,
                onToggleDone = {
                    BlockRepo.completeLesson(lessonId)
                }
            )
        }
    }
}

@Composable
private fun LessonCard(
    index: Int,
    lesson: Lesson,
    isDone: Boolean,
    onToggleDone: () -> Unit
) {
    GlassCard(
        borderColor = if (isDone) AppColors.Lime.copy(alpha = 0.4f) else AppColors.BorderGlass
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index. ${lesson.title}",
                color = if (isDone) AppColors.Lime else AppColors.TextPrimary,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (isDone) {
                Text(
                    text = "הושלם ✓",
                    color = AppColors.Lime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = lesson.body,
            color = AppColors.TextSecondary,
            fontSize = 13.5.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.CardElevated)
                .border(1.dp, AppColors.BorderGlass, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "משימת ביצוע מעשית:",
                    color = AppColors.Amber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = lesson.task,
                    color = AppColors.TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (!isDone) {
            Button(
                onClick = onToggleDone,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Lime.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Lime.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "ביצעתי את המשימה ✓",
                    color = AppColors.Lime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
