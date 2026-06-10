package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.UserRole
import com.example.data.model.Product
import com.example.data.model.Lot
import com.example.data.model.OcrRecipeItem
import java.io.InputStream

enum class RecipeFlow {
    Landing,
    ManualIssue,
    RecipeScan
}

@Composable
fun RecipeScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    var currentFlow by remember { mutableStateOf(RecipeFlow.Landing) }
    
    // Render screen warning if Viewer
    if (currentRole == UserRole.Viewer) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Red.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "READ ONLY PREVENTED",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "You are logged in as Viewer. Only Admin, Manager, and Storekeepers may issue recipe dispensations.",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        return
    }

    if (currentFlow != RecipeFlow.Landing) {
        androidx.activity.compose.BackHandler {
            currentFlow = RecipeFlow.Landing
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (currentFlow == RecipeFlow.Landing) {
            // Option Selection landing menu
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "RECIPE ISSUE MODULE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "Select allocation method below to dispatch chemical & dye loads.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // 1. Manual Issue Option Card
                Card(
                    onClick = { currentFlow = RecipeFlow.ManualIssue },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "1. Manual Issue",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Select items, specify lot codes, and log dispatches",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    }
                }

                // 2. Recipe Scan Option Card
                Card(
                    onClick = { currentFlow = RecipeFlow.RecipeScan },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF9B59B6).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF9B59B6).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF9B59B6), modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "2. Recipe Scan (AI OCR)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Take photo or select formula card to auto-detect products",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    }
                }
            }
        } else {
            // sub-page header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentFlow = RecipeFlow.Landing }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentFlow == RecipeFlow.ManualIssue) "Manual Recipe Issue" else "Recipe Scan (AI OCR)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            if (currentFlow == RecipeFlow.ManualIssue) {
                ManualIssueTab(viewModel)
            } else {
                OcrRecipeScanTab(viewModel)
            }
        }
    }
}

