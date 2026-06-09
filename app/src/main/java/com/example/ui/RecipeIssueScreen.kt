package com.example.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.data.RecipeIssueEntity
import com.example.ui.theme.*
import com.example.viewmodel.GroupedProduct
import com.example.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecipeIssueScreen(viewModel: InventoryViewModel) {
    val lang by remember { derivedStateOf { viewModel.appLanguage.value } }
    val groupedProducts by viewModel.groupedProductsList.collectAsState(initial = emptyList())
    val recipeIssuesList by viewModel.recipeIssues.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(0) } // 0: Options/Workflow, 1: History

    // Active sub-mode within tab 0
    // "MAIN": Option selector, "MANUAL": Manual Issue entry, "SCAN": Scan / OCR
    var issueWorkflowMode by remember { mutableStateOf("MAIN") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Headers: Recipe Hub (Issue & History)
        TabRow(selectedTabIndex = activeTab) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(if (lang == AppLanguage.EN) "Issue Materials" else "রেসিপি স্টক ইস্যু", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(if (lang == AppLanguage.EN) "History Logs" else "ইস্যু ইতিহাস", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (activeTab == 0) {
            when (issueWorkflowMode) {
                "MAIN" -> {
                    RecipeIssueMainOptions(
                        lang = lang,
                        onSelectManual = { issueWorkflowMode = "MANUAL" },
                        onSelectScan = { issueWorkflowMode = "SCAN" }
                    )
                }
                "MANUAL" -> {
                    ManualIssueWorkflow(
                        lang = lang,
                        groupedProducts = groupedProducts,
                        onBack = { issueWorkflowMode = "MAIN" },
                        onSaveRecipe = { itemsToReduce, onFinished ->
                            viewModel.saveRecipeIssue(itemsToReduce) { id ->
                                onFinished(id)
                            }
                        }
                    )
                }
                "SCAN" -> {
                    RecipeScanOcrWorkflow(
                        lang = lang,
                        groupedProducts = groupedProducts,
                        onBack = { issueWorkflowMode = "MAIN" },
                        onSaveRecipe = { itemsToReduce, onFinished ->
                            viewModel.saveRecipeIssue(itemsToReduce) { id ->
                                onFinished(id)
                            }
                        }
                    )
                }
            }
        } else {
            RecipeIssueHistoryScreen(
                lang = lang,
                issues = recipeIssuesList
            )
        }
    }
}

// ==========================================
// SCREEN 0: SELECT WORKFLOW TYPE - MAIN OPTIONS
// ==========================================
@Composable
fun RecipeIssueMainOptions(
    lang: AppLanguage,
    onSelectManual: () -> Unit,
    onSelectScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (lang == AppLanguage.EN) "🧾 Recipe Material Issue" else "🧾 রেসিপি মেটেরিয়াল ইস্যু",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (lang == AppLanguage.EN) 
                "Issue raw material lots directly to the production line. Stock will automatically be deducted from the respective warehouse lot database upon your confirmation." 
                else "উৎপাদনের সুবিধার্থে ডাইং ও কেমিক্যাল লট সরবরাহ করুন। আপনার নিশ্চিতকরণের সাথে সাথে নির্দিষ্ট শেলফ এবং লট ডাটাবেজ থেকে স্বয়ংক্রিয়ভাবে স্টক হ্রাস পাবে।",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Option 1: Manual Issue Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectManual() }
                .testTag("option_manual_issue")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == AppLanguage.EN) "1. Manual Issue" else "১. ম্যানুয়াল ইস্যু",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (lang == AppLanguage.EN) "Select items, specify lot codes, and log dispatches" else "ম্যানুয়ালি পণ্য ও লট নম্বর বেছে নিয়ে স্টক আউট করুন",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Option 2: AI Scan Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectScan() }
                .testTag("option_scan_issue")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == AppLanguage.EN) "2. Recipe Scan (AI OCR)" else "২. রেসিপি স্ক্যান (এআই ওসিআর)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (lang == AppLanguage.EN) "Take photo or select formula card to auto-detect products" else "ছবি তুলে বা আপলোড করে ফর্মুলা শিটের পণ্যগুলো অটো-ডিটেক্ট করুন",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ==========================================
