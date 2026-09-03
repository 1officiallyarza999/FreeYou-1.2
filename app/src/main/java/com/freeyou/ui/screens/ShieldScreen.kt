package com.freeyou.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.freeyou.data.BlockRepo
import com.freeyou.data.InstalledAppItem
import com.freeyou.ui.components.GlassCard
import com.freeyou.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.freeyou.vpn.ShieldVpnService


@Composable
fun ShieldScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by BlockRepo.state.collectAsState()

    var newSiteInput by remember { mutableStateOf("") }
    var updateMsg by remember { mutableStateOf<String?>(null) }
    var isUpdating by remember { mutableStateOf(false) }

    var appSearchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("הכל") }
    var installedApps by remember { mutableStateOf<List<InstalledAppItem>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(false) }
    var showAppsPicker by remember { mutableStateOf(false) }

    fun checkAccessibility(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        return enabledServices.contains("com.freeyou")
    }


    var isVpnActive by remember { mutableStateOf(false) }
    
    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val intent = Intent(context, ShieldVpnService::class.java)
            context.startService(intent)
            isVpnActive = true
        }
    }
    
    fun toggleVpn() {
        if (isVpnActive) {
            val intent = Intent(context, ShieldVpnService::class.java).apply {
                action = "STOP"
            }
            context.startService(intent)
            isVpnActive = false
        } else {
            val vpnIntent = VpnService.prepare(context)
            if (vpnIntent != null) {
                vpnLauncher.launch(vpnIntent)
            } else {
                val intent = Intent(context, ShieldVpnService::class.java)
                context.startService(intent)
                isVpnActive = true
            }
        }
    }

    var hasAccessibility by remember { mutableStateOf(checkAccessibility()) }
    var hasOverlay by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    // Load installed apps when user expands the picker
    LaunchedEffect(showAppsPicker) {
        if (showAppsPicker && installedApps.isEmpty()) {
            isLoadingApps = true
            withContext(Dispatchers.IO) {
                val apps = BlockRepo.getInstalledAppsList()
                installedApps = apps
            }
            isLoadingApps = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
    ) {
        item {
            Text(
                text = "החומה וההגנות (Shield)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.TextPrimary
            )
            Text(
                text = "חסימת אתרים ואפליקציות ברמת המכשיר • עיבוד מקומי ללא ריגול",
                fontSize = 13.5.sp,
                color = AppColors.TextSecondary
            )
        }

        
        // VPN / DNS Filter Banner
        item {
            GlassCard(
                borderColor = if (isVpnActive) AppColors.Lime.copy(alpha = 0.5f) else AppColors.Cyan.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "מנוע סינון DNS עמוק (VPN)",
                            color = AppColors.TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "חסימת אתרי מבוגרים ברמת הרשת (DNS Interception) ללא השפעה על המהירות.",
                            color = AppColors.TextTertiary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Switch(
                        checked = isVpnActive,
                        onCheckedChange = { toggleVpn() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.Lime,
                            checkedTrackColor = AppColors.Lime.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }

        // Permissions banner
        item {
            GlassCard(
                borderColor = if (hasAccessibility && hasOverlay) AppColors.Lime.copy(alpha = 0.35f)
                else AppColors.Rose.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasAccessibility) "שירות הנגישות פעיל ✓" else "שירות הנגישות כבוי ⚠️",
                            color = if (hasAccessibility) AppColors.Lime else AppColors.Rose,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "מאפשר לזהות אתרים ואפליקציות חסומות עוד לפני טעינת תוכן",
                            color = AppColors.TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                    if (!hasAccessibility) {
                        Button(
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Rose),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("הפעל", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = AppColors.BorderGlass)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasOverlay) "חלון מגן מעל אפליקציות ✓" else "הרשאת חלון צף חסרה",
                            color = if (hasOverlay) AppColors.Lime else AppColors.Amber,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "מציג את מסך ההתערבות והמנטור ברגע העצירה מעל דפדפנים",
                            color = AppColors.TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                    if (!hasOverlay) {
                        Button(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Amber),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("אשר", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }

        // Adult & Feeds Block Toggles
        item {
            GlassCard {
                // Block 18+
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "חסימת תכני מבוגרים ודחפים (Block 18+)",
                            color = AppColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "חסימה מוחלטת של Pornhub, Xvideos, OnlyFans, אתרי סקס, וסריקת טריגרים ברשתות (X, TikTok)",
                            color = AppColors.Cyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = state.autoAdult,
                        onCheckedChange = { BlockRepo.setAutoAdult(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.Cyan,
                            checkedTrackColor = AppColors.Cyan.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = updateMsg ?: "רשימת דומיינים חתומה ומעודכנת מקומית",
                        color = AppColors.TextSecondary,
                        fontSize = 11.5.sp
                    )
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isUpdating = true
                                updateMsg = "מעדכן רשימת דומיינים..."
                                try {
                                    val count = withContext(Dispatchers.IO) {
                                        BlockRepo.updateBlocklist("https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/porn/hosts")
                                    }
                                    updateMsg = "עודכנו $count דומיינים בהצלחה ✓"
                                } catch (_: Exception) {
                                    updateMsg = "לא הצליח להוריד, הרשימה המקומית שמורה"
                                } finally {
                                    isUpdating = false
                                }
                            }
                        },
                        enabled = !isUpdating,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isUpdating) "מעדכן..." else "עדכן רשימה", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = AppColors.BorderGlass)
                Spacer(modifier = Modifier.height(14.dp))

                // Feeds blocker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "חסימת גלילה אינסופית (Shorts/Reels)",
                            color = AppColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "עצירת פידים קצרים ב-TikTok, Instagram, YouTube",
                            color = AppColors.TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = state.autoScroll,
                        onCheckedChange = { BlockRepo.setAutoScroll(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.Cyan,
                            checkedTrackColor = AppColors.Cyan.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = AppColors.BorderGlass)
                Spacer(modifier = Modifier.height(14.dp))

                // Strict Mode Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "מצב קפדני (Strict Mode)",
                            color = if (state.strict) AppColors.Rose else AppColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "דורש מנגנון שחרור מורכב לביטול (קוד שותף או הצהרה)",
                            color = AppColors.TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = state.strict,
                        onCheckedChange = { enabled ->
                            if (!enabled) {
                                onNavigate("unlock")
                            } else {
                                BlockRepo.setStrict(true)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.Rose,
                            checkedTrackColor = AppColors.Rose.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }

        // Installed Apps Selector Section
        item {
            GlassCard(
                borderColor = AppColors.VioletSoft.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "חוסם אפליקציות מותקנות",
                            color = AppColors.TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.blocked.size} פריטים ואפליקציות חסומים",
                            color = AppColors.TextTertiary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { showAppsPicker = !showAppsPicker },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showAppsPicker) AppColors.CardElevated else AppColors.Violet
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (showAppsPicker) "סגור רשימה" else "בחר אפליקציות", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (showAppsPicker) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Search field
                    OutlinedTextField(
                        value = appSearchQuery,
                        onValueChange = { appSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("חפש שם אפליקציה...", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val categories = listOf("הכל", "רשתות", "דפדפנים", "בידור")
                        items(categories) { cat ->
                            val isSel = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) AppColors.Violet else AppColors.CardElevated)
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSel) Color.White else AppColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoadingApps) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AppColors.Cyan)
                        }
                    } else {
                        val filteredApps = installedApps.filter { app ->
                            val matchesSearch = app.appName.contains(appSearchQuery, ignoreCase = true) ||
                                    app.packageName.contains(appSearchQuery, ignoreCase = true)
                            val matchesCategory = when (selectedCategory) {
                                "רשתות" -> app.category.contains("רשתות")
                                "דפדפנים" -> app.category.contains("דפדפנים")
                                "בידור" -> app.category.contains("בידור")
                                else -> true
                            }
                            matchesSearch && matchesCategory
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredApps.take(20).forEach { app ->
                                val isBlocked = state.blocked.any { app.packageName.contains(it) || it.contains(app.packageName) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppColors.CardElevated)
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(if (isBlocked) AppColors.Rose.copy(alpha = 0.2f) else AppColors.Cyan.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = app.appName.take(1),
                                                fontWeight = FontWeight.Black,
                                                color = if (isBlocked) AppColors.Rose else AppColors.Cyan
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = app.appName,
                                                color = AppColors.TextPrimary,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = app.packageName,
                                                color = AppColors.TextTertiary,
                                                fontSize = 10.5.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = isBlocked,
                                        onCheckedChange = {
                                            BlockRepo.toggleBlockedApp(app.packageName)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = AppColors.Rose,
                                            checkedTrackColor = AppColors.Rose.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Manual blacklist
        item {
            GlassCard {
                Text(
                    text = "רשימת דומיינים אישית",
                    color = AppColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "הוסף אתר שמבזבז לך את הזמן (לדוגמה: reddit.com)",
                    color = AppColors.TextTertiary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSiteInput,
                        onValueChange = { newSiteInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("הזן כתובת אתר...", fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Violet,
                            unfocusedBorderColor = AppColors.BorderGlass
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newSiteInput.isNotBlank()) {
                                BlockRepo.addBlocked(newSiteInput.trim())
                                newSiteInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Violet),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("הוסף", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (state.blocked.isEmpty()) {
                    Text(
                        text = "אין עדיין אתרים ברשימה האישית.",
                        color = AppColors.TextTertiary,
                        fontSize = 13.sp
                    )
                } else {
                    state.blocked.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• $item",
                                color = AppColors.TextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "מחק",
                                color = AppColors.Rose,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { BlockRepo.removeBlocked(index) }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Emergency Safe Allowlist notice
        item {
            GlassCard(borderColor = AppColors.Lime.copy(alpha = 0.25f)) {
                Text(
                    text = "אפליקציות חירום מוגנות תמיד (Emergency Safe)",
                    color = AppColors.Lime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "טלפון, שיחות חירום, וואטסאפ, ניווט (Waze/Maps), בנקאות ואותנטיקטור לעולם אינם נחסמים.",
                    color = AppColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // Demo test button
        item {
            Button(
                onClick = { onNavigate("intercept") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.CardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Amber.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "בדוק את מסך ההתערבות (Demo)",
                    color = AppColors.Amber,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
