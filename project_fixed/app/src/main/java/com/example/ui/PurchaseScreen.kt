package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppRole
import com.example.viewmodel.InventoryViewModel
import com.example.ui.theme.ColorGreen

@Composable
fun PurchaseScreen(viewModel: InventoryViewModel) {
    val purchases by viewModel.purchases.collectAsState(initial = emptyList())
    val suppliers by viewModel.suppliers.collectAsState(initial = emptyList())
    val lang by remember { derivedStateOf { viewModel.appLanguage.value } }
    val role by remember { derivedStateOf { viewModel.userRole.value } }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Purchases, 1 = Suppliers
    var showAddSupplierDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tab row switching
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(Localization.get("nav_purchases", lang), fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(Localization.get("nav_suppliers", lang), fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            // Purchases Log List
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = ColorGreen, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Localization.get("purchase_log", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            if (purchases.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No stock purchases received yet. Use 'Inward' on any product card inside the inventory tab to register received dye and chemical chemical lots.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(purchases) { req ->
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(req.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(req.purchaseDate, fontSize = 10.sp, color = ColorGreen, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Qty Received: ${req.quantity}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Supplier: ${req.supplierName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Invoice Ref: ${req.invoiceNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    val formattedPrice = if (req.currency == "BDT") "৳ ${String.format("%,.0f", req.totalPrice)}" else "$ ${String.format("%.2f", req.totalPrice)}"
                                    Text("Total Cost: $formattedPrice", fontSize = 13.sp, fontWeight = FontWeight.Black, color = ColorGreen)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Suppliers management list
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active Wholesalers Suppliers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Guard: viewers can't add suppliers
                if (role != AppRole.VIEWER) {
                    Button(
                        onClick = { showAddSupplierDialog = true },
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(Localization.get("add_supplier", lang), fontSize = 10.sp)
                    }
                }
            }

            if (suppliers.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No industrial suppliers currently registered. Tap the button above to include new vendors.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(suppliers) { vendor ->
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(vendor.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Contact Mobile: ", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(vendor.mobile, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Headquarters Address: ", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(vendor.address, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (vendor.notes.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Vendor Notes: ${vendor.notes}", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Supplier dialog UI
    if (showAddSupplierDialog) {
        var sName by remember { mutableStateOf("") }
        var sMobile by remember { mutableStateOf("") }
        var sAddr by remember { mutableStateOf("") }
        var sNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddSupplierDialog = false },
            title = { Text(Localization.get("add_supplier", lang), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sName,
                        onValueChange = { sName = it },
                        label = { Text("Supplier Corporate Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sMobile,
                        onValueChange = { sMobile = it },
                        label = { Text(Localization.get("supplier_mobile", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sAddr,
                        onValueChange = { sAddr = it },
                        label = { Text(Localization.get("supplier_addr", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sNotes,
                        onValueChange = { sNotes = it },
                        label = { Text("Notes (Brands Supplied etc)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (sName.isNotEmpty()) {
                            viewModel.addSupplier(sName, sMobile, sAddr, sNotes) {
                                showAddSupplierDialog = false
                            }
                        }
                    }
                ) {
                    Text("Save Supplier")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSupplierDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
