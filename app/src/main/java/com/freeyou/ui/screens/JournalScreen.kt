package com.freeyou.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import com.freeyou.data.BlockRepo
import com.freeyou.data.DataRepository
import com.freeyou.data.model.JournalEntry
import com.freeyou.ui.components.GlassCard
import com.freeyou.ui.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun JournalScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var text by remember { mutableStateOf("יומנו של גבר:\n") }
    val minChars = 180
    val currentLength = text.trim().length
    val isValid = currentLength >= minChars

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 60.dp)
    ) {
        item {
            Text(
                text = "יומנו של גבר",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "כתיבה היא הכלי החזק ביותר להעביר את הפיקוד מהמוח הזוחל לקליפת המוח הקדמית.",
                fontSize = 13.5.sp,
                color = AppColors.TextSecondary,
                lineHeight = 20.sp
            )
        }

        item {
            GlassCard(borderColor = AppColors.Amber.copy(alpha = 0.3f)) {
                Text(
                    text = "ארבע שאלות מנחות לכתיבה:",
                    color = AppColors.Amber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                val questions = listOf(
                    "1. מה בדיוק הרגשתי ברגעים שלפני הדחף? (שעמום, עייפות, בריחה ממשימה, בדידות)",
                    "2. מה באמת חיפשתי? איזה צורך אמיתי ניסיתי להשתיק עם דופמין מהיר?",
                    "3. מה המחיר שזה ייקח ממני מחר בבוקר — בעסק, במשפחה, בכבוד העצמי?",
                    "4. מה הדבר הקונקרטי האחד שאני הולך לעשות עכשיו בחצי השעה הקרובה במקום?"
                )
                questions.forEach { q ->
                    Text(
                        text = q,
                        color = AppColors.TextSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    if (it.startsWith("יומנו של גבר:")) {
                        text = it
                    } else {
                        text = "יומנו של גבר:\n" + it.removePrefix("יומנו של גבר:").trimStart()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isValid) AppColors.Lime else AppColors.Amber,
                    unfocusedBorderColor = AppColors.BorderGlass,
                    focusedTextColor = AppColors.TextPrimary,
                    unfocusedTextColor = AppColors.TextPrimary
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isValid) AppColors.Lime.copy(alpha = 0.15f) else AppColors.Amber.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isValid) "$currentLength תווים — מוכן לחתימה ✓" else "$currentLength / $minChars תווים מינימום",
                        color = if (isValid) AppColors.Lime else AppColors.Amber,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        if (isValid) {
                            coroutineScope.launch {
                                DataRepository.getInstance(context).insertJournalEntry(
                                    JournalEntry(
                                        notes = text,
                                        intensity = 5 // default for now, could be dynamic
                                    )
                                )
                                BlockRepo.recordUrgeOvercome() // Kept for legacy streak tracking
                                onNavigate("home")
                            }
                        }
                    },
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Amber,
                        disabledContainerColor = AppColors.CardElevated
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "שמור וחתום",
                        color = if (isValid) Color.Black else AppColors.TextTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
