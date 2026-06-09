package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppRole
import com.example.viewmodel.InventoryViewModel
import com.example.viewmodel.NotificationType
import com.example.viewmodel.AppNotification
import com.example.ui.theme.ColorGreen
import com.example.ui.theme.ColorOrange
import com.example.ui.theme.ColorRed
import com.example.ui.theme.ColorBlueAccent

enum class AppScreen {
    DASHBOARD, INVENTORY, CONSUMPTION, PURCHASES, SCANNER, VOICE, ANALYTICS, RACK_VIEW, AI_ASSISTANT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(
    viewModel: InventoryViewModel,
    darkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var showRoleMenu by remember { mutableStateOf(false) }
    var showNotifDialog by remember { mutableStateOf(false) }

    val lang by remember { derivedStateOf { viewModel.appLanguage.value } }
    val role by remember { derivedStateOf { viewModel.userRole.value } }

    // Responsive sizing logic
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth > 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Left Navigation Rail for Tablets
            if (isTablet) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.width(96.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(onClick = onToggleDarkMode) {
                        Icon(
                            imageVector = if (darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.DASHBOARD,
                        onClick = { currentScreen = AppScreen.DASHBOARD },
                        icon = { Icon(Icons.Default.Dashboard, "Dashboard") },
                        label = { Text(Localization.get("nav_dashboard", lang), fontSize = 10.sp) }
                    )
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.INVENTORY,
                        onClick = { currentScreen = AppScreen.INVENTORY },
                        icon = { Icon(Icons.Default.Inventory, "Inventory") },
                        label = { Text(Localization.get("nav_inventory", lang), fontSize = 10.sp) }
                    )
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.CONSUMPTION,
                        onClick = { currentScreen = AppScreen.CONSUMPTION },
                        icon = { Icon(Icons.Default.Assignment, "Recipe Issue") },
                        label = { Text(Localization.get("nav_consumption", lang), fontSize = 10.sp) }
                    )
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.PURCHASES,
                        onClick = { currentScreen = AppScreen.PURCHASES },
                        icon = { Icon(Icons.Default.ShoppingCart, "Purchases") },
                        label = { Text(Localization.get("nav_purchases", lang), fontSize = 10.sp) }
                    )
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.SCANNER,
                        onClick = { currentScreen = AppScreen.SCANNER },
                        icon = { Icon(Icons.Default.QrCodeScanner, "Scanner") },
                        label = { Text(Localization.get("nav_scanner", lang), fontSize = 10.sp) }
                    )
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.VOICE,
                        onClick = { currentScreen = AppScreen.VOICE },
                        icon = { Icon(Icons.Default.Mic, "Voice") },
                        label = { Text(Localization.get("nav_voice", lang), fontSize = 10.sp) }
                    )
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.ANALYTICS,
                        onClick = { currentScreen = AppScreen.ANALYTICS },
                        icon = { Icon(Icons.Default.Assessment, "Analytics") },
                        label = { Text(Localization.get("nav_analytics", lang), fontSize = 10.sp) }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Main Content Area
            Scaffold(
                topBar = {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 20.dp)
                        ) {
                            // Row 1: App header info & Language switchers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Cute Factory icon border from HTML
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🏭", fontSize = 20.sp)
                                    }
                                    
                                    Column(verticalArrangement = Arrangement.Center) {
                                        Text(
                                            text = Localization.get("app_title", lang),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "ENTERPRISE INVENTORY",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White.copy(alpha = 0.7f),
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }

                                // Language Toggle Pills side-by-side (English vs বাংলা) matching active:scale-95 transition HTML style
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                        .padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (lang == AppLanguage.EN) Color.White.copy(alpha = 0.25f) else Color.Transparent)
                                            .clickable { viewModel.appLanguage.value = AppLanguage.EN }
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("English", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (lang == AppLanguage.BN) Color.White.copy(alpha = 0.25f) else Color.Transparent)
                                            .clickable { viewModel.appLanguage.value = AppLanguage.BN }
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("বাংলা", color = if (lang == AppLanguage.BN) Color.White else Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = Color.White.copy(alpha = 0.12f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Row 2: Roles selector chip, Notifications warning, Dark mode
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Clickable Role Select Chip
                                Box {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .clickable { showRoleMenu = true }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Role Selector", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Role: ${role.name}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }

                                    DropdownMenu(
                                        expanded = showRoleMenu,
                                        onDismissRequest = { showRoleMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(Localization.get("btn_admin", lang)) },
                                            onClick = { viewModel.userRole.value = AppRole.ADMIN; showRoleMenu = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(Localization.get("btn_manager", lang)) },
                                            onClick = { viewModel.userRole.value = AppRole.MANAGER; showRoleMenu = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(Localization.get("btn_keeper", lang)) },
                                            onClick = { viewModel.userRole.value = AppRole.KEEPER; showRoleMenu = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(Localization.get("btn_viewer", lang)) },
                                            onClick = { viewModel.userRole.value = AppRole.VIEWER; showRoleMenu = false }
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Notifications Trigger
                                    IconButton(
                                        onClick = { showNotifDialog = true },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                    ) {
                                        Box {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = "Alerts",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            if (viewModel.appNotifications.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(ColorRed, CircleShape)
                                                        .align(Alignment.TopEnd)
                                                )
                                            }
                                        }
                                    }

                                    // Dark Mode Toggle for Mobile
                                    if (!isTablet) {
                                        IconButton(
                                            onClick = onToggleDarkMode,
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = if (darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                contentDescription = "Theme Toggle",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    if (!isTablet) {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.DASHBOARD,
                                onClick = { currentScreen = AppScreen.DASHBOARD },
                                icon = { Icon(Icons.Default.Dashboard, null) },
                                label = { Text(Localization.get("nav_dashboard", lang), fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.INVENTORY,
                                onClick = { currentScreen = AppScreen.INVENTORY },
                                icon = { Icon(Icons.Default.Inventory, null) },
                                label = { Text(Localization.get("nav_inventory", lang), fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.SCANNER,
                                onClick = { currentScreen = AppScreen.SCANNER },
                                icon = { Icon(Icons.Default.QrCodeScanner, null) },
                                label = { Text(Localization.get("nav_scanner", lang), fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.CONSUMPTION,
                                onClick = { currentScreen = AppScreen.CONSUMPTION },
                                icon = { Icon(Icons.Default.Assignment, null) },
                                label = { Text(Localization.get("nav_consumption", lang), fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.ANALYTICS,
                                onClick = { currentScreen = AppScreen.ANALYTICS },
                                icon = { Icon(Icons.Default.Assessment, null) },
                                label = { Text(Localization.get("nav_analytics", lang), fontSize = 10.sp) }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateTo = { currentScreen = it }
                            )
                            AppScreen.INVENTORY -> InventoryScreen(viewModel = viewModel, onNavigateTo = { currentScreen = it })
                            AppScreen.CONSUMPTION -> RecipeIssueScreen(viewModel = viewModel)
                            AppScreen.PURCHASES -> PurchaseScreen(viewModel = viewModel)
                            AppScreen.SCANNER -> ScannerScreen(viewModel = viewModel)
                            AppScreen.VOICE -> VoiceScreen(viewModel = viewModel)
                            AppScreen.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                            AppScreen.RACK_VIEW -> RackViewScreen(viewModel = viewModel, onNavigateBack = { currentScreen = AppScreen.DASHBOARD })
                            AppScreen.AI_ASSISTANT -> AiAssistantScreen(viewModel = viewModel, onNavigateBack = { currentScreen = AppScreen.DASHBOARD })
                        }
                    }
                }
            }
        }
    }

    // Notifications Dialog
    if (showNotifDialog) {
        AlertDialog(
            onDismissRequest = { showNotifDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ColorOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Localization.get("low_stock", lang))
                }
            },
            text = {
                if (viewModel.appNotifications.isEmpty()) {
                    Text("All dyestuffs and chemicals are above low safety margins.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(viewModel.appNotifications) { notif ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (notif.type == NotificationType.WARNING) ColorRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(notif.title, fontWeight = FontWeight.Bold, color = if (notif.type == NotificationType.WARNING) ColorRed else MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(notif.body, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotifDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}
