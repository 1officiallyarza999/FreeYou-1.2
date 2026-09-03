import re

with open("app/src/main/java/com/freeyou/ui/screens/MentorScreen.kt", "r") as f:
    code = f.read()

# Update imports
new_imports = """
import com.freeyou.ai.MentorManager
import com.freeyou.ai.ChatMessage
"""
code = code.replace("import com.freeyou.data.MentorLines", new_imports)

# The entire MentorScreen body will be rebuilt to use MentorManager. 
# We'll replace everything after @Composable fun MentorScreen(
new_body = """
    val context = LocalContext.current
    val state by BlockRepo.state.collectAsState()
    
    // Instantiate MentorManager once per screen lifecycle
    val mentorManager = remember { MentorManager(context).apply { activeMode = state.mentorMode } }
    
    DisposableEffect(Unit) {
        onDispose {
            mentorManager.destroy()
        }
    }

    val messages by mentorManager.messages.collectAsState()
    val isListening by mentorManager.isListening.collectAsState()
    val isProcessing by mentorManager.isProcessing.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    var speechEnabled by remember { mutableStateOf(mentorManager.speechEnabled) }
    var activeMode by remember { mutableStateOf(mentorManager.activeMode) }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            mentorManager.startListening()
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "היצר הטוב שלך",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = if (isListening) "מקשיב לקולך כעת..." else if (isProcessing) "חושב..." else "קול פנימי חכם, רגוע וחזק",
                    fontSize = 12.sp,
                    color = if (isListening) AppColors.Amber else if (isProcessing) AppColors.Lime else AppColors.Cyan
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (speechEnabled) "🔊 קול" else "🔇 שקט",
                    fontSize = 12.sp,
                    color = AppColors.TextTertiary,
                    modifier = Modifier
                        .clickable { 
                            speechEnabled = !speechEnabled
                            mentorManager.speechEnabled = speechEnabled
                        }
                        .padding(4.dp)
                )
            }
        }

        // Personality Mode Selector (Compassion, Coach, Warrior)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.CardSurface)
                .border(1.dp, AppColors.BorderGlass, RoundedCornerShape(14.dp))
                .padding(3.dp)
        ) {
            val modes = listOf(
                Triple("coach", "🎯 מאמן", AppColors.Violet),
                Triple("warrior", "⚔️ לוחם", AppColors.AmberGlow),
                Triple("compassion", "🕊️ חמלה", AppColors.Cyan)
            )

            modes.forEach { (modeId, label, color) ->
                val isSelected = activeMode == modeId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) color else Color.Transparent)
                        .clickable {
                            activeMode = modeId
                            mentorManager.activeMode = modeId
                            BlockRepo.setMentorMode(modeId)
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) (if (modeId == "warrior") Color.Black else Color.White) else AppColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        AudioWaveformVisual(isListening = isListening)

        // Chat messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg = msg, mode = activeMode)
            }
        }

        // Quick Contextual Prompt Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SuggestionChip(
                label = "עובר עליי דחף",
                onClick = { mentorManager.sendMessage("עובר עליי דחף חזק עכשיו") }
            )
            SuggestionChip(
                label = "למה התחלתי?",
                onClick = { mentorManager.sendMessage("תזכיר לי למה התחלתי את המסע הזה") }
            )
            SuggestionChip(
                label = "עייפות ולחץ",
                onClick = { mentorManager.sendMessage("אני מרגיש עייף ולחוץ מאוד") }
            )
        }

        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("דבר או כתוב ליצר הטוב שלך...", fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Violet,
                    unfocusedBorderColor = AppColors.BorderGlass
                ),
                enabled = !isProcessing
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Mic button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isListening) AppColors.Amber else AppColors.CardElevated)
                    .border(
                        1.dp,
                        if (isListening) AppColors.Amber else AppColors.BorderGlass,
                        CircleShape
                    )
                    .clickable(enabled = !isProcessing) {
                        if (isListening) {
                            mentorManager.stopListening()
                        } else {
                            recordAudioLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎙️", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Send button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isProcessing) AppColors.CardElevated else AppColors.Violet)
                    .clickable(enabled = !isProcessing) { 
                        mentorManager.sendMessage(inputText)
                        inputText = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AppColors.Violet, strokeWidth = 2.dp)
                } else {
                    Text(text = "➤", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}
"""

start_index = code.find("val context = LocalContext.current")
end_index = code.find("@Composable\nprivate fun ChatBubble")

final_code = code[:start_index] + new_body + code[end_index:]

# Fix ChatBubble ChatMsg parameter
final_code = final_code.replace("private fun ChatBubble(msg: ChatMsg", "private fun ChatBubble(msg: ChatMessage")
final_code = final_code.replace("data class ChatMsg(val isMentor: Boolean, val text: String)", "")

with open("app/src/main/java/com/freeyou/ui/screens/MentorScreen.kt", "w") as f:
    f.write(final_code)
