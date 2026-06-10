package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.viewmodel.InventoryViewModel
import com.example.data.model.Product
import com.example.data.model.Lot
import com.example.data.model.StockMovement
import java.io.File
import java.io.FileWriter

@Composable
fun AnalyticsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val lots by viewModel.lots.collectAsState()
    val movements by viewModel.stockMovements.collectAsState()

    var selectedInterval by remember { mutableStateOf(1) } // 0: Daily, 1: Monthly, 2: Yearly

    // Grouping calculations for Category breakdown
    val categoryStats = products.map { prod ->
        val totalStock = lots.filter { it.productId == prod.id }.sumOf { it.quantity }
        Pair(prod.category, totalStock)
    }.groupBy({ it.first }, { it.second })
        .mapValues { entry -> entry.value.sum() }

    // Aggregate values for monthly consumption
    val consumptionTotal = movements.filter { it.type == "OUT" }.sumOf { it.quantity }
    val stockInTotal = movements.filter { it.type == "IN" }.sumOf { it.quantity }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "DyeChem Factory Reports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Live analysis of material consumptions",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                // CSV Export Trigger Button
                Button(
                    onClick = { exportAndShareCsv(context, products, lots, movements) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("export_csv_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export CSV", fontSize = 11.sp)
                }
            }
        }

        // Interval selector chips
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Daily Report", "Monthly Report", "Yearly Report").forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedInterval == index,
                        onClick = { selectedInterval = index },
                        label = { Text(title) }
                    )
                }
            }
        }

        // Stock Movement area chart
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Stock Movement Flow (IN vs OUT)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Aggregate metric: In: $stockInTotal | Out (dispensed): $consumptionTotal",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    MovementFlowChart(stockIn = stockInTotal, stockOut = consumptionTotal)
                }
            }
        }

        // Consumption category breakdown chart
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Category Volume Breakdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    CategoryBreakdownChart(categoryStats)
                }
            }
        }

        // Table list of historic movements
        item {
            Text(
                "RECENT MOVEMENT STATS LOG",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
        }

        if (movements.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No transactions logged yet.")
                }
            }
        } else {
            items(movements.take(15)) { log ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                log.productName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            val fDate = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", log.date)
                            Text(
                                "Lot ${log.lotNumber} • $fDate • ${log.remarks}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Text(
                            text = "${if (log.type == "IN") "+" else "-"}${log.quantity}",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = if (log.type == "IN") Color(0xFF2ECC71) else Color(0xFFFF4C00)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovementFlowChart(stockIn: Double, stockOut: Double) {
    val totalMv = stockIn + stockOut
    val inPercent = if (totalMv > 0) (stockIn / totalMv).toFloat() else 0.5f
    val outPercent = if (totalMv > 0) (stockOut / totalMv).toFloat() else 0.5f

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (inPercent > 0.0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(inPercent)
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF2ECC71), Color(0xFF27AE60))),
                                RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                            )
                    )
                }
                if (outPercent > 0.0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(outPercent)
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFFFF5252), Color(0xFFFF1744))),
                                RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(Color(0xFF2ECC71), CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stock In (${String.format("%.1f", inPercent * 100)}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF5252), CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Dye out (${String.format("%.1f", outPercent * 100)}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CategoryBreakdownChart(stats: Map<String, Double>) {
    val totalVal = stats.values.sum()
    if (totalVal <= 0.0) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text("No active categorization stock values.")
        }
        return
    }

    // Color wedges map matching categories
    val colorMap = mapOf(
        "Chemicals" to Color(0xFF00ADB5),
        "Dyes" to Color(0xFFFF9F43),
        "Liquid Colors" to Color(0xFF9575CD),
        "Powder Colors" to Color(0xFF4DD0E1),
        "Auxiliaries" to Color(0xFFF0B27A)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        stats.forEach { (cat, stock) ->
            val fraction = (stock / totalVal).toFloat()
            val color = colorMap[cat] ?: Color.Gray

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${String.format("%.1f", stock)} (${String.format("%.1f", fraction * 100)}%)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Progress Bar representation of the category width
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .background(color, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

// Complete real CSV Export and Android Share Intent capability
private fun exportAndShareCsv(
    context: Context,
    products: List<Product>,
    lots: List<Lot>,
    movements: List<StockMovement>
) {
    try {
        val cachePath = File(context.cacheDir, "csv_exports")
        cachePath.mkdirs()
        val file = File(cachePath, "DyeChem_Inventory_Report.csv")
        val writer = FileWriter(file)

        // Title row
        writer.append("DyeChem Smart Inventory Pro report\n")
        writer.append("Export Time, ${System.currentTimeMillis()}\n\n")

        // Products header
        writer.append("Products Inventory List\n")
        writer.append("Product ID, Name, Category, Rack, Low Stock Limit, Unit, Packaging, Color, Raw Stock\n")
        products.forEach { p ->
            val sumStock = lots.filter { it.productId == p.id }.sumOf { it.quantity }
            writer.append("${p.id}, ${p.name}, ${p.category}, ${p.rackNumber}, ${p.lowStockLimit}, ${p.unit}, ${p.packagingType}, ${p.dyeColor}, $sumStock\n")
        }

        writer.append("\nTransactions Stock Movement log\n")
        writer.append("Trans ID, Product, Lot Number, Direction, Quantity, Entry Time, Remarks\n")
        movements.forEach { m ->
            writer.append("${m.id}, ${m.productName}, ${m.lotNumber}, ${m.type}, ${m.quantity}, ${m.date}, ${m.remarks}\n")
        }

        writer.flush()
        writer.close()

        // Expose via FileProvider so other apps can read it
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "DyeChem factory stock records")
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Inventory CSV Report"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
