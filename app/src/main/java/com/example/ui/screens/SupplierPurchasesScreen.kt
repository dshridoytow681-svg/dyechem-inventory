package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.UserRole
import com.example.data.model.Supplier
import com.example.data.model.Purchase

@Composable
fun SupplierPurchasesScreen(
    viewModel: InventoryViewModel,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val suppliers by viewModel.suppliers.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab) } // 0: Suppliers, 1: Purchase Logs
    var showAddSupplierDialog by remember { mutableStateOf(false) }

    // Forms states
    var sName by remember { mutableStateOf("") }
    var sMobile by remember { mutableStateOf("") }
    var sAddress by remember { mutableStateOf("") }
    var sNotes by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Suppliers") },
                icon = { Icon(Icons.Default.Store, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Purchase Receipts") },
                icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) }
            )
        }

        if (selectedTab == 0) {
            // Suppliers Tab
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "DyeChem Factory Certified Vendors",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (suppliers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No suppliers catalogued.")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(suppliers) { supplier ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(supplier.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            if (currentRole == UserRole.Admin) { // Admin only can deletes
                                                IconButton(
                                                    onClick = { viewModel.deleteSupplier(supplier) },
                                                    modifier = Modifier.size(18.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Mobile: ${supplier.mobile}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                                        Text("Address: ${supplier.address}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        if (supplier.notes.isNotBlank()) {
                                            Text("Notes: ${supplier.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.padding(top = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Add supplier action button
                if (currentRole == UserRole.Admin || currentRole == UserRole.Manager) {
                    FloatingActionButton(
                        onClick = { showAddSupplierDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Supplier")
                    }
                }
            }
        } else {
            // Purchase Logs Tab
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Receiving Inventory Logs (Stock-In Transactions)",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (purchases.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No invoice loadings logged.")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(purchases) { purchase ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Invoice: ${purchase.invoiceNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                        val fDate = android.text.format.DateFormat.format("yyyy-MM-dd", purchase.purchaseDate)
                                        Text(fDate.toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Vendor Supplier: ${purchase.supplierName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("Product Load: ${purchase.productName} (Lot ${purchase.lotNumber})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Received Qty: ${purchase.quantity}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Total Price Paid: ${purchase.price} ${purchase.currency}", fontSize = 12.sp, color = Color(0xFF2ECC71), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Supplier dialog
    if (showAddSupplierDialog) {
        AlertDialog(
            onDismissRequest = { showAddSupplierDialog = false },
            title = { Text("Certified Supplier Register") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = sName,
                        onValueChange = { sName = it },
                        label = { Text("Supplier Company Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sMobile,
                        onValueChange = { sMobile = it },
                        label = { Text("Contact Mobile Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sAddress,
                        onValueChange = { sAddress = it },
                        label = { Text("Operational Warehouse Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sNotes,
                        onValueChange = { sNotes = it },
                        label = { Text("Reference Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (sName.isNotBlank() && sMobile.isNotBlank()) {
                            viewModel.addSupplier(sName.trim(), sMobile.trim(), sAddress.trim(), sNotes.trim())
                            sName = ""
                            sMobile = ""
                            sAddress = ""
                            sNotes = ""
                            showAddSupplierDialog = false
                        }
                    }
                ) {
                    Text("Register Vendor")
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
