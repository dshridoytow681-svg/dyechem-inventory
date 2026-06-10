package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.UserRole
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val productsList by viewModel.products.collectAsState()
    val lotsList by viewModel.lots.collectAsState()
    val lowStockCount by viewModel.lowStockCount.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    val chemCount = productsList.count { it.category.equals("Chemicals", ignoreCase = true) }
    val dyeCount = productsList.count { it.category.equals("Dyes", ignoreCase = true) }
    val rackCount = productsList.map { it.rackNumber }.distinct().count()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Welcoming Card
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Welcome to DyeChem Smart Pro",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Logistics Mode: ${currentRole.name} | Factory Connection: Active & Enforced",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Live Inventory Statistics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCompactCard(
                title = "Chemicals",
                value = chemCount.toString(),
                icon = Icons.Default.Science,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatsCompactCard(
                title = "Dyes",
                value = dyeCount.toString(),
                icon = Icons.Default.Palette,
                color = Color(0xFFFF9F43),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCompactCard(
                title = "Racks active",
                value = rackCount.toString(),
                icon = Icons.Default.Layers,
                color = Color(0xFF2ECC71),
                modifier = Modifier.weight(1f)
            )
            // Low Stock alert badge card
            Box(modifier = Modifier.weight(1f)) {
                StatsCompactCard(
                    title = "Low Stock Limit",
                    value = lowStockCount.toString(),
                    icon = Icons.Default.Warning,
                    color = if (lowStockCount > 0) Color(0xFFFF4C00) else Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.LowStockAlerts) }
                )
                if (lowStockCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Red, RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "ALERT",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "LIVE STOCK FEED (CLICKABLE)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Scrollable Row of clickable stock status cards!
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(productsList) { product ->
                val prodLots = lotsList.filter { it.productId == product.id }
                val totalQty = prodLots.sumOf { it.quantity }
                val isLow = totalQty < product.lowStockLimit
                
                Card(
                    onClick = {
                        viewModel.selectedProduct.value = product
                        viewModel.navigateTo(AppScreen.ProductDetails)
                    },
                    modifier = Modifier
                        .width(170.dp)
                        .border(
                            1.dp,
                            if (isLow) Color(0xFFFF4C00).copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            RoundedCornerShape(10.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLow) Color(0xFFFF4C00).copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isLow) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isLow) Color(0xFFFF4C00) else Color(0xFF2ECC71),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${totalQty} ${product.unit}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Rack: ${product.rackNumber}",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "FACTORY DEPARTMENTS",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Action grid buttons
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ActionMenuCard(
                    title = "Recipe Issue",
                    subtitle = "Formulation dispensation",
                    icon = Icons.Default.ReceiptLong,
                    color = MaterialTheme.colorScheme.primary,
                    testTag = "recipe_issue_card",
                    onClick = { viewModel.navigateTo(AppScreen.RecipeIssue) }
                )
            }
            item {
                ActionMenuCard(
                    title = "Vision Scanner",
                    subtitle = "OCR label & recipe scan",
                    icon = Icons.Default.QrCodeScanner,
                    color = Color(0xFF4DB6AC),
                    testTag = "vision_scanner_card",
                    onClick = { viewModel.navigateTo(AppScreen.Scanner) }
                )
            }
            item {
                ActionMenuCard(
                    title = "AI Assistant",
                    subtitle = "Gemini stock queries",
                    icon = Icons.Default.Psychology,
                    color = Color(0xFF9575CD),
                    testTag = "ai_assistant_card",
                    onClick = { viewModel.navigateTo(AppScreen.VoiceAssistant) }
                )
            }
            item {
                ActionMenuCard(
                    title = "Analytics",
                    subtitle = "Daily / Monthly charts",
                    icon = Icons.Default.InsertChartOutlined,
                    color = Color(0xFFE57373),
                    testTag = "analytics_card",
                    onClick = { viewModel.navigateTo(AppScreen.Analytics) }
                )
            }
            item {
                ActionMenuCard(
                    title = "Rack Management",
                    subtitle = "Location mapping",
                    icon = Icons.Default.Layers,
                    color = Color(0xFFF0B27A),
                    testTag = "rack_manager_card",
                    onClick = { viewModel.navigateTo(AppScreen.RackManagement) }
                )
            }
            item {
                ActionMenuCard(
                    title = "Suppliers Directory",
                    subtitle = "Partner mobile & info",
                    icon = Icons.Default.ContactPhone,
                    color = Color(0xFF4DD0E1),
                    testTag = "suppliers_card",
                    onClick = { viewModel.navigateTo(AppScreen.SupplierModule) }
                )
            }
        }
    }
}

@Composable
fun StatsCompactCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Text(
                    title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ActionMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
