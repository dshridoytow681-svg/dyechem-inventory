package com.example.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ProductEntity
import com.example.ui.theme.*
import com.example.viewmodel.GroupedProduct

/**
 * A highly reusable and interactive Searchable Product Selector Dialog
 * that handles case-insensitive filtering of product names, brand, category, etc.,
 * with auto-focus, smooth scrolling, and instant results.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableProductSelectorDialog(
    lang: AppLanguage,
    groupedProducts: List<GroupedProduct>,
    onDismissRequest: () -> Unit,
    onProductSelected: (GroupedProduct) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Fast, case-insensitive live filtering
    val filteredProducts = remember(searchQuery, groupedProducts) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            groupedProducts
        } else {
            groupedProducts.filter { gp ->
                gp.name.lowercase().contains(q) ||
                gp.brand.lowercase().contains(q) ||
                gp.category.lowercase().contains(q) ||
                gp.lots.any { lot ->
                    lot.lotNumber.lowercase().contains(q) ||
                    lot.batchNumber.lowercase().contains(q)
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp)
                .testTag("searchable_selection_dialog"),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (lang == AppLanguage.EN) "Select Product" else "পণ্য নির্বাচন করুন",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (lang == AppLanguage.EN) "Type keyword to find dyes or chemicals" else "রং বা কেমিক্যাল খুঁজতে টাইপ করুন",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (lang == AppLanguage.EN) "Type name (e.g. 'caus', 'hydr', 'react')" else "নাম লিখুন (যেমন: 'caus', 'hydr')",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selector_search_box"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // List header count
                Text(
                    text = if (lang == AppLanguage.EN) "Matching Dyes & Chemicals (${filteredProducts.size})" else "মিলে যাওয়া উপাদানসমূহ (${filteredProducts.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (lang == AppLanguage.EN) "No matching products found." else "কোনো উপাদান খুঁজে পাওয়া যায়নি।",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredProducts) { gp ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onProductSelected(gp)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Icon indicator
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = if (gp.category == "Dye") ColorOrange.copy(alpha = 0.1f) else ColorGreen.copy(alpha = 0.1f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (gp.category == "Dye") Icons.Default.Palette else Icons.Default.Science,
                                            contentDescription = null,
                                            tint = if (gp.category == "Dye") ColorOrange else ColorGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = gp.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${gp.brand} • ${gp.category}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${gp.totalStock} ${gp.unit}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = if (gp.isLowStock) ColorRed else ColorGreen
                                        )
                                        Text(
                                            text = if (gp.isLowStock) (if (lang == AppLanguage.EN) "Low Stock" else "কম স্টক") else (if (lang == AppLanguage.EN) "In Stock" else "পর্যাপ্ত স্টক"),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (gp.isLowStock) ColorRed else ColorGreen
                                        )
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

/**
 * An interactive form component representing the searchable product selector trigger
 * which displays the selected product and automatically shows available lots, rack number,
 * and current stock in an elegant selectable list directly below it.
 */
@Composable
fun SearchableProductSelector(
    lang: AppLanguage,
    groupedProducts: List<GroupedProduct>,
    selectedProduct: GroupedProduct?,
    selectedLot: ProductEntity?,
    onProductSelected: (GroupedProduct, ProductEntity?) -> Unit,
    onLotSelected: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Trigger Box resembling standard OutlinedTextField form component
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (selectedProduct != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    RoundedCornerShape(14.dp)
                )
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                .clickable { showDialog = true }
                .padding(16.dp)
                .testTag("product_selector_trigger")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (lang == AppLanguage.EN) "Product / Chemical" else "পণ্য / কাঁচামাল",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedProduct != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selectedProduct?.name ?: (if (lang == AppLanguage.EN) "Tap to Select Product (Searchable)" else "পণ্য নির্বাচন করতে ট্যাপ করুন (সার্চযোগ্য)"),
                        fontSize = 14.sp,
                        fontWeight = if (selectedProduct != null) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (selectedProduct != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = if (selectedProduct != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Available Lots, Rack numbers, and Current Stocks display
        if (selectedProduct != null) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = if (lang == AppLanguage.EN) "Select Available Warehouse Lot:" else "মজুদ লট নম্বর নির্বাচন করুন:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
            )

            // Lot details selection cards list
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                selectedProduct.lots.forEach { lot ->
                    val isSelected = selectedLot?.id == lot.id
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.04f)
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLotSelected(lot)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onLotSelected(lot) },
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Lot: ${lot.lotNumber}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (lot.batchNumber.isNotEmpty()) {
                                        Text(
                                            text = " (Batch: ${lot.batchNumber})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "Rack: ${lot.rackNumber} • Loc: ${lot.warehouseLocation}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${lot.currentStock} ${lot.unit}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (lot.currentStock <= lot.lowStockThreshold) ColorRed else ColorGreen
                                )
                                Text(
                                    text = if (lot.currentStock <= lot.lowStockThreshold) (if (lang == AppLanguage.EN) "Low threshold" else "সীমার নিচে") else (if (lang == AppLanguage.EN) "Available" else "পর্যাপ্ত"),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lot.currentStock <= lot.lowStockThreshold) ColorRed else ColorGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        SearchableProductSelectorDialog(
            lang = lang,
            groupedProducts = groupedProducts,
            onDismissRequest = { showDialog = false },
            onProductSelected = { gp ->
                onProductSelected(gp, gp.lots.firstOrNull())
                showDialog = false
            }
        )
    }
}
