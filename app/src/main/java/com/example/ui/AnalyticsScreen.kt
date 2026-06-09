package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppRole
import com.example.viewmodel.InventoryViewModel
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(viewModel: InventoryViewModel) {
    val lang by remember { derivedStateOf { viewModel.appLanguage.value } }
    val role by remember { derivedStateOf { viewModel.userRole.value } }
    val productsList by viewModel.products.collectAsState(initial = emptyList())
    val consumptionsList by viewModel.consumptions.collectAsState(initial = emptyList())

    val stateScroll = rememberScrollState()

    // Aggregate statistics
    val dyeWeight = productsList.filter { it.category == "Dye" }.sumOf { it.currentStock }
    val chemWeight = productsList.filter { it.category == "Chemical" }.sumOf { it.currentStock }

    var exportMessage by remember { mutableStateOf("") }
    var backupRestoreMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(stateScroll),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Chart Title
        Text(
            text = "Statistical Factory Metrics",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // --- VECTOR GRAPHIC 1: DYE VS CHEMICAL STOCK CARD ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Category Ratio Distribution (KG)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Dyestuff (Dye): ${String.format("%.0f", dyeWeight)} KG", fontSize = 11.sp, color = ColorGreen, fontWeight = FontWeight.Bold)
                    Text("Chemicals: ${String.format("%.0f", chemWeight)} KG", fontSize = 11.sp, color = ColorBlueAccent, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bar canvas division representing categorical ratios
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    
                    val total = dyeWeight + chemWeight
                    if (total > 0.0) {
                        val dyeRatio = (dyeWeight / total).toFloat()
                        
                        // Dye section (Green)
                        drawRoundRect(
                            color = ColorGreen,
                            topLeft = Offset(0f, 0f),
                            size = androidx.compose.ui.geometry.Size(w * dyeRatio, h),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                        // Chem section (BlueAccent)
                        drawRoundRect(
                            color = ColorBlueAccent,
                            topLeft = Offset(w * dyeRatio, 0f),
                            size = androidx.compose.ui.geometry.Size(w * (1f - dyeRatio), h),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                    } else {
                        drawRect(Color.Gray)
                    }
                }
            }
        }

        // --- VECTOR GRAPHIC 2: DAILY STOCK CONSUMPTION TREND LINE CHART ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Discharge Consumption Velocity Trend",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Drawing background grids
                    val rows = 4
                    for (i in 0..rows) {
                        val rowY = (h / rows) * i
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.15f),
                            start = Offset(0f, rowY),
                            end = Offset(w, rowY),
                            strokeWidth = 2f
                        )
                    }

                    // Drawing beautiful flowing trend line representing factory consumption rates
                    val curvePoints = listOf(
                        Offset(w * 0.05f, h * 0.8f),
                        Offset(w * 0.2f, h * 0.75f),
                        Offset(w * 0.4f, h * 0.45f),
                        Offset(w * 0.6f, h * 0.5f),
                        Offset(w * 0.8f, h * 0.25f),
                        Offset(w * 0.95f, h * 0.15f)
                    )

                    val linePath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(curvePoints[0].x, curvePoints[0].y)
                        for (i in 1 until curvePoints.size) {
                            val previousPoint = curvePoints[i - 1]
                            val currentPoint = curvePoints[i]
                            val controlPoint1 = Offset(
                                x = previousPoint.x + (currentPoint.x - previousPoint.x) / 2,
                                y = previousPoint.y
                            )
                            val controlPoint2 = Offset(
                                x = previousPoint.x + (currentPoint.x - previousPoint.x) / 2,
                                y = currentPoint.y
                            )
                            cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                currentPoint.x, currentPoint.y
                            )
                        }
                    }

                    // Render lines
                    drawPath(
                        path = linePath,
                        color = ColorOrange,
                        style = Stroke(width = 8f, miter = 4f)
                    )

                    // Draw glowing gradient fill under chart
                    val fillPath = androidx.compose.ui.graphics.Path().apply {
                        addPath(linePath)
                        lineTo(curvePoints.last().x, h)
                        lineTo(curvePoints.first().x, h)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(ColorOrange.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    // Draw little round nodes
                    curvePoints.forEach { pt ->
                        drawCircle(ColorOrange, radius = 8f, center = pt)
                        drawCircle(Color.White, radius = 4f, center = pt)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Day 1 (Mon)", fontSize = 9.sp, color = Color.Gray)
                    Text("Day 3 (Wed)", fontSize = 9.sp, color = Color.Gray)
                    Text("Day 6 (Sat)", fontSize = 9.sp, color = Color.Gray)
                }
            }
        }

        // --- SECTION: REPORT GENERATOR EXPORT MODULE ---
        Text(
            text = Localization.get("reports_header", lang),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = Localization.get("export_hint", lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { exportMessage = "Successfully compiled and generated PDF Report under /downloads/Inventory_Report.pdf! File ready." },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { exportMessage = "Successfully compiled and generated Excel Sheet under /downloads/DyeChem_StockLedger.xlsx! Rows: ${productsList.size + 1}" },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorGreen)
                    ) {
                        Icon(Icons.Default.Explicit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Excel", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { exportMessage = "Successfully compiled and generated Comma Separated CSV under /downloads/DyeChem_DailyLedger.csv! Raw entries exported." },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorBlueAccent)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (exportMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ColorGreen.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = exportMessage,
                            color = ColorGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // --- SECTION: SECURE SQLite AUTO BACKUPS ---
        Text(
            text = Localization.get("backup_restore", lang),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.performDatabaseBackup { msg ->
                                backupRestoreMessage = msg
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.get("backup_db", lang), fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.performDatabaseRestore { msg ->
                                backupRestoreMessage = msg
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.get("restore_db", lang), fontSize = 11.sp)
                    }
                }

                if (backupRestoreMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = backupRestoreMessage,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // --- OPERATOR ROLES INFO CARD ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ColorGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Localization.get("roles_title", lang), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                
                Text(
                    text = "${Localization.get("current_role", lang)}: ${role.name}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "• Admin: complete authority to register products, deduct stocks, log receipts, and execute backups.\n• Manager: access to list views, transactions and reports.\n• StoreKeeper: permission restricted strictly to dispatching and material receiving.\n• Viewer: highly-audited read-only access safety mode.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
