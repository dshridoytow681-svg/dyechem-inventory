package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.InventoryViewModel
import com.example.viewmodel.RecipeConsumptionProposal
import com.example.viewmodel.GroupedProduct
import com.example.data.ProductEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(viewModel: InventoryViewModel) {
    val lang by remember { derivedStateOf { viewModel.appLanguage.value } }
    val dbProductsFlow = remember { viewModel.products }
    val dbProducts by dbProductsFlow.collectAsState(initial = emptyList())
    
    // Switch between 0 = Label/Lot Scanner, 1 = Recipe OCR Consumption
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Aesthetic material Tab selectors
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                text = { Text(if (lang == AppLanguage.EN) "Label Scanner" else "লেবেল স্ক্যানার", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Default.DocumentScanner, contentDescription = null) },
                text = { Text(if (lang == AppLanguage.EN) "Recipe OCR" else "রেসিপি ওসিআর", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
        
        if (selectedTab == 0) {
            // ==========================================
            // TAB 0: ORIGINAL LABEL & QR CODE SCANNER
            // ==========================================
            LabelScannerView(viewModel = viewModel, lang = lang)
        } else {
            // ==========================================
            // TAB 1: DYNAMIC RECIPE OCR CONSUMPTION SCREEN
            // ==========================================
            RecipeOcrConsumptionView(viewModel = viewModel, dbProducts = dbProducts, lang = lang)
        }
    }
}

@Composable
fun LabelScannerView(viewModel: InventoryViewModel, lang: AppLanguage) {
    val scannedItem by remember { derivedStateOf { viewModel.scannedProduct.value } }
    val isScanning by remember { derivedStateOf { viewModel.scanModeActive.value } }
    val statusMsg by remember { derivedStateOf { viewModel.scanStatusMessage.value } }
    
    // Laser sweep animation
    val infiniteTransition = rememberInfiniteTransition(label = "LaserTransition")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserOffset"
    )

    // Preloaded interactive target lots representing the SQLite database cards
    val interactiveTags = listOf(
        DyeStickerTag("Reactive Red Tag", "D-RE-302", "Dye Pack", "LOT-100", "Rack A-3"),
        DyeStickerTag("Disperse Blue Tag", "D-BL-400", "Dye Packet", "LOT-120", "Rack A-5"),
        DyeStickerTag("Sodium Hydro Tag", "C-HY-900", "Chemical Drum", "LOT-205", "Rack B-2"),
        DyeStickerTag("Acetic Acid Label", "C-AC-112", "Liquid Carboy", "LOT-112", "Rack C-1"),
        DyeStickerTag("Caustic Soda Sack", "C-CA-300", "Sack Lot", "LOT-811", "Rack B-4"),
        DyeStickerTag("Hydrogen Peroxide Label", "C-PE-500", "Drum Lot", "LOT-300", "Rack C-3")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Localization.get("camera_title", lang),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = Localization.get("ocr_info", lang),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val strokeW = 4.dp.toPx()
                    val len = 24.dp.toPx()
                    
                    drawLine(Color.White, Offset(20.dp.toPx(), 20.dp.toPx()), Offset(20.dp.toPx() + len, 20.dp.toPx()), strokeWidth = strokeW)
                    drawLine(Color.White, Offset(20.dp.toPx(), 20.dp.toPx()), Offset(20.dp.toPx(), 20.dp.toPx() + len), strokeWidth = strokeW)
                    
                    drawLine(Color.White, Offset(w - 20.dp.toPx(), 20.dp.toPx()), Offset(w - 20.dp.toPx() - len, 20.dp.toPx()), strokeWidth = strokeW)
                    drawLine(Color.White, Offset(w - 20.dp.toPx(), 20.dp.toPx()), Offset(w - 20.dp.toPx(), 20.dp.toPx() + len), strokeWidth = strokeW)
                    
                    drawLine(Color.White, Offset(20.dp.toPx(), h - 20.dp.toPx()), Offset(20.dp.toPx() + len, h - 20.dp.toPx()), strokeWidth = strokeW)
                    drawLine(Color.White, Offset(20.dp.toPx(), h - 20.dp.toPx()), Offset(20.dp.toPx(), h - 20.dp.toPx() - len), strokeWidth = strokeW)
                    
                    drawLine(Color.White, Offset(w - 20.dp.toPx(), h - 20.dp.toPx()), Offset(w - 20.dp.toPx() - len, h - 20.dp.toPx()), strokeWidth = strokeW)
                    drawLine(Color.White, Offset(w - 20.dp.toPx(), h - 20.dp.toPx()), Offset(w - 20.dp.toPx(), h - 20.dp.toPx() - len), strokeWidth = strokeW)
                    
                    if (isScanning) {
                        val currentY = h * laserOffset
                        drawLine(
                            color = Color(0xFF00FFCC),
                            start = Offset(22.dp.toPx(), currentY),
                            end = Offset(w - 22.dp.toPx(), currentY),
                            strokeWidth = 5f
                        )
                    }
                }

                if (isScanning) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00FFCC), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusMsg,
                            color = Color(0xFF00FFCC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else if (scannedItem != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = ColorGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Localization.get("ocr_success", lang),
                                color = ColorGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        
                        Text(
                            text = scannedItem!!.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Product Code:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(scannedItem!!.code, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Stored Lot Number:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(scannedItem!!.lotNumber, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Physical Location:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("${scannedItem!!.rackNumber} (${scannedItem!!.warehouseLocation})", fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Ledger Stock:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            val isCrit = scannedItem!!.currentStock <= scannedItem!!.lowStockThreshold
                            Text("${scannedItem!!.currentStock} ${scannedItem!!.unit}", fontWeight = FontWeight.Black, color = if (isCrit) ColorRed else ColorGreen, fontSize = 12.sp)
                        }
                        
                        Button(
                            onClick = { viewModel.scannedProduct.value = null },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Clear Viewport Scan Again", fontSize = 11.sp)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(46.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Localization.get("scan_instruction_1", lang),
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Text(
            text = Localization.get("sample_scanner_header", lang) + " (Click one to trigger simulated scan)",
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("tag_deck_list")
        ) {
            items(interactiveTags) { tag ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.simulateLabelScan(tag.code) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = tag.originalTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Row {
                                Text("Code: ${tag.code}  |  ", fontSize = 11.sp)
                                Text("Lot: ${tag.lot}", fontSize = 11.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: NEW RECIPE OCR CONSUMPTION VIEWPORT WORKFLOW
// -------------------------------------------------------------
@Composable
fun RecipeOcrConsumptionView(
    viewModel: InventoryViewModel,
    dbProducts: List<ProductEntity>,
    lang: AppLanguage
) {
    val coroutineScope = rememberCoroutineScope()
    
    val groupedDbProducts = remember(dbProducts) {
        dbProducts.groupBy { it.name }.map { entry ->
            val firstItem = entry.value.firstOrNull()
            GroupedProduct(
                name = entry.key,
                category = firstItem?.category ?: "Chemical",
                brand = firstItem?.brand ?: "Generic",
                unit = firstItem?.unit ?: "KG",
                iconName = firstItem?.iconName ?: "science",
                totalStock = entry.value.sumOf { it.currentStock },
                lotCount = entry.value.size,
                lowStockThreshold = firstItem?.lowStockThreshold ?: 50.0,
                lots = entry.value
            )
        }
    }
    
    // OCR scanning simulation state machine
    var scanState by remember { mutableStateOf("IDLE") } // "IDLE", "SCANNING", "PREVIEW"
    var currentOcrProgressText by remember { mutableStateOf("") }
    var showSourceSelectorDialog by remember { mutableStateOf(false) }
    
    // Captured recipe proposals
    val ocrProposals = remember { mutableStateListOf<RecipeConsumptionProposal>() }
    
    // Missing Ingredient Add Dialog variables
    var showAddMissingDialog by remember { mutableStateOf(false) }
    var selectedNewProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var customNewQtyText by remember { mutableStateOf("") }
    var showProductSelectDropdownIndex by remember { mutableStateOf<Int?>(null) }

    // Sample industrial physical recipe drafts to select for simulation
    val mockRecipes = listOf(
        MockRecipeDoc(
            title = "Reactive Dye Formula (Red Sheet)",
            ingredients = listOf(
                MockRecipeItem("Reactive Red HE3B", 45.0, "KG"),
                MockRecipeItem("Sodium Hydrosulfite 85%", 25.0, "KG")
            )
        ),
        MockRecipeDoc(
            title = "Boiler Standard Descaling setup",
            ingredients = listOf(
                MockRecipeItem("Glacial Acetic Acid 99%", 12.0, "KG"),
                MockRecipeItem("Caustic Soda Flakes", 50.0, "KG")
            )
        ),
        MockRecipeDoc(
            title = "Eco-Finishing Formula (Includes Undefined Softener)",
            ingredients = listOf(
                MockRecipeItem("Hydrogen Peroxide 50%", 30.0, "KG"),
                // Represents an unmatched item to showcase fully manual selection validation
                MockRecipeItem("Eco Silicon Softener 202X", 15.0, "KG")
            )
        )
    )

    // Helper functions
    fun findBestWarehouseMatch(extractedName: String): ProductEntity? {
        val clean = extractedName.lowercase().trim()
        
        // Exact name check
        val exact = dbProducts.find { it.name.lowercase().trim() == clean }
        if (exact != null) return exact
        
        // Token substring lookups
        val tokens = clean.split(" ")
        for (token in tokens) {
            if (token.length > 3) {
                val subMatch = dbProducts.find { it.name.lowercase().contains(token) }
                if (subMatch != null) return subMatch
            }
        }
        return null
    }

    fun startOcrSimulationWorkflow(recipe: MockRecipeDoc) {
        ocrProposals.clear()
        scanState = "SCANNING"
        showSourceSelectorDialog = false
        
        coroutineScope.launch {
            currentOcrProgressText = "1/5 Capture completed. Filtering noise..."
            delay(500)
            currentOcrProgressText = "2/5 Deep-OCR: Parsing text segments..."
            delay(500)
            currentOcrProgressText = "3/5 OCR extraction: Found ${recipe.ingredients.size} recipe ingredients..."
            delay(600)
            currentOcrProgressText = "4/5 Cross-referencing current warehouse inventory db..."
            delay(400)
            currentOcrProgressText = "5/5 Generating inventory state preview cards..."
            delay(300)
            
            // Generate matching proposals
            recipe.ingredients.forEach { item ->
                val matched = findBestWarehouseMatch(item.name)
                ocrProposals.add(
                    RecipeConsumptionProposal(
                        productName = item.name,
                        proposedLot = matched,
                        quantityToConsume = item.qty,
                        currentStock = matched?.currentStock ?: 0.0,
                        unit = matched?.unit ?: item.unit
                    )
                )
            }
            scanState = "PREVIEW"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (scanState == "IDLE") {
            // Screen 1: Capture & Selection Triggers
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = if (lang == AppLanguage.EN) "Recipe OCR Consumption" else "রেসিপি ওসিআর স্টক ক্ষয়",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (lang == AppLanguage.EN) 
                        "Scan recipe sheets instantly via camera or upload formula images. The system will auto-extract products & quantities, match existing stock, and batch stock-out instantly upon confirmation." 
                        else "সোজা ক্যামেরা অথবা গ্যালারি থেকে রেসিপি শিটের ছবি স্ক্যান করুন। সিস্টেম অটোমেটিক পণ্য ও পরিমাণ রিড করে লেজার স্টক থেকে হ্রাস বা আপডেট করবে।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // OCR Buttons: Take Photo & Choose Gallery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { showSourceSelectorDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("ocr_take_photo_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lang == AppLanguage.EN) "Take Photo" else "ছবি তুলুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    OutlinedButton(
                        onClick = { showSourceSelectorDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("ocr_choose_gallery_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lang == AppLanguage.EN) "From Gallery" else "গ্যালারি", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (scanState == "SCANNING") {
            // Screen 2: Animated OCR Progress Hud
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(68.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = if (lang == AppLanguage.EN) "Smart Recipe Parsing Active" else "রেসিপি ডেডিকেটেড ওসিআর বিশ্লেষণ সচল",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = currentOcrProgressText,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(12.dp)
                )
            }
        } else if (scanState == "PREVIEW") {
            // Screen 3: Matched OCR Preview Screen Panel
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Headline Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (lang == AppLanguage.EN) "OCR Extracted Preview" else "ওসিআর সংগৃহীত প্রিভিউ",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${ocrProposals.size} Ingredients identified",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Simple restart button
                    IconButton(
                        onClick = { scanState = "IDLE"; ocrProposals.clear() }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Scan Again", tint = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Card items list representation
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ocr_preview_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(ocrProposals) { index, proposal ->
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (proposal.proposedLot == null) 
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (proposal.proposedLot == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Line 1: Extracted metadata & delete trigger
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Extracted: \"${proposal.productName}\"",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Extracted Qty: ${proposal.quantityToConsume} ${proposal.unit}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                    
                                    // Delete Item Button
                                    IconButton(
                                        onClick = { ocrProposals.removeAt(index) },
                                        modifier = Modifier.size(36.dp).testTag("delete_ocr_item_${index}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ColorRed)
                                    }
                                }

                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                                // Line 2: Inline Quantity Editing Textbox
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = (if (lang == AppLanguage.EN) "Modify Qty: " else "পরিমাণ পরিবর্তন: "),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(100.dp)
                                    )
                                    
                                    OutlinedTextField(
                                        value = if (proposal.quantityToConsume == 0.0) "" else proposal.quantityToConsume.toString(),
                                        onValueChange = { newVal ->
                                            val parsed = newVal.toDoubleOrNull() ?: 0.0
                                            ocrProposals[index] = proposal.copy(quantityToConsume = parsed)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("edit_qty_input_${index}"),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                        placeholder = { Text("0.0", fontSize = 12.sp) }
                                    )
                                }

                                // Line 3: Matched product selection & validation errors
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = (if (lang == AppLanguage.EN) "Matched Product: " else "ম্যাচিং ডেক: "),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(100.dp)
                                    )

                                    // Dynamic Dropdown Trigger to EDIT PRODUCT selection
                                    Box(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { showProductSelectDropdownIndex = index }
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                                .testTag("matched_product_dropdown_${index}"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = proposal.proposedLot?.let { "${it.name} (Lot: ${it.lotNumber})" } 
                                                    ?: (if (lang == AppLanguage.EN) "[UNRESOLVED MATCH - SELECT NOW]" else "[অমীমাংসিত - নির্বাচন করুন]"),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (proposal.proposedLot != null) MaterialTheme.colorScheme.primary else ColorRed
                                            )
                                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }

                                        if (showProductSelectDropdownIndex == index) {
                                            SearchableProductSelectorDialog(
                                                lang = lang,
                                                groupedProducts = groupedDbProducts,
                                                onDismissRequest = { showProductSelectDropdownIndex = null },
                                                onProductSelected = { gp ->
                                                    val prod = gp.lots.firstOrNull() ?: ProductEntity(
                                                        id = 0, name = gp.name, code = "", category = gp.category,
                                                        brand = gp.brand, lotNumber = "UNKNOWN", batchNumber = "",
                                                        rackNumber = "", warehouseLocation = "", unit = gp.unit,
                                                        openingStock = 0.0, stockIn = 0.0, stockOut = 0.0,
                                                        currentStock = 0.0, lowStockThreshold = 0.0, purchasePrice = 0.0,
                                                        currency = "BDT", entryDate = "", expiryDate = "", packageType = "", iconName = ""
                                                    )
                                                    ocrProposals[index] = proposal.copy(
                                                        proposedLot = prod,
                                                        currentStock = prod.currentStock,
                                                        unit = prod.unit
                                                    )
                                                    showProductSelectDropdownIndex = null
                                                }
                                            )
                                        }
                                    }
                                }

                                // Line 4: Remaining Stock calculation HUD
                                if (proposal.proposedLot != null) {
                                    val currentStk = proposal.proposedLot.currentStock
                                    val finalRemaining = currentStk - proposal.quantityToConsume
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (finalRemaining < 0) ColorRed.copy(alpha = 0.1f) else ColorGreen.copy(alpha = 0.1f), 
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Ledger Stock: $currentStk ${proposal.unit}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Remaining Stock: $finalRemaining ${proposal.unit}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (finalRemaining < 0) ColorRed else ColorGreen
                                        )
                                    }
                                } else {
                                    // Match failure layout block
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(ColorRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp), tint = ColorRed)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Must match to an existing warehouse lot before processing.",
                                            color = ColorRed,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Control Action Buttons Row: Add Missing Item & Confirm Process Stock Out
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button: Add Missing Item
                    OutlinedButton(
                        onClick = { 
                            selectedNewProduct = dbProducts.firstOrNull()
                            customNewQtyText = ""
                            showAddMissingDialog = true 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ocr_add_missing_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (lang == AppLanguage.EN) "Add Missing Item" else "নিখোঁজ পণ্য যোগ করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Button: Automatically reduce stock, update instantly
                    val allMatched = ocrProposals.all { it.proposedLot != null }
                    val hasIngredients = ocrProposals.isNotEmpty()
                    
                    Button(
                        onClick = {
                            if (allMatched && hasIngredients) {
                                viewModel.recordOcrConsumptionList(ocrProposals.toList()) { ok ->
                                    if (ok) {
                                        ocrProposals.clear()
                                        scanState = "IDLE"
                                    }
                                }
                            }
                        },
                        enabled = allMatched && hasIngredients,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("ocr_confirm_dispatch_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lang == AppLanguage.EN) "Confirm & Process Stock Out" else "নিশ্চিত করে স্টকআউট করুন", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // DIALOG: SOURCE PHOTO SELECTOR SIMULATION
    if (showSourceSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showSourceSelectorDialog = false },
            title = { Text(if (lang == AppLanguage.EN) "Source Imaging Mode" else "ছবি সংগ্রাহক মেথড", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (lang == AppLanguage.EN) 
                            "Select recipe formula document to capture/choose (Camera/Gallery Simulation):" 
                            else "স্ক্যান বা ছবি তুলতে পছন্দসই রেসিপি ডকুমেন্ট নির্বাচন করুন:",
                        fontSize = 12.sp
                    )
                    
                    mockRecipes.forEach { recipe ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { startOcrSimulationWorkflow(recipe) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(recipe.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                recipe.ingredients.forEach { ing ->
                                    Text("• ${ing.name} : ${ing.qty} ${ing.unit}", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSourceSelectorDialog = false }) {
                    Text(if (lang == AppLanguage.EN) "Cancel" else "বাতিল")
                }
            }
        )
    }

    // DIALOG: ADD MISSING INGREDIENT/PRODUCT MANUALLY TO PREVIEW LIST
    if (showAddMissingDialog) {
        var dropdownExpanded by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showAddMissingDialog = false },
            title = { Text(if (lang == AppLanguage.EN) "Add Missing Item" else "নিখোঁজ আইটেম ফর্ম", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Pick warehouse product
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Select Product Lot:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .clickable { dropdownExpanded = true }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedNewProduct?.let { "${it.name} (Lot: ${it.lotNumber})" } ?: "Select Product",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                            
                            if (dropdownExpanded) {
                                SearchableProductSelectorDialog(
                                    lang = lang,
                                    groupedProducts = groupedDbProducts,
                                    onDismissRequest = { dropdownExpanded = false },
                                    onProductSelected = { gp ->
                                        selectedNewProduct = gp.lots.firstOrNull()
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Specify consumption quantity
                    OutlinedTextField(
                        value = customNewQtyText,
                        onValueChange = { customNewQtyText = it },
                        label = { Text("Quantity to Consume") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ocr_add_missing_qty_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = customNewQtyText.toDoubleOrNull() ?: 0.0
                        val prod = selectedNewProduct
                        if (prod != null && qty > 0.0) {
                            ocrProposals.add(
                                RecipeConsumptionProposal(
                                    productName = prod.name,
                                    proposedLot = prod,
                                    quantityToConsume = qty,
                                    currentStock = prod.currentStock,
                                    unit = prod.unit
                                )
                            )
                            showAddMissingDialog = false
                        }
                    },
                    enabled = selectedNewProduct != null && customNewQtyText.isNotEmpty()
                ) {
                    Text("Add Item")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMissingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper models for mock recipe documents
data class MockRecipeItem(
    val name: String,
    val qty: Double,
    val unit: String
)

data class MockRecipeDoc(
    val title: String,
    val ingredients: List<MockRecipeItem>
)

data class DyeStickerTag(
    val originalTitle: String,
    val code: String,
    val description: String,
    val lot: String,
    val rack: String
)
