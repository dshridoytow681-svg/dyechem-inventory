package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.AppScreen
import com.example.data.model.StockMovement
import com.example.data.model.Product
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val stockMovements by viewModel.stockMovements.collectAsState()
    val products by viewModel.products.collectAsState()

    // Filtering Period State: 0 = Daily, 1 = Monthly, 2 = Yearly
    var selectedPeriodTab by remember { mutableStateOf(1) } // Default to Monthly (মাসিক)
    
    // Filtering Category State: 0 = All (সব), 1 = Dyes (ডাই), 2 = Chemicals (কেমিক্যাল)
    var selectedCategoryTab by remember { mutableStateOf(0) }

    // State for specific date/month/year selections
    var calendarInstance = Calendar.getInstance()
    var currentDayOfYear by remember { mutableStateOf(calendarInstance.get(Calendar.DAY_OF_YEAR)) }
    var currentMonth by remember { mutableStateOf(calendarInstance.get(Calendar.MONTH)) } // 0..11
    var currentYear by remember { mutableStateOf(calendarInstance.get(Calendar.YEAR)) }

    // Format current date display
    val dateDisplayLabel = remember(selectedPeriodTab, currentDayOfYear, currentMonth, currentYear) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, currentYear)
        when (selectedPeriodTab) {
            0 -> {
                cal.set(Calendar.DAY_OF_YEAR, currentDayOfYear)
                SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(cal.time)
            }
            1 -> {
                cal.set(Calendar.MONTH, currentMonth)
                SimpleDateFormat("MMMM, yyyy", Locale.getDefault()).format(cal.time)
            }
            else -> {
                "$currentYear (বার্ষিক)"
            }
        }
    }

    // Process movements based on active filters
    val filteredMovements = remember(stockMovements, products, selectedPeriodTab, selectedCategoryTab, currentDayOfYear, currentMonth, currentYear) {
        stockMovements.filter { mov ->
            // 1. Filter by Period (Time range)
            val movCal = Calendar.getInstance().apply { timeInMillis = mov.date }
            val periodMatch = when (selectedPeriodTab) {
                0 -> { // Daily Match
                    movCal.get(Calendar.YEAR) == currentYear && movCal.get(Calendar.DAY_OF_YEAR) == currentDayOfYear
                }
                1 -> { // Monthly Match
                    movCal.get(Calendar.YEAR) == currentYear && movCal.get(Calendar.MONTH) == currentMonth
                }
                else -> { // Yearly Match
                    movCal.get(Calendar.YEAR) == currentYear
                }
            }

            if (!periodMatch) return@filter false

            // 2. Filter by Category
            val associatedProduct = products.find { it.id == mov.productId }
            val categoryMatch = when (selectedCategoryTab) {
                1 -> associatedProduct?.category?.contains("dye", ignoreCase = true) == true
                2 -> associatedProduct?.category?.contains("chemical", ignoreCase = true) == true
                else -> true
            }

            categoryMatch
        }
    }

    // Aggregate summary measurements
    val totalInQty = remember(filteredMovements) {
        filteredMovements.filter { it.type.equals("IN", ignoreCase = true) }.sumOf { it.quantity }
    }
    val totalOutQty = remember(filteredMovements) {
        filteredMovements.filter { it.type.equals("OUT", ignoreCase = true) }.sumOf { it.quantity }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Report Page Period Navigation Option (Daily / Monthly / Yearly)
        TabRow(
            selectedTabIndex = selectedPeriodTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedPeriodTab == 0,
                onClick = { selectedPeriodTab = 0 },
                text = { Text("Daily (আজকের)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedPeriodTab == 1,
                onClick = { selectedPeriodTab = 1 },
                text = { Text("Monthly (মাসিক)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedPeriodTab == 2,
                onClick = { selectedPeriodTab = 2 },
                text = { Text("Yearly (বার্ষিক)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Calendar Navigator Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.YEAR, currentYear)
                        when (selectedPeriodTab) {
                            0 -> {
                                cal.set(Calendar.DAY_OF_YEAR, currentDayOfYear)
                                cal.add(Calendar.DAY_OF_YEAR, -1)
                                currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
                                currentYear = cal.get(Calendar.YEAR)
                            }
                            1 -> {
                                cal.set(Calendar.MONTH, currentMonth)
                                cal.add(Calendar.MONTH, -1)
                                currentMonth = cal.get(Calendar.MONTH)
                                currentYear = cal.get(Calendar.YEAR)
                            }
                            else -> {
                                currentYear -= 1
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Period",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = dateDisplayLabel,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.YEAR, currentYear)
                        when (selectedPeriodTab) {
                            0 -> {
                                cal.set(Calendar.DAY_OF_YEAR, currentDayOfYear)
                                cal.add(Calendar.DAY_OF_YEAR, 1)
                                currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
                                currentYear = cal.get(Calendar.YEAR)
                            }
                            1 -> {
                                cal.set(Calendar.MONTH, currentMonth)
                                cal.add(Calendar.MONTH, 1)
                                currentMonth = cal.get(Calendar.MONTH)
                                currentYear = cal.get(Calendar.YEAR)
                            }
                            else -> {
                                currentYear += 1
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Period",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Category Filter Row (All, Dye category, Chemical Category)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Filter:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                listOf("All Category", "Dyes", "Chemicals").forEachIndexed { index, text ->
                    val isSelected = selectedCategoryTab == index
                    Surface(
                        onClick = { selectedCategoryTab = index },
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.height(30.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = text,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Net Movement Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF2ECC71), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Total IN Stock", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f units", totalInQty),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2ECC71)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFFE74C3C), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Total OUT Issued", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f units", totalOutQty),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE74C3C)
                        )
                    }
                }
            }

            // List Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Report Log Entries (${filteredMovements.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Brief stats
                val netValue = totalInQty - totalOutQty
                Text(
                    text = "Net: ${String.format(Locale.getDefault(), "%.1f", netValue)} units",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (netValue >= 0) Color(0xFF2ECC71) else Color(0xFFE74C3C)
                )
            }

            // Movement Entries Display
            if (filteredMovements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No transactions recorded for this period.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredMovements) { movement ->
                        val dateString = SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault()).format(Date(movement.date))
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Badge signifying Type IN vs OUT
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (movement.type.equals("IN", ignoreCase = true)) Color(0xFF2ECC71).copy(alpha = 0.15f) else Color(0xFFE74C3C).copy(alpha = 0.15f),
                                                CircleShape
                                            )
                                            .size(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (movement.type.equals("IN", ignoreCase = true)) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = if (movement.type.equals("IN", ignoreCase = true)) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = movement.productName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Lot ${movement.lotNumber} | $dateString",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        if (movement.remarks.isNotBlank()) {
                                            Text(
                                                text = "Note: ${movement.remarks}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "${if (movement.type.equals("IN", ignoreCase = true)) "+" else "-"}${movement.quantity}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (movement.type.equals("IN", ignoreCase = true)) Color(0xFF2ECC71) else Color(0xFFE74C3C)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
