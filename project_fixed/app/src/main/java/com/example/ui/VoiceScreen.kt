package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.InventoryViewModel
import com.example.ui.theme.*

@Composable
fun VoiceScreen(viewModel: InventoryViewModel) {
    val lang by remember { derivedStateOf { viewModel.appLanguage.value } }

    val userInput by remember { derivedStateOf { viewModel.voiceInputText.value } }
    val aiResponse by remember { derivedStateOf { viewModel.voiceResponseText.value } }
    val isEvaluating by remember { derivedStateOf { viewModel.isVoiceProcessing.value } }

    var speechPermMessage by remember { mutableStateOf("") }

    // Wave animations representing audio capture input
    val infiniteTransition = rememberInfiniteTransition(label = "VoiceWaveTransition")
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveScale1"
    )
    val waveScale2 by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveScale2"
    )

    // Help command templates
    val voiceHelpPrompts = listOf(
        "Red Dye কত আছে?",
        "Lot 100 কোথায় আছে?",
        "Low Stock দেখাও",
        "আজ কত Chemical ব্যবহার হয়েছে?",
        "সব Dye দেখাও"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Localization.get("voice_title", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = Localization.get("voice_sub", lang),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Live Voice visual console Card
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Interactive mic waves
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isEvaluating) {
                        // Wave 1
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ColorBlueAccent.copy(alpha = 0.2f * waveScale1))
                        )
                        // Wave 2
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f * waveScale2))
                        )
                    }

                    // Main mic icon
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (isEvaluating) ColorBlueAccent else MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .clickable {
                                // Default simulation triggers standard text helper search and response
                                viewModel.executeVoiceQuery("Red Dye কত আছে?")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEvaluating) Icons.Default.Mic else Icons.Default.MicNone,
                            contentDescription = "Mic Trigger Button",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = Localization.get("say_something", lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center
                )

                // Dialog and Output response area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (userInput.isNotEmpty()) {
                        Row {
                            Text(
                                text = if (lang == AppLanguage.EN) "You spoke: " else "আপনি বললেন: ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(text = "\"$userInput\"", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    if (isEvaluating) {
                        Text(
                            text = Localization.get("voice_thinking", lang),
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = ColorOrange,
                            fontSize = 11.sp
                        )
                    } else if (aiResponse.isNotEmpty()) {
                        Text(
                            text = if (lang == AppLanguage.EN) "AI Assistant: " else "সহকারী উত্তর:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ColorGreen
                        )
                        Text(
                            text = aiResponse,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // --- EXAMPLES COMMANDS PALETTE DECK ---
        
        Text(
            text = Localization.get("voice_instruction_title", lang),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("voice_command_deck")
        ) {
            items(voiceHelpPrompts) { cmd ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.executeVoiceQuery(cmd) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Help, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = cmd,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
