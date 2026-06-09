package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.border
import com.example.data.ProductEntity
import com.example.data.TransferEntity
import com.example.viewmodel.AppRole
import com.example.viewmodel.GroupedProduct
import com.example.viewmodel.InventoryViewModel
import com.example.ui.theme.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: InventoryViewModel) {
    val groupedProductsList by viewModel.groupedProductsList.collectAsState(initial = emptyList())
    val transfersList by viewModel.transfers.collectAsState(initial = emptyList())
    val lang by remember { derivedStateOf { viewModel.appLanguage.value } }
    val role by remember { derivedStateOf { viewModel.userRole.value } }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedGroupedProduct by remember { mutableStateOf<GroupedProduct?>(null) }

    // Quick transaction shortcuts linked to particular lots
    var showLotDispatchDialog by remember { mutableStateOf<ProductEntity?>(null) }
    var showLotInwardDialog by remember { mutableStateOf<ProductEntity?>(null) }
    var showLotTransferDialog by remember { mutableStateOf<ProductEntity?>(null) }
    
    // Toggle for FIFO transaction vs single-lot
    var showProductFifoDispatchDialog by remember { mutableStateOf<GroupedProduct?>(null) }

    val activeFilter = viewModel.activeCategoryFilter.value
    val query = viewModel.searchQuery.value.trim().lowercase()

    // 1. Process search AND Multi-category rules live
    val filteredGrouped = groupedProductsList.filter { gp ->
        val matchesCategory = when (activeFilter) {
            "All" -> true
            "Dye" -> gp.category == "Dye"
            "Chemical" -> gp.category == "Chemical"
            "Low Stock" -> gp.isLowStock
            "High Value" -> gp.lots.sumOf { it.currentStock * it.purchasePrice } > 1000.0
            "Recently Updated" -> true
            else -> true
        }
        val matchesSearch = query.isEmpty() || 
            gp.name.lowercase().contains(query) ||
            gp.lots.any { 
                it.lotNumber.lowercase().contains(query) || 
                it.batchNumber.lowercase().contains(query) || 
                it.rackNumber.lowercase().contains(query) || 
                it.brand.lowercase().contains(query)
            }
        matchesCategory && matchesSearch
    }.let { list ->
        when (activeFilter) {
            "Recently Updated" -> {
                list.sortedByDescending { gp -> gp.lots.maxOfOrNull { it.id } ?: 0 }
            }
            "High Value" -> {
                list.sortedByDescending { gp -> gp.lots.sumOf { it.currentStock * it.purchasePrice } }
            }
            else -> list
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- SEARCH HEADER PANEL ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = viewModel.searchQuery.value,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { 
                        Text(
                            text = if (lang == AppLanguage.EN) "Search Name, Lot, Batch, Rack..." else "নাম, লট, ব্যাচ বা র‍্যাক দিয়ে খুঁজুন...", 
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
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_bar"),
                    singleLine = true
                )
                if (viewModel.searchQuery.value.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            }
        }

        // --- SECTION 2: SCROLLABLE CATEGORY / FILTER CHIPS ---
        val filterOptions = listOf(
            "All" to if (lang == AppLanguage.EN) "All Products" else "সব পণ্য",
            "Dye" to if (lang == AppLanguage.EN) "Dye Products" else "রং পণ্য",
            "Chemical" to if (lang == AppLanguage.EN) "Chemical Products" else "কেমিক্যাল পণ্য",
            "Low Stock" to if (lang == AppLanguage.EN) "Low Stock Alerts" else "কম মজুদ",
            "High Value" to if (lang == AppLanguage.EN) "High Value" else "উচ্চ মূল্যের স্টক",
            "Recently Updated" to if (lang == AppLanguage.EN) "Recently Updated" else "নতুন আপডেট"
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(filterOptions) { (key, title) ->
                val isSelected = activeFilter == key
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.activeCategoryFilter.value = key }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

        // --- DOUBLE LEVEL SCREEN RENDERING FLOW ---
        if (selectedGroupedProduct == null) {
            // LEVEL 1: GROUPED PRODUCT SUMMARY CARD LISTS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == AppLanguage.EN) "Cataloged Stocks (${filteredGrouped.size})" else "ক্যাটালগড তালিকা (${filteredGrouped.size})",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (role != AppRole.VIEWER) {
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (lang == AppLanguage.EN) "New Lot" else "নতুন লট এন্ট্রি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (filteredGrouped.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (lang == AppLanguage.EN) "No matching dyes or chemicals cataloged." else "কোনো মিল থাকা রং বা কেমিক্যাল পাওয়া যায়নি।",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("products_list_tag"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredGrouped) { gp ->
                        GroupedProductCard(gp = gp, lang = lang) {
                            selectedGroupedProduct = gp
                        }
                    }
                }
            }
        } else {
            // LEVEL 2: COMPREHENSIVE PRODUCT DETAILS VIEW (WITH ALL SEPARATE LOTS)
            val gp = selectedGroupedProduct!!
            
            // Recalculate values dynamically matching live list modifications
            val liveLots = groupedProductsList.find { it.name == gp.name }?.lots ?: gp.lots
            val totalLiveStock = liveLots.sumOf { it.currentStock }
            val liveLowStockAlert = liveLots.count { it.currentStock <= it.lowStockThreshold }
            
            val totalValueLiveBDT = liveLots.filter { it.currency == "BDT" }.sumOf { it.currentStock * it.purchasePrice }
            val totalValueLiveUSD = liveLots.filter { it.currency == "USD" }.sumOf { it.currentStock * it.purchasePrice }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedGroupedProduct = null }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = gp.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${gp.brand} • ${gp.category}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                
                // FIFO Action trigger gate
                if (liveLots.size > 1 && role != AppRole.VIEWER) {
                    Button(
                        onClick = { showProductFifoDispatchDialog = gp },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("FIFO Dispatch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- PRODUCT DETAIL SUMMARY CARD ---
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "PRODUCT STOCK SUMMARY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total Stock", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$totalLiveStock ${gp.unit}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                }
                                Column {
                                    Text("Registered Lots", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${liveLots.size} Lots", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Column {
                                    Text("Critical Lots", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$liveLowStockAlert Alert", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (liveLowStockAlert > 0) ColorRed else ColorGreen)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Valuation", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Column(horizontalAlignment = Alignment.End) {
                                    if (totalValueLiveBDT > 0) {
                                        Text("৳ ${String.format("%,.0f", totalValueLiveBDT)} BDT", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = ColorGreen)
                                    }
                                    if (totalValueLiveUSD > 0) {
                                        Text("$ ${String.format("%,.2f", totalValueLiveUSD)} USD", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = ColorBlueAccent)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Separate Storage Lots",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Render each Lot separately
                items(liveLots) { lot ->
                    val isCritical = lot.currentStock <= lot.lowStockThreshold
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCritical) {
                                if (MaterialTheme.colorScheme.primary == DarkPrimary) Color(0xFF2D1418) else Color(0xFFFFF1F2)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isCritical) ColorRed.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (gp.category == "Dye") ColorGreen.copy(alpha = 0.1f) else ColorBlueAccent.copy(alpha = 0.1f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Inbox,
                                            contentDescription = null,
                                            tint = if (gp.category == "Dye") ColorGreen else ColorBlueAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Lot: ${lot.lotNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Batch: ${lot.batchNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${lot.currentStock} ${lot.unit}", fontWeight = FontWeight.Black, fontSize = 17.sp, color = if (isCritical) ColorRed else ColorGreen)
                                    Text("Remaining", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Grid with Storage placement, rack ID, entry, supplier and price
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("Rack Assignment: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${lot.rackNumber} (${lot.warehouseLocation})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("Entry / Expiry: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${lot.entryDate} / ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(lot.expiryDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorRed)
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("Purchase Valuation: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    val priceSymbol = if (lot.currency == "BDT") "৳" else "$"
                                    Text("$priceSymbol${lot.purchasePrice} per ${lot.unit}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // --- INDIVIDUAL ACTIONS ROW per LOT ---
                            if (role != AppRole.VIEWER) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 1. Consume Lot
                                    Button(
                                        onClick = { showLotDispatchDialog = lot },
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorOrange),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Dispatch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // 2. Transfer coordinates
                                    Button(
                                        onClick = { showLotTransferDialog = lot },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.MoveUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Move Rack", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // 3. Inward re-stock
                                    Button(
                                        onClick = { showLotInwardDialog = lot },
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Re-Stock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Show localized transfers history for this lot
                item {
                    val matchingTransfers = transfersList.filter { liveLots.any { lot -> lot.lotNumber == it.lotNumber } }
                    if (matchingTransfers.isNotEmpty()) {
                        Text(
                            text = "Lot Movement History logs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        matchingTransfers.forEach { tr ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Lot ${tr.lotNumber}: ${tr.fromRack} ➔ ${tr.toRack}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("By ${tr.operator} • ${tr.date}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- FORM 1: ADD NEW PRODUCT/LOT DIALOG ---
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Dye") }
        var brand by remember { mutableStateOf("") }
        var lotNumber by remember { mutableStateOf("") }
        var batchNumber by remember { mutableStateOf("") }
        var rackNumber by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var unit by remember { mutableStateOf("KG") }
        var openingStock by remember { mutableStateOf("") }
        var threshold by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        var currency by remember { mutableStateOf("BDT") }
        var iconName by remember { mutableStateOf("color_bag") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (lang == AppLanguage.EN) "Register New Lot" else "নতুন কোড ও লট রেজিস্ট্রি", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Product Code (SKU)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = category == "Dye", onClick = { category = "Dye" })
                            Text("Dye (রং)")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = category == "Chemical", onClick = { category = "Chemical" })
                            Text("Chemical (রসায়ন)")
                        }
                    }
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand/Supplier Company") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = lotNumber,
                            onValueChange = { lotNumber = it },
                            label = { Text("Lot #") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = batchNumber,
                            onValueChange = { batchNumber = it },
                            label = { Text("Batch #") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = rackNumber,
                            onValueChange = { rackNumber = it },
                            label = { Text("Rack Coordinate") },
                            modifier = Modifier.weight(1.1f)
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location") },
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = openingStock,
                            onValueChange = { openingStock = it },
                            label = { Text("Opening Stock") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.1f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (e.g. KG, Liter, Bag)") },
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = threshold,
                            onValueChange = { threshold = it },
                            label = { Text("Low Stock Threshold") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("UnitPrice") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Currency: ")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = currency == "BDT", onClick = { currency = "BDT" })
                            Text("BDT (৳)")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = currency == "USD", onClick = { currency = "USD" })
                            Text("USD ($)")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val initStock = openingStock.toDoubleOrNull() ?: 0.0
                        val rawPrice = price.toDoubleOrNull() ?: 0.0
                        val rawThreshold = threshold.toDoubleOrNull() ?: 10.0
                        if (name.isNotEmpty() && lotNumber.isNotEmpty()) {
                            viewModel.addProduct(
                                name = name,
                                code = code.ifEmpty { "SKU-DYE" },
                                category = category,
                                brand = brand.ifEmpty { "BASF Dyeing" },
                                lotNumber = lotNumber,
                                batchNumber = batchNumber.ifEmpty { "BATCH-A" },
                                rackNumber = rackNumber.ifEmpty { "Rack A-01" },
                                warehouseLocation = location.ifEmpty { "Main Dye Store" },
                                unit = unit.ifEmpty { "KG" },
                                openingStock = initStock,
                                lowStockThreshold = rawThreshold,
                                purchasePrice = rawPrice,
                                currency = currency,
                                iconName = iconName
                            ) {
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text("Save Lot")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- FORM 2: DISPATCH DIALOG MATRIX FOR SINGLE LOT ---
    if (showLotDispatchDialog != null) {
        val lot = showLotDispatchDialog!!
        var qtyStr by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("") }
        var operator by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showLotDispatchDialog = null },
            title = { Text("Lot Stock Dispatch Form", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Product: ${lot.name}", fontWeight = FontWeight.SemiBold)
                    Text("Selected Lot Number: ${lot.lotNumber}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("Current Rack: ${lot.rackNumber} (${lot.currentStock} ${lot.unit} available)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Quantity to dispatch (${lot.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dept,
                        onValueChange = { dept = it },
                        label = { Text("Department / Recipe Card #") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = operator,
                        onValueChange = { operator = it },
                        label = { Text("Operator Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Additional notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val taken = qtyStr.toDoubleOrNull() ?: 0.0
                        if (taken > 0.0 && taken <= lot.currentStock) {
                            viewModel.recordLotConsumption(
                                lotId = lot.id,
                                quantity = taken,
                                department = dept.ifEmpty { "Dyehouse Unit" },
                                operator = operator.ifEmpty { "Vocal Service API" },
                                notes = notes.ifEmpty { "Manual dispatch checkout" }
                            ) { success ->
                                if (success) {
                                    showLotDispatchDialog = null
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorOrange)
                ) {
                    Text("Confirm Dispatch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLotDispatchDialog = null }) { Text("Cancel") }
            }
        )
    }

    // --- FORM 3: FIFO AUTOMATED DISPATCH DIALOG ---
    if (showProductFifoDispatchDialog != null) {
        val gp = showProductFifoDispatchDialog!!
        var qtyStr by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("") }
        var operator by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showProductFifoDispatchDialog = null },
            title = { Text("FIFO Automatic Sequential Dispatch", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Product: ${gp.name}", fontWeight = FontWeight.Bold)
                    Text("Total combined stock: ${gp.totalStock} ${gp.unit}", fontSize = 13.sp, color = ColorGreen, fontWeight = FontWeight.Bold)
                    Text("Aesthetic FIFO: oldest lots will be drained of stock first automatically, leaving newest stock intact.", fontSize = 11.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Quantity to dispatch (${gp.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dept,
                        onValueChange = { dept = it },
                        label = { Text("Department") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = operator,
                        onValueChange = { operator = it },
                        label = { Text("Operator Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val taken = qtyStr.toDoubleOrNull() ?: 0.0
                        if (taken > 0.0 && taken <= gp.totalStock) {
                            viewModel.recordFifoConsumption(
                                productName = gp.name,
                                quantity = taken,
                                department = dept.ifEmpty { "FIFO Automatic batch" },
                                operator = operator.ifEmpty { "Store Keeper FIFO Console" },
                                notes = notes.ifEmpty { "FIFO sequential automated checkout" }
                            ) { success ->
                                if (success) {
                                    showProductFifoDispatchDialog = null
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorOrange)
                ) {
                    Text("Confirm FIFO Dispatch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProductFifoDispatchDialog = null }) { Text("Cancel") }
            }
        )
    }

    // --- FORM 4: RACK RELOCATION CABINET HISTORY LOG DRAFT ---
    if (showLotTransferDialog != null) {
        val lot = showLotTransferDialog!!
        var targetRack by remember { mutableStateOf("") }
        var operator by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showLotTransferDialog = null },
            title = { Text("Move Lot Rack Coordinate", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Transfering Lot: ${lot.lotNumber}", fontWeight = FontWeight.Bold)
                    Text("Chemical: ${lot.name}", fontSize = 12.sp)
                    Text("Original Location: ${lot.rackNumber} (${lot.warehouseLocation})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = targetRack,
                        onValueChange = { targetRack = it },
                        label = { Text("Target Rack ID (e.g. Rack B-10)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = operator,
                        onValueChange = { operator = it },
                        label = { Text("Authorized Operator") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (targetRack.isNotEmpty()) {
                            viewModel.transferLotRack(
                                lotId = lot.id,
                                toRack = targetRack,
                                operator = operator.ifEmpty { "Manual coordinator" }
                            ) {
                                showLotTransferDialog = null
                            }
                        }
                    }
                ) {
                    Text("Complete Relocation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLotTransferDialog = null }) { Text("Cancel") }
            }
        )
    }

    // --- FORM 5: RE-STOCK / PURCHASE INWARD LOT ---
    if (showLotInwardDialog != null) {
        val lot = showLotInwardDialog!!
        var invoiceNum by remember { mutableStateOf("") }
        var supName by remember { mutableStateOf("") }
        var addStockStr by remember { mutableStateOf("") }
        var priceStr by remember { mutableStateOf(lot.purchasePrice.toString()) }

        AlertDialog(
            onDismissRequest = { showLotInwardDialog = null },
            title = { Text("Add Lot Stock Volume", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Product: ${lot.name}", fontWeight = FontWeight.Bold)
                    Text("Reffilling Lot Number: ${lot.lotNumber}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = addStockStr,
                        onValueChange = { addStockStr = it },
                        label = { Text("Inward Stock Quantity (${lot.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = invoiceNum,
                        onValueChange = { invoiceNum = it },
                        label = { Text("Invoice/Receipt Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = supName,
                        onValueChange = { supName = it },
                        label = { Text("Supplier Organization") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Refill unit price (${lot.currency})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val vol = addStockStr.toDoubleOrNull() ?: 0.0
                        val rate = priceStr.toDoubleOrNull() ?: lot.purchasePrice
                        if (vol > 0.0) {
                            viewModel.recordPurchase(
                                productId = lot.id,
                                supplierName = supName.ifEmpty { lot.brand },
                                invoiceNumber = invoiceNum.ifEmpty { "REC-AUTO" },
                                quantity = vol,
                                unitPrice = rate,
                                currency = lot.currency
                            ) { success ->
                                if (success) {
                                    showLotInwardDialog = null
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGreen)
                ) {
                    Text("Re-Stock Lot")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLotInwardDialog = null }) { Text("Cancel") }
            }
        )
    }
}

// 1st Level aggregate Product Card displaying cartoon logo, composite stocks, and total lots count
@Composable
fun GroupedProductCard(gp: GroupedProduct, lang: AppLanguage, onClick: () -> Unit) {
    val isAlert = gp.totalStock <= gp.lowStockThreshold
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAlert) {
                if (MaterialTheme.colorScheme.primary == DarkPrimary) Color(0xFF2D1418) else Color(0xFFFFF1F2)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isAlert) ColorRed.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cartoon Realistic Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (gp.category == "Dye") ColorGreen.copy(alpha = 0.12f) else ColorBlueAccent.copy(alpha = 0.12f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val iconType = when (gp.iconName) {
                        "color_bag" -> LocalCartoonIconType.DYE_BAG
                        "drum" -> LocalCartoonIconType.CHEM_DRUM
                        "bottle" -> LocalCartoonIconType.DISPATCH_METER
                        else -> LocalCartoonIconType.WAREHOUSE
                    }
                    LocalCartoonDrawnIcon(type = iconType, color = if (gp.category == "Dye") ColorGreen else ColorBlueAccent)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gp.name,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(gp.brand, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("${gp.lots.size} Lots", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${gp.totalStock} ${gp.unit}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isAlert) ColorRed else ColorGreen
                    )
                    Text(
                        text = "Total Combined",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isAlert) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ColorRed.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ColorRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lang == AppLanguage.EN) "LOW STOCK ALERT: Combined safety threshold limit breached!" else "স্টক অ্যালার্ট: সম্মিলিত মজুদ রি-অর্ডার সীমার নিচে!",
                        color = ColorRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

enum class LocalCartoonIconType {
    WAREHOUSE, DYE_BAG, CHEM_DRUM, WARNING_BELL, DISPATCH_METER, CHART_LINE
}

@Composable
fun LocalCartoonDrawnIcon(type: LocalCartoonIconType, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (type) {
            LocalCartoonIconType.WAREHOUSE -> {
                drawRect(
                    color = color,
                    topLeft = Offset(w * 0.2f, h * 0.5f),
                    size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.4f)
                )
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.1f, h * 0.5f)
                    lineTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.9f, h * 0.5f)
                    close()
                }
                drawPath(path = path, color = color)
                drawRect(
                    color = Color.White.copy(alpha = 0.8f),
                    topLeft = Offset(w * 0.38f, h * 0.65f),
                    size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.25f)
                )
            }
            LocalCartoonIconType.DYE_BAG -> {
                val sackPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.3f, h * 0.2f)
                    cubicTo(w * 0.15f, h * 0.5f, w * 0.15f, h * 0.85f, w * 0.5f, h * 0.9f)
                    cubicTo(w * 0.85f, h * 0.85f, w * 0.85f, h * 0.5f, w * 0.7f, h * 0.2f)
                    close()
                }
                drawPath(path = sackPath, color = color)
                drawCircle(color = Color.White.copy(alpha = 0.9f), radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.25f))
            }
            LocalCartoonIconType.CHEM_DRUM -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.25f, h * 0.2f),
                    size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.7f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(w * 0.25f, h * 0.45f),
                    end = Offset(w * 0.75f, h * 0.45f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(w * 0.25f, h * 0.65f),
                    end = Offset(w * 0.75f, h * 0.65f),
                    strokeWidth = 3f
                )
            }
            LocalCartoonIconType.WARNING_BELL -> {
                val bellPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    cubicTo(w * 0.3f, h * 0.25f, w * 0.2f, h * 0.5f, w * 0.2f, h * 0.75f)
                    lineTo(w * 0.8f, h * 0.75f)
                    cubicTo(w * 0.8f, h * 0.5f, w * 0.7f, h * 0.25f, w * 0.5f, h * 0.15f)
                    close()
                }
                drawPath(path = bellPath, color = color)
                drawCircle(color = color, radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.85f))
            }
            LocalCartoonIconType.DISPATCH_METER -> {
                drawCircle(
                    color = color,
                    radius = w * 0.4f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = w * 0.1f)
                )
                drawLine(
                    color = color,
                    start = Offset(w * 0.5f, h * 0.5f),
                    end = Offset(w * 0.75f, h * 0.3f),
                    strokeWidth = 5f
                )
            }
            LocalCartoonIconType.CHART_LINE -> {
                drawLine(color = color.copy(alpha = 0.3f), start = Offset(0f, h), end = Offset(w, h), strokeWidth = 3f)
                drawLine(color = color.copy(alpha = 0.3f), start = Offset(0f, 0f), end = Offset(0f, h), strokeWidth = 3f)
                val curvePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.1f, h * 0.82f)
                    quadraticTo(w * 0.4f, h * 0.7f, w * 0.55f, h * 0.4f)
                    quadraticTo(w * 0.75f, h * 0.38f, w * 0.9f, h * 0.15f)
                }
                drawPath(path = curvePath, color = color, style = Stroke(width = w * 0.12f, miter = 4f))
            }
        }
    }
}

