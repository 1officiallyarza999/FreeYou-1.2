package com.freeyou

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeyou.data.BlockRepo
import com.freeyou.ui.components.MeshBackground
import com.freeyou.ui.components.TopBarHeader
import com.freeyou.ui.screens.*
import com.freeyou.ui.theme.AppColors
import com.freeyou.ui.theme.FreeYouTheme

class MainActivity : ComponentActivity() {

    private var currentRoute = mutableStateOf("home")
    private var routeTarget = mutableStateOf("")
    private var routeCount = mutableStateOf(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        BlockRepo.init(this)
        MentorOverlay.init(this)

        handleIntent(intent)

        setContent {
            FreeYouTheme {
                val state by BlockRepo.state.collectAsState()
                var activeTab by remember { mutableStateOf("home") }
                val route by currentRoute
                val target by routeTarget
                val count by routeCount

                fun isAccessibilityActive(): Boolean {
                    val enabled = Settings.Secure.getString(
                        contentResolver,
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                    ) ?: ""
                    return enabled.contains("com.freeyou")
                }

                fun navigateTo(destination: String) {
                    when (destination) {
                        "home", "shield", "mentor", "grow", "me" -> {
                            activeTab = destination
                            currentRoute.value = destination
                        }
                        else -> {
                            currentRoute.value = destination
                        }
                    }
                }

                BackHandler(enabled = route !in listOf("home", "shield", "mentor", "grow", "me")) {
                    currentRoute.value = activeTab
                }

                MeshBackground {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars),
                        containerColor = Color.Transparent,
                        topBar = {
                            if (route in listOf("home", "shield", "mentor", "grow", "me")) {
                                TopBarHeader(
                                    isStrict = state.strict,
                                    isShieldActive = isAccessibilityActive(),
                                    onVoiceClick = { navigateTo("mentor") }
                                )
                            }
                        },
                        bottomBar = {
                            if (route in listOf("home", "shield", "mentor", "grow", "me")) {
                                BottomGlassBar(
                                    activeTab = activeTab,
                                    onSelectTab = { tab ->
                                        activeTab = tab
                                        currentRoute.value = tab
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (route) {
                                "home" -> HomeScreen(onNavigate = { navigateTo(it) })
                                "shield" -> ShieldScreen(onNavigate = { navigateTo(it) })
                                "mentor" -> MentorScreen(onNavigate = { navigateTo(it) })
                                "grow" -> GrowScreen(onNavigate = { navigateTo(it) })
                                "me" -> MeScreen(onNavigate = { navigateTo(it) })
                                "intercept" -> InterceptScreen(
                                    target = target,
                                    count = count,
                                    onNavigate = { navigateTo(it) }
                                )
                                "journal" -> JournalScreen(onNavigate = { navigateTo(it) })
                                "mission" -> MissionScreen(onNavigate = { navigateTo(it) })
                                "sos" -> SosScreen(onNavigate = { navigateTo(it) })
                                "unlock" -> UnlockScreen(onNavigate = { navigateTo(it) })
                                "urge_flow" -> UrgeFlowScreen(onNavigate = { navigateTo(it) })
                                "progress" -> ProgressScreen(onNavigate = { navigateTo(it) })
                                else -> HomeScreen(onNavigate = { navigateTo(it) })
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val r = intent?.getStringExtra("freeyou_route")
        val t = intent?.getStringExtra("freeyou_target") ?: ""
        val c = intent?.getIntExtra("freeyou_count", 1) ?: 1

        if (!r.isNullOrBlank()) {
            currentRoute.value = r
            routeTarget.value = t
            routeCount.value = c
            return
        }

        val pending = BlockRepo.pendingRoute()
        if (pending != null) {
            currentRoute.value = pending.first
            routeTarget.value = pending.second
            routeCount.value = pending.third
            BlockRepo.clearPendingRoute()
        }
    }
}

@Composable
private fun BottomGlassBar(
    activeTab: String,
    onSelectTab: (String) -> Unit
) {
    val items = listOf(
        Triple("home", "הבית", "🔥"),
        Triple("shield", "חוסם", "🛡️"),
        Triple("mentor", "מנטור", "🎙️"),
        Triple("grow", "בנייה", "📈"),
        Triple("me", "אני", "⚙️")
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(AppColors.CardSurface)
                .border(1.dp, AppColors.BorderGlass, RoundedCornerShape(26.dp))
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (tabKey, label, icon) ->
                val isSelected = activeTab == tabKey
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) AppColors.Violet.copy(alpha = 0.22f) else Color.Transparent)
                        .clickable { onSelectTab(tabKey) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = icon,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) AppColors.TextPrimary else AppColors.TextTertiary
                    )
                }
            }
        }
    }
}
