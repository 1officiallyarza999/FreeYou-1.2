import re

with open("app/src/main/java/com/freeyou/ui/screens/ShieldScreen.kt", "r") as f:
    code = f.read()

# Add imports
imports = """
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.freeyou.vpn.ShieldVpnService
"""

code = code.replace("import kotlinx.coroutines.withContext", "import kotlinx.coroutines.withContext\n" + imports)

# Add VPN state inside ShieldScreen
state_vars = """
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
"""

code = code.replace("    var hasAccessibility by remember { mutableStateOf(checkAccessibility()) }", state_vars + "\n    var hasAccessibility by remember { mutableStateOf(checkAccessibility()) }")

# Add the VPN UI card
vpn_ui = """
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
"""

code = code.replace("// Permissions banner", vpn_ui + "\n        // Permissions banner")

with open("app/src/main/java/com/freeyou/ui/screens/ShieldScreen.kt", "w") as f:
    f.write(code)

