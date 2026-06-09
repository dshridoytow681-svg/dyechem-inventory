package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.InventoryViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: InventoryViewModel,
    onNavigateTo: (AppScreen) -> Unit
) {
    // Dynamic lists from viewmodel
    val groupedProducts by viewModel.groupedProductsList.collectAsState()
    val lang by remember { derivedStateOf { viewModel.appLanguage.value } }

    // Aggregate product counts
    val chemicalCount = groupedProducts.count { it.category == "Chemical" }
    val dyeCount = groupedProducts.count { it.category == "Dye" }
    val lowStockCount = groupedProducts.count { it.isLowStock }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming and Status banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == AppLanguage.EN) "DyeChem Smart Hub" else "ডাইচেম স্মার্ট হাব",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lang == AppLanguage.EN) 
                            "Automated Lot-level store logs. Ready to scan or command." 
                            else "স্বয়ংক্রিয় লট ট্র্যাকিং ব্যবস্থা। স্ক্যান বা ডিরেক্ট ভয়েস রেডি।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (lowStockCount > 0) {
                    Box(
                        modifier = Modifier
                            .background(ColorRed.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (lang == AppLanguage.EN) "$lowStockCount Alerts!" else "$lowStockCount নোটিশ!",
                            color = ColorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Text(
            text = if (lang == AppLanguage.EN) "Inventory Categories" else "স্টোর ক্যাটাগরি এবং টুলস",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Category Cards Section
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val cardModifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)

            // --- 1. Chemical Items Card ---
            CategoryItemCard(
                title = if (lang == AppLanguage.EN) "Chemical Items" else "কেমিক্যাল আইটেম",
                countText = if (lang == AppLanguage.EN) "$chemicalCount Products" else "$chemicalCount টি প্রোডাক্ট",
                icon = Icons.Default.Science,
                accentColor = ColorOrange,
                onClick = {
                    viewModel.activeCategoryFilter.value = "Chemical"
                    viewModel.searchQuery.value = ""
                    onNavigateTo(AppScreen.INVENTORY)
                },
                modifier = cardModifier
            )

            // --- 2. Dye Items Card ---
            CategoryItemCard(
                title = if (lang == AppLanguage.EN) "Dye Items" else "ডাইং কালারসমূহ",
                countText = if (lang == AppLanguage.EN) "$dyeCount Products" else "$dyeCount টি প্রোডাক্ট",
                icon = Icons.Default.ColorLens,
                accentColor = ColorGreen,
                onClick = {
                    viewModel.activeCategoryFilter.value = "Dye"
                    viewModel.searchQuery.value = ""
                    onNavigateTo(AppScreen.INVENTORY)
                },
                modifier = cardModifier
            )
        }

        // --- 3. Low Stock Card ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (lowStockCount > 0) {
                    if (MaterialTheme.colorScheme.primary == DarkPrimary) Color(0xFF2D1418) else Color(0xFFFFF1F2)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (lowStockCount > 0) ColorRed.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.activeCategoryFilter.value = "Low Stock"
                    viewModel.searchQuery.value = ""
                    onNavigateTo(AppScreen.INVENTORY)
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ColorRed.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = ColorRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == AppLanguage.EN) "Low Stock Alert" else "কম মজুদ সতর্কবার্তা",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (lang == AppLanguage.EN) 
                            "$lowStockCount products threshold warning" 
                            else "$lowStockCount টি পণ্য রি-অর্ডার সীমার নিচে",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ColorRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // --- 4. Recipe Issue Card ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateTo(AppScreen.CONSUMPTION) }
                .testTag("recipe_issue_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ColorOrange.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Assignment,
                        contentDescription = "Recipe Issue",
                        tint = ColorOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == AppLanguage.EN) "🧾 Recipe Issue" else "🧾 রেসিপি স্টক ইস্যু",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (lang == AppLanguage.EN) 
                            "Issue raw materials to production & update stocks automatically" 
                            else "উৎপাদনে কাঁচামাল প্রদান ও স্বয়ংক্রিয়ভাবে স্টক আপডেট করুন",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ColorOrange,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // --- 5. Rack Locations Card ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateTo(AppScreen.RACK_VIEW) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ColorBlueAccent.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.GridView,
                        contentDescription = "Rack Locations",
                        tint = ColorBlueAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == AppLanguage.EN) "📦 Rack Locations" else "📦 র‍্যাক লোকেশনস",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (lang == AppLanguage.EN) "Warehouse Location Tracking & Bin Map" else "ওয়্যারহাউজ বিন ট্র্যাকিং এবং ম্যাপ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ColorBlueAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            text = if (lang == AppLanguage.EN) "Intelligent Hub Utilities" else "স্মার্ট পরিচালনা টুলস",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // --- AI Assistant Module Card ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateTo(AppScreen.AI_ASSISTANT) }
                .testTag("ai_assistant_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == AppLanguage.EN) "✨ AI Assistant" else "✨ এআই অ্যাসিস্ট্যান্ট",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (lang == AppLanguage.EN) 
                            "Intelligent natural voice & text query system" 
                            else "স্টকের নিখুঁত হিস্যা কথা বলে বা লিখে জানুন",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Camera Scan quick Gate (Large Camera Icon)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateTo(AppScreen.SCANNER) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Camera Scan",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (lang == AppLanguage.EN) "Camera Scanner" else "ক্যামেরা স্ক্যানার",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (lang == AppLanguage.EN) "OCR Lot & Recipe Scan" else "ওসিআর দিয়ে লট অনুসন্ধান",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Voice Assistant quick Gate (Large Microphone Icon)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateTo(AppScreen.VOICE) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Voice Mode",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (lang == AppLanguage.EN) "Voice Assistant" else "ভয়েস অ্যাসিস্ট্যান্ট",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (lang == AppLanguage.EN) "Bengali Vocal Command" else "কথা বলে দ্রুত চেক করুন",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryItemCard(
    title: String,
    countText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(accentColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = countText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
