package com.example.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.InventoryViewModel
import com.example.ui.theme.*
import java.util.*

// Model representing a single chat dialog bubble
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(viewModel: InventoryViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val lang by remember { derivedStateOf { viewModel.appLanguage.value } }

    val userInputText = remember { mutableStateOf("") }
    val isProcessing by remember { derivedStateOf { viewModel.isAiProcessing.value } }
    val currentAiResponse by remember { derivedStateOf { viewModel.aiResponseText.value } }
    val loggedUserQuery by remember { derivedStateOf { viewModel.aiInputText.value } }

    // List of chat messages to build a conversation layout
    val chatHistory = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()

    // Initialize Android's native Text-to-Speech system
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            } else {
                Toast.makeText(context, "TTS Initialization failed", Toast.LENGTH_SHORT).show()
            }
        }
        ttsInstance = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // Speak helper
    val speakText = { targetText: String ->
        ttsInstance?.let { tts ->
            if (isTtsReady) {
                // Set language locale dynamically based on actual content
                val hasBengali = targetText.any { it.code in 0x0980..0x09FF }
                val locale = if (hasBengali) Locale("bn", "BD") else Locale.US
                tts.language = locale
                tts.speak(targetText, TextToSpeech.QUEUE_FLUSH, null, "AiSpeechOutput")
            }
        }
    }

    // Trigger TTS and chat history logging when a new AI response arrives
    LaunchedEffect(currentAiResponse) {
        if (currentAiResponse.isNotEmpty()) {
            val lastMsg = chatHistory.lastOrNull()
            if (lastMsg == null || !lastMsg.isUser || lastMsg.text != loggedUserQuery) {
                // If query is not in chat, insert it first (this covers quick command clicks and voice direct inputs)
                if (loggedUserQuery.isNotEmpty() && (chatHistory.isEmpty() || chatHistory.last().text != loggedUserQuery)) {
                    chatHistory.add(ChatMessage(text = loggedUserQuery, isUser = true))
                }
            }
            chatHistory.add(ChatMessage(text = currentAiResponse, isUser = false))
            speakText(currentAiResponse)
        }
    }

    // Automatically scroll to the bottom of the chat list on new items
    LaunchedEffect(chatHistory.size, isProcessing) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    // Speech-to-Text Activity Result Launcher (Standard Android Intent)
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            if (spokenText.isNotEmpty()) {
                chatHistory.add(ChatMessage(text = spokenText, isUser = true))
                viewModel.executeAiAssistantQuery(spokenText)
            }
        }
    }

    // Recommended Bengali and English prompt commands for Store Keepers
    val recommendedPrompts = listOf(
        "Hydrogen Peroxide কত আছে?",
        "HP001 কোথায় আছে?",
        "Low Stock দেখাও",
        "Rack A-01 এ কী আছে?",
        "আজকে কত Stock Out হয়েছে?"
    )

    // Glowing animation transitions for visual AI assistant vibes
    val infiniteTransition = rememberInfiniteTransition(label = "AiRippleOuter")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AiPulseSize"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- 1. AI Assistant Header ---
        Card(
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("ai_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Navigate Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Sparkle / Glowing AI Brain Icon Container
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant Logo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (lang == AppLanguage.EN) "DyeChem Smart AI" else "ডাই-কেম স্মার্ট এআই",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (lang == AppLanguage.EN) "Store Assistant • Live SQLite Access" else "স্টোর এআই সহকারী • সরাসরি ইনভেন্টরি ট্র্যাকিং",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- 2. Scrollable Response Console Area ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (chatHistory.isEmpty()) {
                // Unoccupied Empty State Dashboard Guidance
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (lang == AppLanguage.EN) "Ask Me Anything" else "আমাকে যেকোনো প্রশ্ন করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (lang == AppLanguage.EN)
                            "I have complete real-time access to the local database list. Voice and text prompts are translated automatically."
                            else "স্টোরের যেকোনো লট, সর্বোচ্চ মজুদ, র‍্যাক লোকেশন অথবা আজকের স্টক আউট সম্পর্কে জিজ্ঞাসা করতে পারেন।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                // Interactive Chat dialog lists
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(chatHistory) { message ->
                        val alignment = if (message.isUser) Alignment.End else Alignment.Start
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = alignment
                        ) {
                            if (message.isUser) {
                                // User prompt Bubble tag
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp),
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Text(
                                        text = message.text,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                // AI Smart response card Bubble
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (MaterialTheme.colorScheme.primary == DarkPrimary) {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        } else {
                                            Color(0xFFF1F5F9)
                                        }
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = ColorGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (lang == AppLanguage.EN) "AI Store Expert" else "স্মার্ট স্টোর এআই",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = ColorGreen
                                                )
                                            }

                                            // Speak Audio replay button
                                            IconButton(
                                                onClick = { speakText(message.text) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.VolumeUp,
                                                    contentDescription = "Speak Response Again",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = message.text,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp,
                                            lineHeight = 20.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Blinking loading indicator when AI is generating answers
                    if (isProcessing) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)),
                                    modifier = Modifier.widthIn(max = 200.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val animateScale1 by infiniteTransition.animateFloat(
                                            initialValue = 0.3f,
                                            targetValue = 1f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(650, easing = FastOutSlowInEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "Dot1"
                                        )
                                        val animateScale2 by infiniteTransition.animateFloat(
                                            initialValue = 0.3f,
                                            targetValue = 1f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(650, delayMillis = 150, easing = FastOutSlowInEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "Dot2"
                                        )
                                        val animateScale3 by infiniteTransition.animateFloat(
                                            initialValue = 0.3f,
                                            targetValue = 1f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(650, delayMillis = 300, easing = FastOutSlowInEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "Dot3"
                                        )

                                        Box(modifier = Modifier.size(7.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = animateScale1), CircleShape))
                                        Box(modifier = Modifier.size(7.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = animateScale2), CircleShape))
                                        Box(modifier = Modifier.size(7.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = animateScale3), CircleShape))

                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (lang == AppLanguage.EN) "Formulating response..." else "বিশ্লেষণ করা হচ্ছে...",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Instant Recommended Command Deck (Horizontal Chips) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (lang == AppLanguage.EN) "Recommended Store Inquiries" else "প্রস্তাবিত এআই অনুসন্ধান সমূহ:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                recommendedPrompts.forEach { query ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .clickable {
                                chatHistory.add(ChatMessage(text = query, isUser = true))
                                viewModel.executeAiAssistantQuery(query)
                            }
                    ) {
                        Text(
                            text = query,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // --- 4. Interactive Command Input Panel (Text & Voice) ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Speech Input Mic Action Button
                Box(
                    modifier = Modifier.size(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing Ring for assistant listening mode
                    Box(
                        modifier = Modifier
                            .size(if (isProcessing) 48.dp else 42.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = if (isProcessing) 0.15f * pulseSize else 0.08f)
                            )
                    )

                    FloatingActionButton(
                        onClick = {
                            try {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lang == AppLanguage.BN) "bn-BD" else "en-US")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, if (lang == AppLanguage.EN) "Please ask your question..." else "অনুগ্রহ করে আপনার প্রশ্নটি বলুন...")
                                }
                                speechRecognizerLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Voice input not supported on this device", Toast.LENGTH_SHORT).show()
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("ai_mic_trigger")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input Button",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Text Field Question Editor
                TextField(
                    value = userInputText.value,
                    onValueChange = { userInputText.value = it },
                    placeholder = {
                        Text(
                            text = if (lang == AppLanguage.EN) "Query stock codes or locations..." else " স্টক কোড বা লোকেশন লিখুন...",
                            fontSize = 12.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        disabledContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        val trimmed = userInputText.value.trim()
                        if (trimmed.isNotEmpty()) {
                            chatHistory.add(ChatMessage(text = trimmed, isUser = true))
                            viewModel.executeAiAssistantQuery(trimmed)
                            userInputText.value = ""
                            focusManager.clearFocus()
                        }
                    }),
                    modifier = Modifier
                        .weight(1f)
                        .height(49.dp)
                        .testTag("ai_text_input")
                )

                // Ask Send Action Button
                IconButton(
                    onClick = {
                        val trimmed = userInputText.value.trim()
                        if (trimmed.isNotEmpty()) {
                            chatHistory.add(ChatMessage(text = trimmed, isUser = true))
                            viewModel.executeAiAssistantQuery(trimmed)
                            userInputText.value = ""
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .testTag("ai_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Text Question",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