@Composable
fun ManualIssueTab(viewModel: InventoryViewModel) {
    val productsList by viewModel.products.collectAsState()
    val lotsList by viewModel.lots.collectAsState()

    // Issue cart items built dynamically
    var cartItems by remember { mutableStateOf(listOf<ManualCartItem>()) }

    // Search and selection process
    var searchInput by remember { mutableStateOf("") }
    var matchingProducts by remember { mutableStateOf(listOf<Product>()) }
    var selectedProd by remember { mutableStateOf<Product?>(null) }
    var selectedLotNum by remember { mutableStateOf("") }
    var qtyToIssueStr by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    
    var showReviewSummary by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Trigger FIFO success overlay
    val fifoConsumptionResult by viewModel.fifoConsumptionResult.collectAsState()
    val showFifoDialog by viewModel.showFifoDialog.collectAsState()

    // Trigger matching products list filtering
    LaunchedEffect(searchInput) {
        matchingProducts = if (searchInput.isBlank()) {
            productsList
        } else {
            productsList.filter {
                it.name.contains(searchInput, ignoreCase = true) ||
                it.category.contains(searchInput, ignoreCase = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize().padding(bottom = 60.dp)
        ) {
            item {
                Text(
                    text = "Material #${cartItems.size + 1}",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Searchable Product input selection
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Tap to Select Product (Searchable)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = if (selectedProd != null) selectedProd!!.name else searchInput,
                                onValueChange = {
                                    searchInput = it
                                    if (selectedProd != null) {
                                        selectedProd = null
                                    }
                                    isDropdownExpanded = true
                                },
                                placeholder = { Text("Tap to Select Product (Searchable)") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                            )

                            // Dropdown matching results
                            DropdownMenu(
                                expanded = isDropdownExpanded && matchingProducts.isNotEmpty(),
                                onDismissRequest = { isDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                matchingProducts.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.name) },
                                        onClick = {
                                            selectedProd = item
                                            searchInput = item.name
                                            isDropdownExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Inventory,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Select Lot & Enter Quantity for selected product
            if (selectedProd != null) {
                val prod = selectedProd!!
                val availLots = lotsList.filter { it.productId == prod.id && it.quantity > 0 }
                
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Dye Stock Configuration", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Unit: ${prod.unit} • Storage: ${prod.rackNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            
                            Spacer(modifier = Modifier.height(10.dp))

                            // Lot selection chips or list
                            Text("Available Lots in stock:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            if (availLots.isEmpty()) {
                                Text("No lots found in stock for this product!", color = Color.Red, fontSize = 12.sp)
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    availLots.forEach { lot ->
                                        FilterChip(
                                            selected = selectedLotNum == lot.lotNumber,
                                            onClick = { selectedLotNum = lot.lotNumber },
                                            label = { Text("Lot ${lot.lotNumber} (${lot.quantity} ${prod.unit})") }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = qtyToIssueStr,
                                onValueChange = { qtyToIssueStr = it },
                                label = { Text("Quantity to Issue (${prod.unit})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Add More Item Button
                            Button(
                                onClick = {
                                    val qtyVal = qtyToIssueStr.toDoubleOrNull() ?: 0.0
                                    if (qtyVal > 0.0 && selectedLotNum.isNotBlank()) {
                                        cartItems = cartItems + ManualCartItem(
                                            product = prod,
                                            lotNum = selectedLotNum,
                                            qtyToIssue = qtyVal
                                        )
                                        // Reset selected options for Material #X+1
                                        selectedProd = null
                                        selectedLotNum = ""
                                        qtyToIssueStr = ""
                                        searchInput = ""
                                    }
                                },
                                enabled = selectedLotNum.isNotBlank() && (qtyToIssueStr.toDoubleOrNull() ?: 0.0) > 0.0,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add More Item")
                            }
                        }
                    }
                }
            }

            // Disp dispensation matrix list preview inside scroll
            if (cartItems.isNotEmpty()) {
                item {
                    Text("CURRENT FORMULATION DISPENSATION MATRIX", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }

                itemsIndexed(cartItems) { index, cartItem ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(cartItem.product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Lot: ${cartItem.lotNum} • Unit: ${cartItem.product.unit}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${cartItem.qtyToIssue} ${cartItem.product.unit}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = {
                                    cartItems = cartItems.toMutableList().apply { removeAt(index) }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Review Issue Summary Bottom sticky button
        if (cartItems.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(14.dp)
            ) {
                Button(
                    onClick = { showReviewSummary = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.FactCheck, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Review Issue Summary", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }

    // Interactive Full Review Dialog summary
    if (showReviewSummary) {
        AlertDialog(
            onDismissRequest = { showReviewSummary = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Review Issue Summary", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Verify and approve current formulation dispenses:", fontSize = 12.sp, color = Color.Gray)
                    
                    Box(modifier = Modifier.heightIn(max = 200.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            itemsIndexed(cartItems) { _, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp)).padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Lot: ${item.lotNum}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text("${item.qtyToIssue} ${item.product.unit}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks (Batch / Formulation Name)") },
                        placeholder = { Text("e.g. Formula B3 Cotton Batch") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        for (item in cartItems) {
                            viewModel.issueStock(
                                productId = item.product.id,
                                quantityToIssue = item.qtyToIssue,
                                remarks = "Recipe Dispatch: ${remarks.ifBlank { "Manual formulation" }}"
                            )
                        }
                        cartItems = emptyList()
                        remarks = ""
                        showReviewSummary = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CONFIRM DISPENSATION (FIFO)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewSummary = false }) {
                    Text("Modify List")
                }
            }
        )
    }

    // FIFO Consumption Dialog representation (Fulfils: FIFO Dialog Required!)
    if (showFifoDialog && fifoConsumptionResult != null) {
        val result = fifoConsumptionResult!!
        AlertDialog(
            onDismissRequest = { viewModel.showFifoDialog.value = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF2ECC71))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FIFO Audit Confirmed", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "The stock was successfully reduced based on FIFO (First-In, First-Out) criteria.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Lots Consumed Details:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    result.forEach { (lot, consumed, lotNum) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• Lot $lotNum", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Deducted $consumed", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.showFifoDialog.value = false }) {
                    Text("Done")
                }
            }
        )
    }
}

// --- Recipe OCR Scanner Tab implementation ---
@Composable
fun OcrRecipeScanTab(viewModel: InventoryViewModel) {
    val ocrItems by viewModel.scannedOcrItems.collectAsState()
    val ocrStatus by viewModel.ocrStatus.collectAsState()
    val context = LocalContext.current

    // Image Pick result activity
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    viewModel.processOcrRecipeImage(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (ocrStatus == "Scanning") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "PROCESSING OCR EXTRACTIONS...",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Gemini 2.0 is reading formulation lines.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            }
            return
        }

        if (ocrItems.isEmpty()) {
            // OCR Intro Selection Screen
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Gemini Vision OCR Formulation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "Take a photo of a dyeing formulation recipe or upload from gallery. " +
                                "Gemini will extract products, lots, and weights instantly, and prepare dispensation queues.",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SELECT FORMULA SHEET")
                    }
                }
            }
        } else {
            // OCR extracted content confirm preview (Supports: Edit, Delete, Add Missing Item)
            var currentOcrStateList by remember { mutableStateOf(ocrItems) }
            
            // Add custom dialog item states
            var showAddDialog by remember { mutableStateOf(false) }
            var editIndex by remember { mutableStateOf(-1) }
            var editName by remember { mutableStateOf("") }
            var editQty by remember { mutableStateOf("") }

            Text(
                "GEMINI-EXTRACTED CHECKLIST PREVIEW",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(currentOcrStateList) { index, item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (item.lotNumber.isNotBlank()) {
                                    Text("Lot suggested: ${item.lotNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${item.quantity} KG", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // Edit Button
                                IconButton(onClick = {
                                    editIndex = index
                                    editName = item.productName
                                    editQty = item.quantity.toString()
                                    showAddDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                }

                                // Delete Button
                                IconButton(onClick = {
                                    currentOcrStateList = currentOcrStateList.toMutableList().apply { removeAt(index) }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Floating actions for Edit/Add Missing Items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        editIndex = -1
                        editName = ""
                        editQty = ""
                        showAddDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Missing Item")
                }

                Button(
                    onClick = {
                        viewModel.confirmOcrRecipeChanges(currentOcrStateList)
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CONFIRM DISPENSATIONS")
                }
            }

            // Add or Edit item Dialog
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text(if (editIndex == -1) "Add Missing Formulation Loading" else "Edit Loading") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Product Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editQty,
                                onValueChange = { editQty = it },
                                label = { Text("Quantity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val dQty = editQty.toDoubleOrNull() ?: 0.0
                                if (editName.isNotBlank() && dQty > 0.0) {
                                    if (editIndex == -1) {
                                        // Add
                                        currentOcrStateList = currentOcrStateList + OcrRecipeItem(productName = editName, quantity = dQty)
                                    } else {
                                        // Edit
                                        currentOcrStateList = currentOcrStateList.toMutableList().apply {
                                            set(editIndex, OcrRecipeItem(productName = editName, quantity = dQty))
                                        }
                                    }
                                    showAddDialog = false
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

data class ManualCartItem(
    val product: Product,
    val lotNum: String,
    val qtyToIssue: Double
)
