package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
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

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Live Inventory Statistics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCompactCard(
                title = "Chemical Items",
                value = "$chemCount Products",
                icon = Icons.Default.Science,
                color = Color(0xFFFF9F43), // Vivid Orange matching chemical flask
                onClick = {
                    viewModel.productListFilter.value = "Chemical Products"
                    viewModel.productSubTab.value = 0
                    viewModel.navigateTo(AppScreen.ProductList)
                },
                modifier = Modifier.weight(1f)
            )
            StatsCompactCard(
                title = "Dye Items",
                value = "$dyeCount Products",
                icon = Icons.Default.Palette,
                color = Color(0xFF2ECC71), // Vivid Teal/Green matching palette
                onClick = {
                    viewModel.productListFilter.value = "Dye Products"
                    viewModel.productSubTab.value = 0
                    viewModel.navigateTo(AppScreen.ProductList)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCompactCard(
                title = "Active Locations",
                value = "$rackCount Racks",
                icon = Icons.Default.Layers,
                color = Color(0xFF3498DB), // Vivid Slate Blue
                onClick = {
                    viewModel.navigateTo(AppScreen.RackManagement)
                },
                modifier = Modifier.weight(1f)
            )
            
            StatsCompactCard(
                title = "Low Stock Items",
                value = "$lowStockCount Alerts",
                icon = Icons.Default.Warning,
                color = if (lowStockCount > 0) Color(0xFFE74C3C) else Color.Gray,
                onClick = {
                    viewModel.navigateTo(AppScreen.LowStockAlerts)
                },
                trailing = {
                    if (lowStockCount > 0) {
                        Box(
                            modifier = Modifier
                                .background(Color.Red, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "ALERT",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "FACTORY DEPARTMENTS",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Action grid buttons - dynamic column scroll layout with nested scroll safety
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ActionMenuCard(
                        title = "Recipe Issue",
                        subtitle = "Formulation dispensation",
                        icon = Icons.Default.ReceiptLong,
                        color = MaterialTheme.colorScheme.primary,
                        testTag = "recipe_issue_card",
                        onClick = { viewModel.navigateTo(AppScreen.RecipeIssue) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ActionMenuCard(
                        title = "Vision Scanner",
                        subtitle = "OCR label & recipe scan",
                        icon = Icons.Default.QrCodeScanner,
                        color = Color(0xFF4DB6AC),
                        testTag = "vision_scanner_card",
                        onClick = { viewModel.navigateTo(AppScreen.Scanner) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ActionMenuCard(
                        title = "AI Assistant",
                        subtitle = "Gemini stock queries",
                        icon = Icons.Default.Psychology,
                        color = Color(0xFF9575CD),
                        testTag = "ai_assistant_card",
                        onClick = { viewModel.navigateTo(AppScreen.VoiceAssistant) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ActionMenuCard(
                        title = "Analytics",
                        subtitle = "Daily / Monthly charts",
                        icon = Icons.Default.InsertChartOutlined,
                        color = Color(0xFFE57373),
                        testTag = "analytics_card",
                        onClick = { viewModel.navigateTo(AppScreen.Analytics) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ActionMenuCard(
                        title = "Rack Management",
                        subtitle = "Location mapping",
                        icon = Icons.Default.Layers,
                        color = Color(0xFFF0B27A),
                        testTag = "rack_manager_card",
                        onClick = { viewModel.navigateTo(AppScreen.RackManagement) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
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
}

@Composable
fun StatsCompactCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .background(color.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (trailing != null) {
                    trailing()
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
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
