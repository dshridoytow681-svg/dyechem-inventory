package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.InventoryViewModel
import com.example.ui.theme.*

@Composable
fun ScannerScreen(viewModel: InventoryViewModel) {
    val lang by remember { derivedStateOf { viewModel.appLanguage.value } }
    
    val scannedItem by remember { derivedStateOf { viewModel.scannedProduct.value } }
    val isScanning by remember { derivedStateOf { viewModel.scanModeActive.value } }
    val statusMsg by remember { derivedStateOf { viewModel.scanStatusMessage.value } }
    
    // Laser sweep animation
    val infiniteTransition = rememberInfiniteTransition(label = "LaserTransition")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserOffset"
    )

    // Preloaded interactive target lots representing the SQLite database cards
    val interactiveTags = listOf(
        DyeStickerTag("Reactive Red Tag", "D-RE-302", "Dye Pack", "LOT-100", "Rack A-3"),
        DyeStickerTag("Disperse Blue Tag", "D-BL-400", "Dye Packet", "LOT-120", "Rack A-5"),
        DyeStickerTag("Sodium Hydro Tag", "C-HY-900", "Chemical Drum", "LOT-205", "Rack B-2"),
        DyeStickerTag("Acetic Acid Label", "C-AC-112", "Liquid Carboy", "LOT-112", "Rack C-1"),
        DyeStickerTag("Caustic Soda Sack", "C-CA-300", "Sack Lot", "LOT-811", "Rack B-4"),
        DyeStickerTag("Hydrogen Peroxide Label", "C-PE-500", "Drum Lot", "LOT-300", "Rack C-3")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core scanning HUD viewport representation
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Localization.get("camera_title", lang),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = Localization.get("ocr_info", lang),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // Live Scanning Viewport boundary Card
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Background grid pattern simulation
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Crosshair Corners
                    val strokeW = 4.dp.toPx()
                    val len = 24.dp.toPx()
                    
                    // Top Left
                    drawLine(Color.White, Offset(20.dp.toPx(), 20.dp.toPx()), Offset(20.dp.toPx() + len, 20.dp.toPx()), strokeWidth = strokeW)
                    drawLine(Color.White, Offset(20.dp.toPx(), 20.dp.toPx()), Offset(20.dp.toPx(), 20.dp.toPx() + len), strokeWidth = strokeW)
                    
                    // Top Right
                    drawLine(Color.White, Offset(w - 20.dp.toPx(), 20.dp.toPx()), Offset(w - 20.dp.toPx() - len, 20.dp.toPx()), strokeWidth = strokeW)
                    drawLine(Color.White, Offset(w - 20.dp.toPx(), 20.dp.toPx()), Offset(w - 20.dp.toPx(), 20.dp.toPx() + len), strokeWidth = strokeW)
                    
                    // Bottom Left
                    drawLine(Color.White, Offset(20.dp.toPx(), h - 20.dp.toPx()), Offset(20.dp.toPx() + len, h - 20.dp.toPx()), strokeWidth = strokeW)
                    drawLine(Color.White, Offset(20.dp.toPx(), h - 20.dp.toPx()), Offset(20.dp.toPx(), h - 20.dp.toPx() - len), strokeWidth = strokeW)
                    
                    // Bottom Right
                    drawLine(Color.White, Offset(w - 20.dp.toPx(), h - 20.dp.toPx()), Offset(w - 20.dp.toPx() - len, h - 20.dp.toPx()), strokeWidth = strokeW)
                    drawLine(Color.White, Offset(w - 20.dp.toPx(), h - 20.dp.toPx()), Offset(w - 20.dp.toPx(), h - 20.dp.toPx() - len), strokeWidth = strokeW)
                    
                    // Holographic neon laser line representing active scan
                    if (isScanning) {
                        val currentY = h * laserOffset
                        drawLine(
                            color = Color(0xFF00FFCC),
                            start = Offset(22.dp.toPx(), currentY),
                            end = Offset(w - 22.dp.toPx(), currentY),
                            strokeWidth = 5f
                        )
                    }
                }

                if (isScanning) {
                    // Active scanner pulsing text
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00FFCC), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusMsg,
                            color = Color(0xFF00FFCC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else if (scannedItem != null) {
                    // Successful Scan Summary Display Overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = ColorGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Localization.get("ocr_success", lang),
                                color = ColorGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        
                        Text(
                            text = scannedItem!!.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SQL Code:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(scannedItem!!.code, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Stored Lot Number:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(scannedItem!!.lotNumber, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Physical Location:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("${scannedItem!!.rackNumber} (${scannedItem!!.warehouseLocation})", fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Ledger Stock:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            val isCrit = scannedItem!!.currentStock <= scannedItem!!.lowStockThreshold
                            Text("${scannedItem!!.currentStock} ${scannedItem!!.unit}", fontWeight = FontWeight.Black, color = if (isCrit) ColorRed else ColorGreen, fontSize = 12.sp)
                        }
                        
                        Button(
                            onClick = { viewModel.scannedProduct.value = null },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Clear Viewport Scan Again", fontSize = 11.sp)
                        }
                    }
                } else {
                    // Help state
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(46.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Localization.get("scan_instruction_1", lang),
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // --- SAMPLE STICKER/LABEL LOT DECK ---
        
        Text(
            text = Localization.get("sample_scanner_header", lang) + " (Click one to trigger simulated scan)",
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("tag_deck_list")
        ) {
            items(interactiveTags) { tag ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.simulateLabelScan(tag.code) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = tag.originalTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Row {
                                Text("Code: ${tag.code}  |  ", fontSize = 11.sp)
                                Text("Lot: ${tag.lot}", fontSize = 11.sp)
                            }
                        }

                        // Cute mini scan button
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

data class DyeStickerTag(
    val originalTitle: String,
    val code: String,
    val description: String,
    val lot: String,
    val rack: String
)