// WORKFLOW A: MANUAL ISSUE MODE
// ==========================================
data class ManualIssueItem(
    var selectedGroupProduct: GroupedProduct? = null,
    var selectedLot: ProductEntity? = null,
    var quantityText: String = "",
    var isProductDropdownExpanded: Boolean = false,
    var isLotDropdownExpanded: Boolean = false
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManualIssueWorkflow(
    lang: AppLanguage,
    groupedProducts: List<GroupedProduct>,
    onBack: () -> Unit,
    onSaveRecipe: (List<Pair<ProductEntity, Double>>, onFinished: (String) -> Unit) -> Unit
) {
    val itemsList = remember { mutableStateListOf(ManualIssueItem()) }
    var currentSubStage by remember { mutableStateOf("ENTRY") } // ENTRY, SUMMARY, CONFIRMED
    var newlyCreatedIssueId by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentSubStage == "ENTRY") onBack()
                    else currentSubStage = "ENTRY"
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (lang == AppLanguage.EN) "Manual Recipe Issue" else "ম্যানুয়াল রেসিপি সরবরাহ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            when (currentSubStage) {
                "ENTRY" -> {
                    Text(
                        text = if (lang == AppLanguage.EN) "Add Materials to Issue" else "সরবরাহের জন্য কেমিক্যাল/রঙ যুক্ত করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(itemsList.size) { index ->
                            val item = itemsList[index]
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (lang == AppLanguage.EN) "Material #${index + 1}" else "উপাদান #${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (itemsList.size > 1) {
                                            IconButton(
                                                modifier = Modifier.size(24.dp),
                                                onClick = { itemsList.removeAt(index) }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ColorRed, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // A. Searchable Product Selector with available warehouse lots, shelf rack locations, and current stocks
                                    SearchableProductSelector(
                                        lang = lang,
                                        groupedProducts = groupedProducts,
                                        selectedProduct = item.selectedGroupProduct,
                                        selectedLot = item.selectedLot,
                                        onProductSelected = { gp, firstLot ->
                                            itemsList[index] = itemsList[index].copy(
                                                selectedGroupProduct = gp,
                                                selectedLot = firstLot,
                                                isProductDropdownExpanded = false
                                            )
                                        },
                                        onLotSelected = { lot ->
                                            itemsList[index] = itemsList[index].copy(
                                                selectedLot = lot,
                                                isLotDropdownExpanded = false
                                            )
                                        }
                                    )

                                    if (item.selectedGroupProduct != null) {
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // B. Quantity input
                                        OutlinedTextField(
                                            value = item.quantityText,
                                            onValueChange = { qtyVal ->
                                                itemsList[index] = itemsList[index].copy(
                                                    quantityText = qtyVal
                                                )
                                            },
                                            label = { Text(if (lang == AppLanguage.EN) "Specify Quantity to Issue (${item.selectedGroupProduct?.unit ?: ""})" else "সরবরাহের পরিমাণ (${item.selectedGroupProduct?.unit ?: ""})") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        // Balance indicator
                                        item.selectedLot?.let { lot ->
                                            val currentQuantity = item.quantityText.toDoubleOrNull() ?: 0.0
                                            val remaining = lot.currentStock - currentQuantity
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = if (lang == AppLanguage.EN) "In Stock: ${lot.currentStock} ${lot.unit}" else "মজুদ: ${lot.currentStock} ${lot.unit}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = if (lang == AppLanguage.EN) "Remains: ${String.format("%.2f", remaining)} ${lot.unit}" else "অবশিষ্ট: ${String.format("%.2f", remaining)} ${lot.unit}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (remaining < 0) ColorRed else ColorGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = { itemsList.add(ManualIssueItem()) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .testTag("add_more_item_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = if (lang == AppLanguage.EN) "+ Add More Item" else "+ উপাদান যোগ করুন",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            // Validate entry points
                            var hasErrors = false
                            val validatedProducts = mutableListOf<Pair<ProductEntity, Double>>()
                            for (item in itemsList) {
                                if (item.selectedGroupProduct == null || item.selectedLot == null) {
                                    hasErrors = true
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Please select products and lots for all items.")
                                    }
                                    break
                                }
                                val qtyOfItem = item.quantityText.toDoubleOrNull()
                                if (qtyOfItem == null || qtyOfItem <= 0.0) {
                                    hasErrors = true
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Please specify positive quantities.")
                                    }
                                    break
                                }
                                if (qtyOfItem > item.selectedLot!!.currentStock) {
                                    hasErrors = true
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Cannot issue amount larger than current stock for ${item.selectedLot!!.name}.")
                                    }
                                    break
                                }
                                validatedProducts.add(Pair(item.selectedLot!!, qtyOfItem))
                            }

                            if (!hasErrors) {
                                currentSubStage = "SUMMARY"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("validate_manual_issue_button")
                    ) {
                        Text(if (lang == AppLanguage.EN) "Review Issue Summary" else "সরবরাহের খসড়া পর্যালোচনা", fontWeight = FontWeight.Bold)
                    }
                }
                "SUMMARY" -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (lang == AppLanguage.EN) "Confirm Recipe Issue Summary" else "রেসিপি ইস্যুর চূড়ান্ত সারসংক্ষেপ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = if (lang == AppLanguage.EN) 
                                "Verify that the quantities listed below match the manufacturing sheet formula layout precisely before processing stock deductions."
                                else "সংযোজিত পণ্যসমূহের স্টক ডিক্রিমেন্ট করার পূর্বে ফর্মুলা কোডের সাথে পরিমাণ মিলিয়ে চূড়ান্তভাবে নিশ্চিত করুন।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(itemsList.size) { rIdx ->
                                    val rowItem = itemsList[rIdx]
                                    val lotItem = rowItem.selectedLot!!
                                    val amtToIssue = rowItem.quantityText.toDoubleOrNull() ?: 0.0
                                    
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(lotItem.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text("${amtToIssue} ${lotItem.unit}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = ColorOrange)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Lot: ${lotItem.lotNumber} | Shelf: ${lotItem.rackNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Remaining: ${lotItem.currentStock - amtToIssue} ${lotItem.unit}", fontSize = 11.sp, color = ColorGreen)
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentSubStage = "ENTRY" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text(if (lang == AppLanguage.EN) "Edit Entries" else "এন্ট্রি সংশোধন")
                            }

                            Button(
                                onClick = {
                                    val pairs = itemsList.map { Pair(it.selectedLot!!, it.quantityText.toDouble()) }
                                    onSaveRecipe(pairs) { issueId ->
                                        newlyCreatedIssueId = issueId
                                        currentSubStage = "CONFIRMED"
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("confirm_recipe_issue_button")
                            ) {
                                Text(if (lang == AppLanguage.EN) "Confirm Issue" else "নিশ্চিত করুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                "CONFIRMED" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(ColorGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ColorGreen, modifier = Modifier.size(44.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == AppLanguage.EN) "Recipe Stock Issued!" else "স্টক সরবরাহ সফল হয়েছে!",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${if (lang == AppLanguage.EN) "Transaction ID" else "আইডি"}: $newlyCreatedIssueId",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == AppLanguage.EN) 
                                "Lot stock balances and product aggregates were successfully reduced from the SQLite local instance registry."
                                else "এসকিউলাইট স্থানীয় রেজিস্ট্রি থেকে লট স্টকের পরিমাপ সার্থকভাবে আপডেট ও হ্রাস করা হয়েছে।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                itemsList.clear()
                                itemsList.add(ManualIssueItem())
                                currentSubStage = "ENTRY"
                                onBack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(if (lang == AppLanguage.EN) "Finish Work & Exit" else "সম্পন্ন করে বের হোন")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// WORKFLOW B: SCAN MODE (AI OCR)
// ==========================================
data class ScannedOcrItem(
    var id: String = UUID.randomUUID().toString(),
    var name: String,
    var quantity: Double,
    var selectedLot: ProductEntity? = null,
    var dropdownExpanded: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScanOcrWorkflow(
    lang: AppLanguage,
    groupedProducts: List<GroupedProduct>,
    onBack: () -> Unit,
    onSaveRecipe: (List<Pair<ProductEntity, Double>>, onFinished: (String) -> Unit) -> Unit
) {
    // Stage controller: "PHOTO_CHOOSE" -> "AI_PROCESSING" -> "PREVIEW" -> "CONFIRMED"
    var currentOcrStage by remember { mutableStateOf("PHOTO_CHOOSE") }
    var detectedItems = remember { mutableStateListOf<ScannedOcrItem>() }
    var aiProcessingText by remember { mutableStateOf("") }
    var newlyCreatedIssueId by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dropdowns configuration for missing items
    var showAddMissingItemDialog by remember { mutableStateOf(false) }

    fun runOcrAnalysisSimulation() {
        currentOcrStage = "AI_PROCESSING"
        coroutineScope.launch {
            aiProcessingText = "Uploading recipe sheet image..."
            delay(1000)
            aiProcessingText = "Analyzing pixels using AI Vision Model..."
            delay(1200)
            aiProcessingText = "Performing Optical Character Recognition (OCR)..."
            delay(1000)
            aiProcessingText = "Recognizing: Hydrogen Peroxide 10 KG\nRecognizing: Reactive Red 5 KG\nRecognizing: Acetic Acid 20 KG"
            delay(1200)
            aiProcessingText = "Linking ingredients with live database chemistry records..."
            delay(800)

            // Populate detected items with simulated OCR output matched with database
            detectedItems.clear()
            
            // Item 1: Hydrogen Peroxide
            val hpMatch = groupedProducts.find { it.name.lowercase().contains("peroxide") || it.name.lowercase().contains("hydrogen") }
            detectedItems.add(
                ScannedOcrItem(
                    name = hpMatch?.name ?: "Hydrogen Peroxide",
                    quantity = 10.0,
                    selectedLot = hpMatch?.lots?.firstOrNull()
                )
            )

            // Item 2: Reactive Red
            val rrMatch = groupedProducts.find { it.name.lowercase().contains("red") || it.name.lowercase().contains("reactive") }
            detectedItems.add(
                ScannedOcrItem(
                    name = rrMatch?.name ?: "Reactive Red",
                    quantity = 5.0,
                    selectedLot = rrMatch?.lots?.firstOrNull()
                )
            )

            // Item 3: Acetic Acid
            val aaMatch = groupedProducts.find { it.name.lowercase().contains("acetic") || it.name.lowercase().contains("acid") }
            detectedItems.add(
                ScannedOcrItem(
                    name = aaMatch?.name ?: "Acetic Acid",
                    quantity = 20.0,
                    selectedLot = aaMatch?.lots?.firstOrNull()
                )
            )

            currentOcrStage = "PREVIEW"
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentOcrStage == "PHOTO_CHOOSE") onBack()
                    else if (currentOcrStage == "PREVIEW") currentOcrStage = "PHOTO_CHOOSE"
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (lang == AppLanguage.EN) "Recipe Smart Scan (AI OCR)" else "রেসিপি স্মার্ট স্ক্যান (এআই)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            when (currentOcrStage) {
                "PHOTO_CHOOSE" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text(
                            text = if (lang == AppLanguage.EN) "Select Formula Source" else "ফর্মুলা শিটের উৎস সিলেক্ট করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (lang == AppLanguage.EN) 
                                "Take a photograph of the recipe sheet or import one from your photo gallery. AI OCR analyzes dynamic layout and structures formulas automatically." 
                                else "ফর্মুলা শিটের ছবি সরাসরি ক্যামেরা দিয়ে তুলুন অথবা গ্যালারি থেকে ইম্পোর্ট করুন। এআই রিয়েল-টাইম ছবি স্ক্যান বিশ্লেষণ করে স্টক মেলাবে।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(40.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { runOcrAnalysisSimulation() }
                                    .testTag("ocr_take_photo_button")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(if (lang == AppLanguage.EN) "Take Photo" else "ক্যামেরা ওপেন", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { runOcrAnalysisSimulation() }
                                    .testTag("ocr_gallery_button")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(if (lang == AppLanguage.EN) "Select Gallery" else "গ্যালারি সন্ধান", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                "AI_PROCESSING" -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (lang == AppLanguage.EN) "Processing AI OCR Analysis..." else "এআই ওসিআর বিশ্লেষণ করা হচ্ছে...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = aiProcessingText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
                "PREVIEW" -> {
                    Text(
                        text = if (lang == AppLanguage.EN) "AI Detected Recipe Formula Preview" else "ওসিআর শনাক্তকৃত ফর্মুলার খসড়া ও প্রাকদর্শন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = if (lang == AppLanguage.EN) 
                            "Confirm scanned items. Real-time lot quantities are matched. You can edit quantities, select/change lot codes, add missing entries, or delete erroneous detections." 
                            else "অটো-ডিটেক্টকৃত আইটেম পর্যালোচনা করুন। ম্যানুয়ালি স্টক কমাবার আগে পণ্য সংশোধন করতে, লট পরিবর্তন করতে বা ডিলিট করতে পারেন।",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(detectedItems) { ocrItem ->
                            val linkedProduct = groupedProducts.find { it.name.lowercase() == ocrItem.name.lowercase() }
                            val lotsList = linkedProduct?.lots ?: emptyList()

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Product Title
                                        Text(ocrItem.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                        
                                        // Delete element button
                                        IconButton(
                                            modifier = Modifier.size(24.dp),
                                            onClick = { detectedItems.remove(ocrItem) }
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = ColorRed, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // A. Select / Change matching lot code dropdown
                                        Box(modifier = Modifier.weight(1.2f)) {
                                            OutlinedTextField(
                                                value = ocrItem.selectedLot?.lotNumber ?: (if (lang == AppLanguage.EN) "Select Lot" else "লট চয়ন করুন"),
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Lot Number") },
                                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .clickable {
                                                        // Toggle dropdown
                                                        detectedItems.indexOf(ocrItem).let { idx ->
                                                            if (idx != -1) {
                                                                detectedItems[idx] = detectedItems[idx].copy(dropdownExpanded = true)
                                                            }
                                                        }
                                                    }
                                            )

                                            DropdownMenu(
                                                expanded = ocrItem.dropdownExpanded,
                                                onDismissRequest = {
                                                    val idx = detectedItems.indexOf(ocrItem)
                                                    if (idx != -1) detectedItems[idx] = detectedItems[idx].copy(dropdownExpanded = false)
                                                }
                                            ) {
                                                if (lotsList.isEmpty()) {
                                                    DropdownMenuItem(
                                                        text = { Text("No lots registered for this product name!") },
                                                        onClick = {}
                                                    )
                                                } else {
                                                    lotsList.forEach { singleLot ->
                                                        DropdownMenuItem(
                                                            text = { Text("Lot: ${singleLot.lotNumber} (Stock: ${singleLot.currentStock} ${singleLot.unit})") },
                                                            onClick = {
                                                                val idx = detectedItems.indexOf(ocrItem)
                                                                if (idx != -1) {
                                                                    detectedItems[idx] = detectedItems[idx].copy(
                                                                        selectedLot = singleLot,
                                                                        dropdownExpanded = false
                                                                    )
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // B. Edit Quantity input
                                        OutlinedTextField(
                                            value = ocrItem.quantity.toString(),
                                            onValueChange = { valStr ->
                                                valStr.toDoubleOrNull()?.let { dVal ->
                                                    val idx = detectedItems.indexOf(ocrItem)
                                                    if (idx != -1) {
                                                        detectedItems[idx] = detectedItems[idx].copy(quantity = dVal)
                                                    }
                                                }
                                            },
                                            label = { Text("Qty (${ocrItem.selectedLot?.unit ?: "KG"})") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(0.8f)
                                        )
                                    }

                                    // C. PREVIEW: Remaining stock calculations layout
                                    ocrItem.selectedLot?.let { lot ->
                                        val remaining = lot.currentStock - ocrItem.quantity
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Current: ${lot.currentStock} ${lot.unit}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "→ Issue: ${ocrItem.quantity} ${lot.unit} →",
                                                fontSize = 11.sp,
                                                color = ColorOrange,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Remains: ${String.format("%.2f", remaining)} ${lot.unit}",
                                                fontSize = 11.sp,
                                                color = if (remaining < 0) ColorRed else ColorGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } ?: run {
                                        Text(
                                            text = "⚠️ Unable to calculate: select correct registered lot",
                                            color = ColorOrange,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Add missing item button
                        item {
                            OutlinedButton(
                                onClick = { showAddMissingItemDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("add_missing_item_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                                    Text(if (lang == AppLanguage.EN) "Add Missing Item" else "বাদ পড়া পণ্য সংযোজন করুন")
                                }
                            }
                        }
                    }

                    // Bottom Confirmation buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentOcrStage = "PHOTO_CHOOSE" },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(if (lang == AppLanguage.EN) "Re-Scan" else "পুনরায় স্ক্যান")
                        }

                        Button(
                            onClick = {
                                // Validation checking
                                var hasErrors = false
                                val itemsToReduce = mutableListOf<Pair<ProductEntity, Double>>()
                                for (it in detectedItems) {
                                    if (it.selectedLot == null) {
                                        hasErrors = true
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("All items must have a lot selected to reduce stock.")
                                        }
                                        break
                                    }
                                    if (it.quantity <= 0.0) {
                                        hasErrors = true
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Quantities must be positive values.")
                                        }
                                        break
                                    }
                                    if (it.quantity > it.selectedLot!!.currentStock) {
                                        hasErrors = true
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Cannot issue: stock deficient for ${it.name}.")
                                        }
                                        break
                                    }
                                    itemsToReduce.add(Pair(it.selectedLot!!, it.quantity))
                                }

                                if (!hasErrors && itemsToReduce.isNotEmpty()) {
                                    onSaveRecipe(itemsToReduce) { issueId ->
                                        newlyCreatedIssueId = issueId
                                        currentOcrStage = "CONFIRMED"
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .testTag("ocr_confirm_issue_button")
                        ) {
                            Text(if (lang == AppLanguage.EN) "Confirm Recipe Issue" else "ইস্যু নিশ্চিত করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                "CONFIRMED" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(ColorGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TaskAlt, contentDescription = null, tint = ColorGreen, modifier = Modifier.size(44.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == AppLanguage.EN) "AI-OCR Recipe Issued Successfully!" else "এআই-ওসিআর রেসিপি স্টক চয়ন সফল!",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${if (lang == AppLanguage.EN) "Issue ID" else "আইডি"}: $newlyCreatedIssueId",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == AppLanguage.EN) 
                                "All lot records, product total structures, and shelf locations were updated in the SQLite local dashboard registries."
                                else "তথ্যগুলো সফলভাবে আপডেট করা হয়েছে। ড্যাশবোর্ড পরিসংখ্যানসহ গুদামের র‍্যাক স্টকের পরিমাণ হ্রাস করা হয়েছে সার্থকভাবে।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                detectedItems.clear()
                                currentOcrStage = "PHOTO_CHOOSE"
                                onBack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(if (lang == AppLanguage.EN) "Return to Dashboard" else "ড্যাশবোর্ডে ফিরে যান")
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue to select matching product and add toocr preview
    if (showAddMissingItemDialog) {
        var selectedGpItem by remember { mutableStateOf<GroupedProduct?>(null) }
        var dropdownExpanded by remember { mutableStateOf(false) }
        var qtyInputVal by remember { mutableStateOf("1.0") }

        AlertDialog(
            onDismissRequest = { showAddMissingItemDialog = false },
            title = { Text(if (lang == AppLanguage.EN) "Add Missing Item" else "বাদ পড়া পণ্য চয়ন") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var showSelectorDialog by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { showSelectorDialog = true }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedGpItem?.name ?: (if (lang == AppLanguage.EN) "Select Product" else "পণ্য চয়ন করুন"),
                                fontSize = 13.sp,
                                fontWeight = if (selectedGpItem != null) FontWeight.Bold else FontWeight.Normal
                            )
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    }

                    if (showSelectorDialog) {
                        SearchableProductSelectorDialog(
                            lang = lang,
                            groupedProducts = groupedProducts,
                            onDismissRequest = { showSelectorDialog = false },
                            onProductSelected = { gp ->
                                selectedGpItem = gp
                                showSelectorDialog = false
                            }
                        )
                    }

                    OutlinedTextField(
                        value = qtyInputVal,
                        onValueChange = { qtyInputVal = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val productInstance = selectedGpItem
                        val dVal = qtyInputVal.toDoubleOrNull() ?: 1.0
                        if (productInstance != null) {
                            detectedItems.add(
                                ScannedOcrItem(
                                    name = productInstance.name,
                                    quantity = dVal,
                                    selectedLot = productInstance.lots.firstOrNull()
                                )
                            )
                            showAddMissingItemDialog = false
                        }
                    },
                    enabled = selectedGpItem != null
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMissingItemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// SCREEN 1: HISTORICAL TRANSACTION LOGS
// ==========================================
@Composable
fun RecipeIssueHistoryScreen(
    lang: AppLanguage,
    issues: List<RecipeIssueEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (lang == AppLanguage.EN) "Recipe Issue History Logbook" else "রেসিপি স্টক বিতরণের ইতিহাস ও তালিকা",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (lang == AppLanguage.EN) "${issues.size} Logs" else "${issues.size} টি লগ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (issues.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.AssignmentLate, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (lang == AppLanguage.EN) "No issues processed yet today." else "আজ কোনো রেসিপি স্টক ইস্যু করা হয়নি।",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(issues) { entry ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = entry.recipeIssueId,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = entry.date,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(ColorGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.EN) "STOCKS DEDUCTED" else "স্টক পরিশোধিত",
                                        color = ColorGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (lang == AppLanguage.EN) "Issued Items:" else "সরবরাহকৃত উপকরণসমূহ:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Text(
                                text = entry.itemsSummary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
