package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.UserRole
import com.example.data.model.Product
import com.example.data.model.Lot

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun ProductListScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val searchResults by viewModel.searchedProducts.collectAsState()
    val allProductsList by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val lots by viewModel.lots.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var activeFilter by remember { mutableStateOf("All Products") }
    
    // Quick stock actions dialog builders
    var showQuickInDialog by remember { mutableStateOf(false) }
    var showQuickOutDialog by remember { mutableStateOf(false) }

    // Quick Stock In states
    var quickInProduct by remember { mutableStateOf<Product?>(null) }
    var quickInLotNum by remember { mutableStateOf("") }
    var quickInQty by remember { mutableStateOf("") }
    var quickInSelectorExpanded by remember { mutableStateOf(false) }

    // Quick Stock Out states
    var quickOutProduct by remember { mutableStateOf<Product?>(null) }
    var quickOutQty by remember { mutableStateOf("") }
    var quickOutRemarks by remember { mutableStateOf("") }
    var quickOutSelectorExpanded by remember { mutableStateOf(false) }

    // Filter products based on active Chip
    val filteredProducts = remember(searchResults, activeFilter) {
        searchResults.filter { prod ->
            when (activeFilter) {
                "Dye Products" -> prod.category.contains("dye", ignoreCase = true)
                "Chemical Products" -> prod.category.contains("chemical", ignoreCase = true)
                else -> true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Enhanced Search Input with Mic icon & Clear button
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                label = { Text("Search Name, Lot, Batch, Rack...") },
                placeholder = { Text("Search Name, Lot, Batch, Rack...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.VoiceAssistant) }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Assistant")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_field"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontally scrollable capsule category filter chips (Scrolling Capsules UI)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("All Products", "Dye Products", "Chemical Products").forEach { filterText ->
                    val isActive = activeFilter == filterText
                    Surface(
                        onClick = { activeFilter = filterText },
                        shape = CircleShape,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            }
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = filterText,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isActive) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Counter & Clickable action buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cataloged Stocks (${filteredProducts.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // + New Action Button (Blue) (Admin/Manager only)
                    if (currentRole == UserRole.Admin || currentRole == UserRole.Manager) {
                        Button(
                            onClick = {
                                viewModel.selectedProduct.value = null
                                viewModel.navigateTo(AppScreen.AddEditProduct)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("+ New", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // ↑ In Action Button (Green) (Non-viewers only)
                    if (currentRole != UserRole.Viewer) {
                        Button(
                            onClick = {
                                quickInProduct = filteredProducts.firstOrNull()
                                quickInLotNum = ""
                                quickInQty = ""
                                showQuickInDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("↑ In", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // ↓ Out Action Button (Red) (Non-viewers only)
                    if (currentRole != UserRole.Viewer) {
                        Button(
                            onClick = {
                                quickOutProduct = filteredProducts.firstOrNull()
                                quickOutQty = ""
                                quickOutRemarks = ""
                                showQuickOutDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("↓ Out", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Text(
                            "No products found",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize().weight(1f)
                ) {
                    items(filteredProducts) { product ->
                        val productLots = lots.filter { it.productId == product.id }
                        val totalStock = productLots.sumOf { it.quantity }
                        val lotCount = productLots.count { it.quantity > 0.0 }

                        ProductItemCard(
                            product = product,
                            totalStock = totalStock,
                            lotCount = lotCount,
                            onClick = {
                                viewModel.selectedProduct.value = product
                                viewModel.navigateTo(AppScreen.ProductDetails)
                            }
                        )
                    }
                }
            }
        }

        // Keep standard floating button as fallback
        if (currentRole == UserRole.Admin || currentRole == UserRole.Manager) {
            FloatingActionButton(
                onClick = {
                    viewModel.selectedProduct.value = null
                    viewModel.navigateTo(AppScreen.AddEditProduct)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    }

    // Modern Quick Stock-In Dialog
    if (showQuickInDialog) {
        AlertDialog(
            onDismissRequest = { showQuickInDialog = false },
            title = { Text("Stock In Formulation Load") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Clickable Selector for Product Choice
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = quickInProduct?.name ?: "No product selected",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            label = { Text("Product Selection") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { quickInSelectorExpanded = true }
                        )
                        DropdownMenu(
                            expanded = quickInSelectorExpanded,
                            onDismissRequest = { quickInSelectorExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.81f)
                        ) {
                            allProductsList.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text(prod.name) },
                                    onClick = {
                                        quickInProduct = prod
                                        quickInSelectorExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = quickInLotNum,
                        onValueChange = { quickInLotNum = it },
                        label = { Text("Lot Code / Number") },
                        placeholder = { Text("e.g. IN-LOT-909") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = quickInQty,
                        onValueChange = { quickInQty = it },
                        label = { Text("Quantity (${quickInProduct?.unit ?: "KG"})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val prod = quickInProduct
                        val qty = quickInQty.toDoubleOrNull()
                        if (prod != null && quickInLotNum.isNotBlank() && qty != null) {
                            viewModel.performStockIn(
                                productId = prod.id,
                                lotNumber = quickInLotNum.trim(),
                                quantity = qty,
                                invoice = "Quick Stock In Menu"
                            )
                            showQuickInDialog = false
                        }
                    },
                    enabled = quickInProduct != null && quickInLotNum.isNotBlank() && quickInQty.toDoubleOrNull() != null
                ) {
                    Text("Register Stock In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickInDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modern Quick Stock-Out Dialog using core FIFO logic
    if (showQuickOutDialog) {
        AlertDialog(
            onDismissRequest = { showQuickOutDialog = false },
            title = { Text("Stock Out - Dispatch Engine") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Clickable Selector for Product Choice
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = quickOutProduct?.name ?: "No product selected",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            label = { Text("Product Selection") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { quickOutSelectorExpanded = true }
                        )
                        DropdownMenu(
                            expanded = quickOutSelectorExpanded,
                            onDismissRequest = { quickOutSelectorExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.81f)
                        ) {
                            allProductsList.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text(prod.name) },
                                    onClick = {
                                        quickOutProduct = prod
                                        quickOutSelectorExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = quickOutQty,
                        onValueChange = { quickOutQty = it },
                        label = { Text("Quantity to Issue (${quickOutProduct?.unit ?: "KG"})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = quickOutRemarks,
                        onValueChange = { quickOutRemarks = it },
                        label = { Text("Remarks (Purpose / Lot)") },
                        placeholder = { Text("e.g. Dispensing for Dyeing Batch") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val prod = quickOutProduct
                        val qty = quickOutQty.toDoubleOrNull()
                        if (prod != null && qty != null) {
                            viewModel.issueStock(
                                productId = prod.id,
                                quantityToIssue = qty,
                                remarks = quickOutRemarks.ifBlank { "Quick stock dispensation out" }
                            )
                            showQuickOutDialog = false
                        }
                    },
                    enabled = quickOutProduct != null && quickOutQty.toDoubleOrNull() != null
                ) {
                    Text("Dispatch FIFO")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickOutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    totalStock: Double,
    lotCount: Int,
    onClick: () -> Unit
) {
    val isLowStock = totalStock < product.lowStockLimit

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isLowStock) Color(0xFFFF4C00).copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) Color(0xFFFF4C00).copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dynamic Packaging Icon representation
            PackagingIcon(
                packagingType = product.packagingType,
                dyeColor = product.dyeColor,
                modifier = Modifier.size(46.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${product.category} • Rack ${product.rackNumber}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$lotCount Lots",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (isLowStock) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF4C00).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LOW STOCK",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF4C00)
                            )
                        }
                    }
                }
            }

            // Clean, non-drum displays (display unit strictly, e.g. "15 KG", never "15 Drum")
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$totalStock ${product.unit}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = if (isLowStock) Color(0xFFFF4C00) else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Total Stock",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun PackagingIcon(
    packagingType: String,
    dyeColor: String,
    modifier: Modifier = Modifier
) {
    // Resolve solid color from dyeColor
    val colorResolve = when (dyeColor.lowercase()) {
        "red" -> Color(0xFFE84545)
        "blue" -> Color(0xFF3282B8)
        "yellow" -> Color(0xFFFDD835)
        "black" -> Color(0xFF212121)
        else -> MaterialTheme.colorScheme.primary
    }

    // Resolve structural vector icon representing the packaging class
    val iconResolve = when (packagingType.lowercase()) {
        "carton" -> Icons.Default.AllInbox
        "drum" -> Icons.Default.OilBarrel
        "jar" -> Icons.Default.CoffeeMaker
        "bottle" -> Icons.Default.WineBar
        "container" -> Icons.Default.Inventory2
        "bucket" -> Icons.Default.DeleteOutline
        else -> Icons.Default.LocalMall // Default to Bag
    }

    Box(
        modifier = modifier
            .background(colorResolve.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(1.dp, colorResolve.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconResolve,
            contentDescription = null,
            tint = colorResolve,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// --- Product DETAILS Screen ---
@Composable
fun ProductDetailsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val product by viewModel.selectedProduct.collectAsState()
    val lots by viewModel.lots.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No product selected")
        }
        return
    }

    val prod = product!!
    val productLots = lots.filter { it.productId == prod.id }
    val totalStock = productLots.sumOf { it.quantity }

    var localLotNum by remember { mutableStateOf("") }
    var localLotQty by remember { mutableStateOf("") }
    var showQuickStockInDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Back Button & Operations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.ProductList) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Row {
                if (currentRole == UserRole.Admin || currentRole == UserRole.Manager) {
                    IconButton(onClick = {
                        viewModel.navigateTo(AppScreen.AddEditProduct)
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Product")
                    }
                }
                if (currentRole == UserRole.Admin) { // Admin has ONLY delete access
                    IconButton(onClick = {
                        viewModel.deleteProduct(prod)
                        viewModel.navigateTo(AppScreen.ProductList)
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = Color.Red)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Product Summary Header
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PackagingIcon(prod.packagingType, prod.dyeColor, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = prod.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Category: ${prod.category} | Packaging: ${prod.packagingType}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Storage Rack Location: ${prod.rackNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Active stock",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "$totalStock ${prod.unit}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = if (totalStock < prod.lowStockLimit) Color(0xFFFF4C00) else MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Low Stock limit alert",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "${prod.lowStockLimit} ${prod.unit}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LOT MATRIX BATCHES (${productLots.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            // Stock In Quick Button (Restricted by viewer)
            if (currentRole != UserRole.Viewer) {
                Button(
                    onClick = { showQuickStockInDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stock In", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Lots list
        if (productLots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No batch lots present. Click 'Stock In' to add dynamic product loads.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(productLots) { lot ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tag,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Lot ${lot.lotNumber}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val entryFormatted = android.text.format.DateFormat.format("yyyy-MM-dd", lot.entryDate)
                                    Text(
                                        text = "Loaded: $entryFormatted",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${lot.quantity} ${prod.unit}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Init: ${lot.initialQuantity} ${prod.unit}",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Quick Stock In Dialog implementation (Real Database function)
    if (showQuickStockInDialog) {
        AlertDialog(
            onDismissRequest = { showQuickStockInDialog = false },
            title = { Text("Stock In - ${prod.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = localLotNum,
                        onValueChange = { localLotNum = it },
                        label = { Text("Lot Number") },
                        placeholder = { Text("e.g. HP004") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = localLotQty,
                        onValueChange = { localLotQty = it },
                        label = { Text("Quantity (${prod.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = localLotQty.toDoubleOrNull()
                        if (localLotNum.isNotBlank() && qty != null) {
                            viewModel.performStockIn(
                                productId = prod.id,
                                lotNumber = localLotNum.trim(),
                                quantity = qty,
                                invoice = "Manual Quick Stock In"
                            )
                            localLotNum = ""
                            localLotQty = ""
                            showQuickStockInDialog = false
                        }
                    }
                ) {
                    Text("Confirm Stock In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickStockInDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- ADD / EDIT Product Register Form Screen ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditProductScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    
    val categoryOptions = listOf("Dyes", "Chemicals", "Liquid Colors", "Powder Colors", "Auxiliaries")
    val packagingOptions = listOf("Bag", "Drum", "Jar", "Bottle", "Carton", "Container", "Bucket")
    val allowedUnits = listOf("KG", "Gram", "Liter", "ML", "Piece", "Meter")
    val dyeColorOptions = listOf("None", "Red", "Blue", "Black", "Yellow")

    // Component Form States
    var name by remember { mutableStateOf(selectedProduct?.name ?: "") }
    var category by remember { mutableStateOf(selectedProduct?.category ?: categoryOptions[0]) }
    var rackNumber by remember { mutableStateOf(selectedProduct?.rackNumber ?: "") }
    var lowStockLimitText by remember { mutableStateOf(selectedProduct?.lowStockLimit?.toString() ?: "10.0") }
    var unit by remember { mutableStateOf(selectedProduct?.unit ?: allowedUnits[0]) }
    var packagingType by remember { mutableStateOf(selectedProduct?.packagingType ?: packagingOptions[0]) }
    var dyeColor by remember { mutableStateOf(selectedProduct?.dyeColor ?: dyeColorOptions[0]) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var packagingExpanded by remember { mutableStateOf(false) }
    var dyeColorExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.ProductList) }) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
            }
            Text(
                text = if (selectedProduct == null) "Register New Product" else "Modify Product Record",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    placeholder = { Text("e.g. Caustic Soda Flakes, Hydrogen Peroxide...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_product_name"),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Category Selection Dropdown
            item {
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("Category Classification") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        enabled = false, // delegate to clicking box
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        categoryOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    category = opt
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Rack Storage Number Input
            item {
                OutlinedTextField(
                    value = rackNumber,
                    onValueChange = { rackNumber = it },
                    label = { Text("Storage Rack Location Coordinates") },
                    placeholder = { Text("e.g. Rack A01, Rack B04") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Low Stock Limit Alert Threshold
            item {
                OutlinedTextField(
                    value = lowStockLimitText,
                    onValueChange = { lowStockLimitText = it },
                    label = { Text("Low Stock Alert Limit Threshold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("e.g. 20.0, 50.0") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Allowed Unit Classification
            item {
                Box {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        label = { Text("Measure Unit") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { unitExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    DropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        allowedUnits.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    unit = opt
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Packaging Category class
            item {
                Box {
                    OutlinedTextField(
                        value = packagingType,
                        onValueChange = {},
                        label = { Text("Packaging Type Class") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { packagingExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    DropdownMenu(
                        expanded = packagingExpanded,
                        onDismissRequest = { packagingExpanded = false }
                    ) {
                        packagingOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    packagingType = opt
                                    packagingExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Dye Color configuration
            item {
                Box {
                    OutlinedTextField(
                        value = dyeColor,
                        onValueChange = {},
                        label = { Text("Dye Color Code (if applicable)") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dyeColorExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    DropdownMenu(
                        expanded = dyeColorExpanded,
                        onDismissRequest = { dyeColorExpanded = false }
                    ) {
                        dyeColorOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    dyeColor = opt
                                    dyeColorExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Submit Button (Enabled based on form validity)
        Button(
            onClick = {
                val lowLimit = lowStockLimitText.toDoubleOrNull() ?: 10.0
                if (name.isNotBlank() && rackNumber.isNotBlank()) {
                    viewModel.saveProduct(
                        id = selectedProduct?.id ?: 0L,
                        name = name.trim(),
                        category = category,
                        rackNumber = rackNumber.trim(),
                        lowLimit = lowLimit,
                        unit = unit,
                        packagingType = packagingType,
                        dyeColor = dyeColor
                    )
                    viewModel.navigateTo(AppScreen.ProductList)
                }
            },
            enabled = name.isNotBlank() && rackNumber.isNotBlank(),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_product_button")
        ) {
            Text(
                if (selectedProduct == null) "CONFIRM REGISTRATION" else "SAVE RECORD CHANGES",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
