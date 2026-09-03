package com.freeyou

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.freeyou.data.BlockRepo
import com.freeyou.ui.components.MeshBackground
import com.freeyou.ui.components.TopBarHeader
import com.freeyou.ui.screens.*
import com.freeyou.ui.theme.AppColors
import com.freeyou.ui.theme.FreeYouTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val _navEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navEvents = _navEvents.asSharedFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        BlockRepo.init(this)
        MentorOverlay.init(this)

        handleIntent(intent)

        setContent {
            FreeYouTheme {
                val state by BlockRepo.state.collectAsState()
                val navController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()
                
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route?.substringBefore("?") ?: "home"

                LaunchedEffect(Unit) {
                    navEvents.collect { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                }

                fun isAccessibilityActive(): Boolean {
                    val enabled = Settings.Secure.getString(
                        contentResolver,
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                    ) ?: ""
                    return enabled.contains("com.freeyou")
                }

                MeshBackground {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars),
                        containerColor = Color.Transparent,
                        topBar = {
                            if (currentRoute in listOf("home", "shield", "mentor", "grow", "me")) {
                                TopBarHeader(
                                    isStrict = state.strict,
                                    isShieldActive = isAccessibilityActive(),
                                    onVoiceClick = { navController.navigate("mentor") }
                                )
                            }
                        },
                        bottomBar = {
                            if (currentRoute in listOf("home", "shield", "mentor", "grow", "me")) {
                                BottomGlassBar(
                                    activeTab = currentRoute,
                                    onSelectTab = { tab ->
                                        navController.navigate(tab) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
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
                            NavHost(navController = navController, startDestination = "home") {
                                val navigateAction: (String) -> Unit = { route ->
                                    if (route == "back") {
                                        navController.popBackStack()
                                    } else {
                                        navController.navigate(route) {
                                            if (route in listOf("home", "shield", "mentor", "grow", "me")) {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            } else {
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                }
                                composable("home") { HomeScreen(onNavigate = navigateAction) }
                                composable("shield") { ShieldScreen(onNavigate = navigateAction) }
                                composable("mentor") { MentorScreen(onNavigate = navigateAction) }
                                composable("grow") { GrowScreen(onNavigate = navigateAction) }
                                composable("me") { MeScreen(onNavigate = navigateAction) }
                                
                                composable(
                                    route = "intercept?target={target}&count={count}",
                                    arguments = listOf(
                                        navArgument("target") { type = NavType.StringType; defaultValue = "" },
                                        navArgument("count") { type = NavType.IntType; defaultValue = 1 }
                                    )
                                ) { backStackEntry ->
                                    val target = backStackEntry.arguments?.getString("target") ?: ""
                                    val count = backStackEntry.arguments?.getInt("count") ?: 1
                                    InterceptScreen(target = target, count = count, onNavigate = navigateAction)
                                }
                                
                                composable("journal") { JournalScreen(onNavigate = navigateAction) }
                                composable("mission") { MissionScreen(onNavigate = navigateAction) }
                                composable("sos") { SosScreen(onNavigate = navigateAction) }
                                composable("unlock") { UnlockScreen(onNavigate = navigateAction) }
                                composable("urge_flow") { UrgeFlowScreen(onNavigate = navigateAction) }
                                composable("progress") { ProgressScreen(onNavigate = navigateAction) }
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
            val route = if (r == "intercept") "intercept?target=$t&count=$c" else r
            _navEvents.tryEmit(route)
            return
        }

        val pending = BlockRepo.pendingRoute()
        if (pending != null) {
            val pendingRoute = if (pending.first == "intercept") "intercept?target=${pending.second}&count=${pending.third}" else pending.first
            _navEvents.tryEmit(pendingRoute)
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
                    Text(text = icon, fontSize = 18.sp)
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
