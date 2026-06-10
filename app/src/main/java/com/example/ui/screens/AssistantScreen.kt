package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.voice.VoiceManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AssistantScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val chatHistory by viewModel.assistantChatHistory.collectAsState()
    val scrollState = rememberLazyListState()

    var inputMessage by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var audioPermissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)

    // Voice Manager setup
    var voiceManager by remember { mutableStateOf<VoiceManager?>(null) }
    var readAloudEnabled by remember { mutableStateOf(true) } // TTS response default enabled

    // Initialize Voice Manager on launch
    DisposableEffect(Unit) {
        val manager = VoiceManager(
            context = context,
            onResults = { text ->
                inputMessage = text
                isListening = false
                // Auto send to Gemini once voice inputs resolve! (Voice -> Ask -> Get Answer)
                if (text.isNotBlank()) {
                    viewModel.sendQuestionToAssistant(text)
                    inputMessage = ""
                }
            },
            onPartialResults = { text ->
                inputMessage = text
            },
            onError = { err ->
                isListening = false
                Log.e("AssistantScreen", "Voice mistake: $err")
            }
        )
        voiceManager = manager

        onDispose {
            manager.destroy()
        }
    }

    // Capture newest message and speak aloud
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            // Scroll to the bottom of the chat dynamically
            scrollState.animateScrollToItem(chatHistory.size - 1)
            
            // TTS speak if the last response is from AI and readAloud is enabled
            val lastMsg = chatHistory.last()
            if (lastMsg.sender == "AI" && !lastMsg.loading && readAloudEnabled) {
                voiceManager?.speak(lastMsg.message)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // AI Assistant top stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DyeChem Warehouse AI Bot",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Powered by Gemini 2.0 • Reads live SQLite Room state",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            Row {
                // Speech Read-Aloud Toggle
                IconButton(
                    onClick = {
                        readAloudEnabled = !readAloudEnabled
                        if (!readAloudEnabled) {
                            voiceManager?.stopSpeaking()
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (readAloudEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    Icon(
                        imageVector = if (readAloudEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Read Aloud"
                    )
                }

                // Clear Chat History
                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = Color.Red.copy(alpha = 0.7f))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Conversation pane
        LazyColumn(
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    RoundedCornerShape(12.dp)
                )
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            items(chatHistory) { message ->
                ChatBubble(message)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Message input compose panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Speech Input Microphone (Accompanied by record permission request)
            IconButton(
                onClick = {
                    if (audioPermissionState.status.isGranted) {
                        if (isListening) {
                            voiceManager?.stopListening()
                            isListening = false
                        } else {
                            voiceManager?.stopSpeaking() // stop TTS before listening
                            voiceManager?.startListening()
                            isListening = true
                        }
                    } else {
                        audioPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (isListening) Color.Red else MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (isListening) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Speak Input"
                )
            }

            // Text Entry Field
            OutlinedTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                placeholder = { Text("Ask: Hydrogen Peroxide কত আছে?") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_input_field"),
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    if (inputMessage.isNotEmpty()) {
                        IconButton(onClick = { inputMessage = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                }
            )

            // Submit Question Button
            IconButton(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        voiceManager?.stopSpeaking() // silence active TTS when sending new input
                        viewModel.sendQuestionToAssistant(inputMessage)
                        inputMessage = ""
                    }
                },
                enabled = inputMessage.isNotBlank(),
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (inputMessage.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Message")
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.sender == "User"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 12.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (msg.loading) {
                    Box(modifier = Modifier.padding(6.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    }
                } else {
                    Text(
                        text = msg.message,
                        fontSize = 13.sp,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

private object Log {
    fun e(tag: String, msg: String) {
        android.util.Log.e(tag, msg)
    }
}
