package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.viewmodel.InventoryViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RackViewScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val groupedProducts by viewModel.groupedProductsList.collectAsState()
    val lang by remember { derivedStateOf { viewModel.appLanguage.value } }

    var searchQuery by remember { mutableStateOf("") }
    var selectedViewMode by remember { mutableStateOf(0) } // 0: Rack Grid View, 1: Product View
    var selectedRackDetails by remember { mutableStateOf<String?>(null) }

    val allLots = remember(groupedProducts) {
        groupedProducts.flatMap { it.lots }
    }

    // Rows and Racks list
    val rackMap = remember {
        mapOf(
            "ROW A" to listOf("A-01", "A-02", "A-03"),
            "ROW B" to listOf("B-01", "B-02", "B-03"),
            "ROW C" to listOf("C-01", "C-02", "C-03")
        )
    }

    // Process search
    val q = searchQuery.trim().lowercase()
    val filteredLots = remember(allLots, q) {
        if (q.isEmpty()) {
            allLots
        } else {
            allLots.filter { lot ->
                lot.name.lowercase().contains(q) ||
                lot.lotNumber.lowercase().contains(q) ||
                lot.rackNumber.lowercase().contains(q) ||
                lot.brand.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (lang == AppLanguage.EN) "Warehouse Rack Layout" else "ওয়্যারহাউজ বিন ম্যাপ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (lang == AppLanguage.EN) "Real-time Row & Rack assignments" else "র্যাকে পণ্য ও লট লোকেশন ট্র্যাকিং",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Search Bar with Microphone voice gate inside it
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (lang == AppLanguage.EN) "Search product, lot #, rack..." else "পণ্য, লট #, র‍্যাক খুঁজুন...",
                            fontSize = 12.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            }
        }

        // View Mode Switch Tabs
        TabRow(
            selectedTabIndex = selectedViewMode,
            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            indicator = { TabRowDefaults.SecondaryIndicator(color = MaterialTheme.colorScheme.primary) }
        ) {
            Tab(
                selected = selectedViewMode == 0,
                onClick = { selectedViewMode = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(if (lang == AppLanguage.EN) "Rack Layout" else "র‍্যাক লেআউট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedViewMode == 1,
                onClick = { selectedViewMode = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(if (lang == AppLanguage.EN) "Product Lots (Filter)" else "পণ্য লট ফিল্টার", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Render views based on tab
        if (selectedViewMode == 0) {
            // RACK LAYOUT VIEW BY ROWS A, B, C
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("rack_layout_grid")
            ) {
                rackMap.forEach { (rowName, racksList) ->
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Row Header Section
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ViewStream, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = rowName,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Racks inside Row (Grid style)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                racksList.forEach { rackNum ->
                                    val matchingLots = allLots.filter {
                                        it.rackNumber.contains(rackNum, ignoreCase = true)
                                    }
                                    val matchingLotsQuery = filteredLots.filter {
                                        it.rackNumber.contains(rackNum, ignoreCase = true)
                                    }

                                    val isHighlightedByQuery = q.isNotEmpty() && matchingLotsQuery.isNotEmpty()
                                    val totalStock = matchingLots.sumOf { it.currentStock }
                                    val lotCount = matchingLots.size
                                    val isEmpty = matchingLots.isEmpty()
                                    val isLowStock = matchingLots.any { it.currentStock <= it.lowStockThreshold }

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = when {
                                                isEmpty -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                isHighlightedByQuery -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                isLowStock -> ColorRed.copy(alpha = 0.06f)
                                                else -> MaterialTheme.colorScheme.surface
                                            }
                                        ),
                                        border = BorderStroke(
                                            width = if (isHighlightedByQuery) 2.dp else 1.dp,
                                            color = when {
                                                isHighlightedByQuery -> MaterialTheme.colorScheme.primary
                                                isLowStock -> ColorRed.copy(alpha = 0.3f)
                                                isEmpty -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                            }
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedRackDetails = rackNum }
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = rackNum,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp,
                                                color = if (isEmpty) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                            )
                                            
                                            Spacer(modifier = Modifier.height(4.dp))

                                            if (isEmpty) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "EMPTY",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(18.dp))
                                            } else {
                                                Text(
                                                    text = "$lotCount Lots",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "$totalStock KG",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isLowStock) ColorRed else ColorGreen
                                                )
                                                
                                                if (isLowStock) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(top = 4.dp)
                                                            .background(ColorRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("LOW", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ColorRed)
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.height(13.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // PRODUCT LIST PLACEMENT FILTER VIEW
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("rack_product_lots")
            ) {
                if (filteredLots.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No lots match the search criteria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(filteredLots) { lot ->
                        val isCritical = lot.currentStock <= lot.lowStockThreshold
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(ColorBlueAccent.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Domain, contentDescription = null, tint = ColorBlueAccent, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(lot.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Lot: ${lot.lotNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Bin: ${lot.rackNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorOrange)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${lot.currentStock} ${lot.unit}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = if (isCritical) ColorRed else ColorGreen)
                                    Text(lot.warehouseLocation, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Rack Details Dialog
        selectedRackDetails?.let { rackNum ->
            val matchingLots = allLots.filter { it.rackNumber.contains(rackNum, ignoreCase = true) }
            val totalStock = matchingLots.sumOf { it.currentStock }

            AlertDialog(
                onDismissRequest = { selectedRackDetails = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = ColorOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rack Details: $rackNum", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Item Count:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${matchingLots.size} Lots", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Stock quantity:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("$totalStock KG", fontWeight = FontWeight.Black, color = ColorGreen)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(4.dp))

                        if (matchingLots.isEmpty()) {
                            Text(
                                "No items in this rack. You can edit a lot's assignment or add a product to this rack Coordinate.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(matchingLots) { lot ->
                                    val isCritical = lot.currentStock <= lot.lowStockThreshold
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(lot.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("Lot: ${lot.lotNumber} • ${lot.brand}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Text(
                                                    "${lot.currentStock} ${lot.unit}",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 13.sp,
                                                    color = if (isCritical) ColorRed else ColorGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedRackDetails = null }) {
                        Text("Dismiss", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
