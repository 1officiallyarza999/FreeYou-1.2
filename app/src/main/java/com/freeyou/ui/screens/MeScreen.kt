package com.freeyou.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeyou.AdminReceiver
import com.freeyou.data.BlockRepo
import com.freeyou.ui.components.GlassCard
import com.freeyou.ui.theme.AppColors

@Composable
fun MeScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val state by BlockRepo.state.collectAsState()

    var showAddReasonDialog by remember { mutableStateOf(false) }
    var newReasonText by remember { mutableStateOf("") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val dpm = remember { context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager }
    val comp = remember { ComponentName(context, AdminReceiver::class.java) }
    var isAdminActive by remember { mutableStateOf(dpm.isAdminActive(comp)) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
    ) {
        item {
            Text(
                text = "אני והגדרות אישיות",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.TextPrimary
            )
            Text(
                text = "הסיבות שלך, אבטחת המכשיר והיסטוריית הכתיבה",
                fontSize = 14.sp,
                color = AppColors.TextSecondary
            )
        }

        // Reasons manager
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
                        text = "הסיבות שלי (מדוע התחלתי)",
                        color = AppColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+ הוסף",
                        color = AppColors.Amber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { showAddReasonDialog = true }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "משפטים אלו מושמעים לך ברגע העצירה כדי לעורר את התודעה שלך",
                    color = AppColors.TextTertiary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                state.reasons.forEachIndexed { idx, reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• $reason",
                            color = AppColors.TextSecondary,
                            fontSize = 13.5.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "מחק",
                            color = AppColors.Rose,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable { BlockRepo.removeReason(idx) }
                                .padding(6.dp)
                        )
                    }
                }
            }
        }

        // Night safe mode & Device admin
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "מצב לילה בטוח (23:00 - 05:00)",
                            color = AppColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "הלילה הוא שעת הפגיעות. משימת הגוף בלילה מותאמת לפרוטוקול ביתי",
                            color = AppColors.TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = state.nightSafe,
                        onCheckedChange = { BlockRepo.setNightSafe(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.Violet,
                            checkedTrackColor = AppColors.Violet.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = AppColors.BorderGlass)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "הגנת הסרה (מנהל מכשיר)",
                            color = if (isAdminActive) AppColors.Lime else AppColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "מונע מחיקה אימפולסיבית של FreeYou מתוך חולשה",
                            color = AppColors.TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isAdminActive,
                        onCheckedChange = { enable ->
                            if (enable) {
                                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp)
                                    putExtra(
                                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                        "FreeYou משתמש בהרשאה זו למניעת הסרה פזיזה ברגע דחף."
                                    )
                                }
                                context.startActivity(intent)
                            } else {
                                dpm.removeActiveAdmin(comp)
                                isAdminActive = false
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.Lime,
                            checkedTrackColor = AppColors.Lime.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }

        // Accountability Partner
        item {
            GlassCard(
                borderColor = AppColors.Cyan.copy(alpha = 0.25f)
            ) {
                Text(
                    text = "שותף למסע (Accountability Partner)",
                    color = AppColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "קוד בן 6 תווים הידוע רק לשותף או מנטור. נדרש לשחרור מהיר של מצב קפדני",
                    color = AppColors.TextTertiary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = state.partnerCode,
                    onValueChange = { BlockRepo.setPartnerCode(it.uppercase()) },
                    placeholder = { Text("לדוגמה: BR749K", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Cyan,
                        unfocusedBorderColor = AppColors.BorderGlass
                    )
                )
            }
        }

        // Journal entries preview
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "יומנים שנכתבו (${state.journals.size})",
                        color = AppColors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "פתח יומן",
                        color = AppColors.Amber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigate("journal") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (state.journals.isEmpty()) {
                    Text(
                        text = "טרם נכתבו יומנים. כל כתיבה ברגע עצירה נשמרת כאן.",
                        color = AppColors.TextTertiary,
                        fontSize = 12.sp
                    )
                } else {
                    state.journals.takeLast(3).reversed().forEach { j ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.CardElevated)
                                .padding(8.dp)
                        ) {
                            Text(text = j.date, color = AppColors.Amber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = j.text, color = AppColors.TextSecondary, fontSize = 12.sp, maxLines = 2)
                        }
                    }
                }
            }
        }

        // Reset streak
        item {
            Button(
                onClick = { showResetConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Rose.copy(alpha = 0.15f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Rose.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "איפוס ספירה כן (התחלתי מחדש)",
                    color = AppColors.Rose,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showAddReasonDialog) {
        AlertDialog(
            onDismissRequest = { showAddReasonDialog = false },
            title = { Text("הוסף סיבה אישית", color = AppColors.TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newReasonText,
                    onValueChange = { newReasonText = it },
                    placeholder = { Text("משפט אחד חזק שמזכיר לך...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newReasonText.isNotBlank()) {
                            BlockRepo.addReason(newReasonText.trim())
                            newReasonText = ""
                            showAddReasonDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Amber)
                ) {
                    Text("שמור", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReasonDialog = false }) {
                    Text("ביטול", color = AppColors.TextSecondary)
                }
            },
            containerColor = AppColors.CardSurface
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("איפוס ספירת הימים", color = AppColors.TextPrimary) },
            text = {
                Text(
                    text = "אחי, אין כאן שיפוטיות. כנות היא התנאי הראשון לחופש. הנפילה לא מוחקת את השריר שבנית — אנחנו פשוט מתחילים שוב מהיום הראשון.",
                    color = AppColors.TextSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        BlockRepo.resetStreak()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Rose)
                ) {
                    Text("אפס בכנות", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("ביטול", color = AppColors.TextSecondary)
                }
            },
            containerColor = AppColors.CardSurface
        )
    }
}
