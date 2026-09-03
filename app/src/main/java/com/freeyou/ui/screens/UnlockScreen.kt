package com.freeyou.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.freeyou.data.BlockRepo
import com.freeyou.ui.components.GlassCard
import com.freeyou.ui.theme.AppColors

@Composable
fun UnlockScreen(
    onNavigate: (String) -> Unit
) {
    val state by BlockRepo.state.collectAsState()
    var selectedMethod by remember { mutableStateOf(0) } // 0 = Pledge, 1 = Partner Code, 2 = Timer

    val targetPledge = "אני מבקש לפתוח את החומה ואני לוקח אחריות מלאה על מה שיקרה אחר כך"
    var typedPledge by remember { mutableStateOf("") }
    var typedPartnerCode by remember { mutableStateOf("") }
    var partnerError by remember { mutableStateOf<String?>(null) }

    fun doUnlock() {
        BlockRepo.setStrict(false)
        onNavigate("shield")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 60.dp)
    ) {
        item {
            Text(
                text = "שחרור ממצב קפדני",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "מצב קפדני נועד למנוע החלטה אימפולסיבית ברגע של חולשה. בחר מנגנון שחרור מודע.",
                fontSize = 13.5.sp,
                color = AppColors.TextSecondary,
                lineHeight = 20.sp
            )
        }

        // Method selector tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.CardSurface)
                    .border(1.dp, AppColors.BorderGlass, RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                listOf("הצהרת כוונה", "קוד שותף", "המתנה 60 דק'").forEachIndexed { i, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedMethod == i) AppColors.Violet else Color.Transparent)
                            .clickable { selectedMethod = i }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selectedMethod == i) Color.White else AppColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Method 0: Exact Pledge Typing
        if (selectedMethod == 0) {
            item {
                GlassCard(borderColor = AppColors.Amber.copy(alpha = 0.3f)) {
                    Text(
                        text = "הקלד את המשפט הבא במדויק:",
                        color = AppColors.Amber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"$targetPledge\"",
                        color = AppColors.TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = typedPledge,
                        onValueChange = { typedPledge = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("הקלד כאן בדיוק...", fontSize = 13.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (typedPledge.trim() == targetPledge) AppColors.Lime else AppColors.Amber,
                            unfocusedBorderColor = AppColors.BorderGlass
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val matches = typedPledge.trim() == targetPledge
                    Button(
                        onClick = { if (matches) doUnlock() },
                        enabled = matches,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Rose,
                            disabledContainerColor = AppColors.CardElevated
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (matches) "אשר ביטול מצב קפדני" else "המשפט אינו תואם עדיין",
                            color = if (matches) Color.White else AppColors.TextTertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Method 1: Partner Code
        if (selectedMethod == 1) {
            item {
                GlassCard(borderColor = AppColors.Cyan.copy(alpha = 0.3f)) {
                    Text(
                        text = "הזן קוד שותף למסע:",
                        color = AppColors.Cyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "אם הגדרת שותף, הוא מחזיק בקוד השחרור.",
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = typedPartnerCode,
                        onValueChange = {
                            typedPartnerCode = it.uppercase()
                            partnerError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("הזן קוד בן 6 תווים", fontSize = 13.sp) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (partnerError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = partnerError!!, color = AppColors.Rose, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (state.partnerCode.isNotBlank() && typedPartnerCode.trim() == state.partnerCode) {
                                doUnlock()
                            } else {
                                partnerError = "הקוד שגוי או שטרם הוגדר שותף במסך 'אני'."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Cyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("אמת ושחרר", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Method 2: 60-Minute Cooling-off
        if (selectedMethod == 2) {
            item {
                GlassCard {
                    Text(
                        text = "המתנה מקררת של 60 דקות",
                        color = AppColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "הפעל טיימר של שעה. מחקרים מראים כי 95% מהדחפים נעלמים לחלוטין לאחר 30-45 דקות.",
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onNavigate("shield") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Violet),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("התחל המתנה וחזור להגנה", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { onNavigate("shield") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("התחרטתי, השאר את החומה פעילה ✓", color = AppColors.Lime)
            }
        }
    }
}
