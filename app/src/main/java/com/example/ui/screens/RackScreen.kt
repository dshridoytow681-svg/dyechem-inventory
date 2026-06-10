package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.CustomRack
import com.example.data.model.Product
import com.example.data.model.Lot

@Composable
fun RackScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val lots by viewModel.lots.collectAsState()
    val customRacks by viewModel.customRacks.collectAsState()

    // Union of product rack numbers and custom created racks
    val allRackNames = remember(products, customRacks) {
        (products.map { it.rackNumber } + customRacks.map { it.name })
            .filter { it.isNotBlank() }
            .toSet()
            .toList()
            .sorted()
    }

    // Maintain state of expanded rack numbers
    var expandedRacks by remember { mutableStateOf(setOf<String>()) }

    // Dialog state for adding a new storage rack
    var showAddRackDialog by remember { mutableStateOf(false) }
    var newRackName by remember { mutableStateOf("") }
    var newRackCapacity by remember { mutableStateOf("") }
    var newRackDescription by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Storage Header with clean + Add New Rack action button (Top-Right aligned!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Storage Racks Distribution Grid",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = { showAddRackDialog = true },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add New Rack",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (allRackNames.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No designated storage racks found.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allRackNames) { rackNumber ->
                    val rackProducts = products.filter { it.rackNumber == rackNumber }
                    val customRackDetail = customRacks.find { it.name.trim().lowercase() == rackNumber.trim().lowercase() }
                    val isExpanded = expandedRacks.contains(rackNumber)

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            // Expandable Header Row with Dropdown Arrows
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        expandedRacks = if (isExpanded) {
                                            expandedRacks - rackNumber
                                        } else {
                                            expandedRacks + rackNumber
                                        }
                                    }
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = rackNumber,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${rackProducts.size} Products Stored",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand info",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }

                            // Expanded Contents Display
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Render custom rack detail (Capacity and Description notes if registered)
                                    if (customRackDetail != null) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Rack Capacity Status",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "Max Limit: ${customRackDetail.maxCapacity.toInt()} units/KG",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                            if (customRackDetail.description.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Location Notes: ${customRackDetail.description}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    }

                                    // Render products assigned to this rack
                                    if (rackProducts.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Default.FolderOpen,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "No products stored yet in this rack.",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    } else {
                                        rackProducts.forEach { product ->
                                            val productLots = lots.filter { it.productId == product.id }
                                            val totalStock = productLots.sumOf { it.quantity }

                                            Column(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = product.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "$totalStock ${product.unit}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                // Show Lots list inside rack
                                                if (productLots.isEmpty()) {
                                                    Text(
                                                        "No active lots in stock",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                    )
                                                } else {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        productLots.forEach { lot ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(
                                                                        MaterialTheme.colorScheme.primaryContainer,
                                                                        RoundedCornerShape(4.dp)
                                                                    )
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = "Lot ${lot.lotNumber}: ${lot.quantity} ${product.unit}",
                                                                    fontSize = 9.sp,
                                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                    fontWeight = FontWeight.Medium
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
                    }
                }
            }
        }
    }

    // "Create New Rack" dialog module modal
    if (showAddRackDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddRackDialog = false
                newRackName = ""
                newRackCapacity = ""
                newRackDescription = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AddBox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add New Storage Rack",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newRackName,
                        onValueChange = { newRackName = it },
                        label = { Text("Rack Name / Code") },
                        placeholder = { Text("e.g. Rack D01") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newRackCapacity,
                        onValueChange = { newRackCapacity = it },
                        label = { Text("Max Capacity (Number of Products or KG)") },
                        placeholder = { Text("e.g. 500") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newRackDescription,
                        onValueChange = { newRackDescription = it },
                        label = { Text("Description / Location Notes (Optional)") },
                        placeholder = { Text("e.g. Row 3, Chemical Storage South") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cap = newRackCapacity.toDoubleOrNull() ?: 0.0
                        if (newRackName.isNotBlank() && cap > 0.0) {
                            viewModel.addCustomRack(
                                name = newRackName.trim(),
                                maxCapacity = cap,
                                description = newRackDescription.trim()
                            )
                            // Reset and dismiss
                            newRackName = ""
                            newRackCapacity = ""
                            newRackDescription = ""
                            showAddRackDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = newRackName.isNotBlank() && (newRackCapacity.toDoubleOrNull() ?: 0.0) > 0.0
                ) {
                    Text("Create Rack")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        newRackName = ""
                        newRackCapacity = ""
                        newRackDescription = ""
                        showAddRackDialog = false
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
