package com.example.ui.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.GeminiRepository
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    Splash,
    Dashboard,
    ProductList,
    ProductDetails,
    AddEditProduct,
    RackManagement,
    RecipeIssue,
    Scanner,
    VoiceAssistant,
    Analytics,
    SupplierModule,
    PurchaseLog,
    LowStockAlerts
}

enum class UserRole {
    Admin,
    Manager,
    StoreKeeper,
    Viewer
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = InventoryRepository(db.dao())
    private val geminiRepository = GeminiRepository()

    // UI Configuration States
    val isDarkMode = MutableStateFlow(true) // Default to standard futuristic dark mode
    val currentRole = MutableStateFlow(UserRole.Admin) // Starts as Admin for testing complete access
    val currentScreen = MutableStateFlow(AppScreen.Splash)
    val selectedProduct = MutableStateFlow<Product?>(null)

    // DB Driven Flow States
    val products = repository.allProducts.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val lots = repository.allLots.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val suppliers = repository.allSuppliers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val purchases = repository.allPurchases.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val stockMovements = repository.allStockMovements.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Product search query
    val searchQuery = MutableStateFlow("")
    val searchedProducts = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allProducts else repository.searchProducts(query)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Low Stock Alert Badge & Triggering system
    val lowStockCount = combine(products, lots) { prodList, lotList ->
        prodList.count { prod ->
            val totalQty = lotList.filter { it.productId == prod.id }.sumOf { it.quantity }
            totalQty < prod.lowStockLimit
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val lowStockProducts = combine(products, lots) { prodList, lotList ->
        prodList.filter { prod ->
            val totalQty = lotList.filter { it.productId == prod.id }.sumOf { it.quantity }
            totalQty < prod.lowStockLimit
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // FIFO consumption dialog detail states
    val fifoConsumptionResult = MutableStateFlow<List<Triple<Lot, Double, String>>?>(null)
    val showFifoDialog = MutableStateFlow(false)

    // Camera & OCR Scan States
    val ocrCaptureUri = MutableStateFlow<android.net.Uri?>(null)
    val scannedOcrItems = MutableStateFlow<List<OcrRecipeItem>>(emptyList())
    val ocrStatus = MutableStateFlow("Idle") // "Idle", "Scanning", "Success", "Error"

    init {
        // Build Notification Channel for Low Stock
        createNotificationChannel()
        
        // Seed Database with realistic records if it is currently empty
        viewModelScope.launch {
            products.first { true } // wait for first emission
            val size = products.value.size
            if (size == 0) {
                seedInitialData()
            }
        }
    }

    // Simple state-friendly backstack for Android System Navigation and custom Back gestures
    private val _backStack = mutableListOf<AppScreen>()

    // Navigation helper
    fun navigateTo(screen: AppScreen, clearHistory: Boolean = false) {
        if (clearHistory) {
            _backStack.clear()
        } else {
            val current = currentScreen.value
            if (current != screen && current != AppScreen.Splash) {
                // Remove existing to maintain modern non-circular back flow
                _backStack.remove(current)
                _backStack.add(current)
            }
        }
        currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (_backStack.isNotEmpty()) {
            val prev = _backStack.removeAt(_backStack.size - 1)
            currentScreen.value = prev
            return true
        }
        return false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "DyeChem Stock Alerts"
            val descriptionText = "Notifications for low stock DyeChem items"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("STOCK_ALERTS_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun triggerLowStockNotification(productName: String, totalQty: Double, limit: Double, unit: String) {
        val context = getApplication<Application>()
        val builder = NotificationCompat.Builder(context, "STOCK_ALERTS_CHANNEL")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("DyeChem Low Stock Alert!")
            .setContentText("$productName stock is currently $totalQty $unit (Limit: $limit $unit)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(productName.hashCode(), builder.build())
        } catch (e: Exception) {
            Log.e("InventoryViewModel", "Error posting notification", e)
        }
    }

    // --- DB Data Seeding ---
    private suspend fun seedInitialData() {
        Log.d("InventoryViewModel", "Seeding initial factory inventory records...")

        // Seeding suppliers
        val s1 = repository.insertSupplier(
            Supplier(
                name = "Apex Industrial Chemicals",
                mobile = "+8801927999251",
                address = "Tejgaon Industrial Area, Dhaka",
                notes = "Primary local manufacturer for hydrogen peroxide."
            )
        )
        val s2 = repository.insertSupplier(
            Supplier(
                name = "Golden Dyeing Solutions Ltd",
                mobile = "+8801711223344",
                address = "EPZ Road, Chittagong",
                notes = "Specializes in reactive dyes and stabilizers."
            )
        )

        // Seeding products
        val p1LocalId = repository.insertProduct(
            Product(
                name = "Hydrogen Peroxide",
                category = "Chemicals",
                rackNumber = "Rack A01",
                lowStockLimit = 20.0,
                unit = "KG",
                packagingType = "Drum"
            )
        )
        val p2LocalId = repository.insertProduct(
            Product(
                name = "Sodium Bisulfite",
                category = "Chemicals",
                rackNumber = "Rack A02",
                lowStockLimit = 40.0,
                unit = "KG",
                packagingType = "Bag"
            )
        )
        val p3LocalId = repository.insertProduct(
            Product(
                name = "Reactive Red FH",
                category = "Dyes",
                rackNumber = "Rack B01",
                lowStockLimit = 25.0,
                unit = "KG",
                packagingType = "Carton",
                dyeColor = "Red"
            )
        )
        val p4LocalId = repository.insertProduct(
            Product(
                name = "Reactive Blue BL",
                category = "Dyes",
                rackNumber = "Rack B02",
                lowStockLimit = 15.0,
                unit = "KG",
                packagingType = "Bucket",
                dyeColor = "Blue"
            )
        )
        val p5LocalId = repository.insertProduct(
            Product(
                name = "Caustic Soda Flakes",
                category = "Chemicals",
                rackNumber = "Rack A01",
                lowStockLimit = 30.0,
                unit = "KG",
                packagingType = "Bag"
            )
        )
        val p6LocalId = repository.insertProduct(
            Product(
                name = "Detergent Active Liquid",
                category = "Liquid Colors",
                rackNumber = "Rack C01",
                lowStockLimit = 50.0,
                unit = "Liter",
                packagingType = "Bottle"
            )
        )

        // Seeding Lots for Hydrogen Peroxide
        repository.insertLot(Lot(productId = p1LocalId, lotNumber = "HP001", quantity = 50.0, initialQuantity = 50.0, entryDate = System.currentTimeMillis() - 86400000 * 3))
        repository.insertLot(Lot(productId = p1LocalId, lotNumber = "HP002", quantity = 70.0, initialQuantity = 70.0, entryDate = System.currentTimeMillis() - 86400000 * 2))
        repository.insertLot(Lot(productId = p1LocalId, lotNumber = "HP003", quantity = 30.0, initialQuantity = 30.0, entryDate = System.currentTimeMillis() - 86400000 * 1))

        // Seeding Lots for Sodium Bisulfite
        repository.insertLot(Lot(productId = p2LocalId, lotNumber = "SB-88", quantity = 35.0, initialQuantity = 50.0, entryDate = System.currentTimeMillis() - 86400000 * 5)) // Will trigger alert since total = 35 < 40!

        // Seeding Lots for Reactive Red
        // Limit is 25.0, seeding total 8.0, triggers alert!
        repository.insertLot(Lot(productId = p3LocalId, lotNumber = "R-RED-1", quantity = 4.0, initialQuantity = 10.0, entryDate = System.currentTimeMillis() - 86400000 * 4))
        repository.insertLot(Lot(productId = p3LocalId, lotNumber = "R-RED-2", quantity = 4.0, initialQuantity = 20.0, entryDate = System.currentTimeMillis() - 86400000 * 3))

        // Seeding Lots for Reactive Blue
        repository.insertLot(Lot(productId = p4LocalId, lotNumber = "R-BLUE-X", quantity = 50.0, initialQuantity = 100.0))

        // Seeding Lots for Caustic Soda
        repository.insertLot(Lot(productId = p5LocalId, lotNumber = "CAUS-LT-01", quantity = 100.0, initialQuantity = 100.0))

        // Seeding Lots for Detergent Active Liquid
        repository.insertLot(Lot(productId = p6LocalId, lotNumber = "DET-1", quantity = 80.0, initialQuantity = 80.0))

        // Register Seed Purchases
        repository.insertPurchase(
            Purchase(
                supplierName = "Apex Industrial Chemicals",
                invoiceNumber = "INV-07711",
                purchaseDate = System.currentTimeMillis() - 86400000 * 3,
                productName = "Hydrogen Peroxide",
                lotNumber = "HP001",
                quantity = 50.0,
                price = 1500.0,
                currency = "BDT"
            )
        )
        repository.insertPurchase(
            Purchase(
                supplierName = "Golden Dyeing Solutions Ltd",
                invoiceNumber = "DYE-30018",
                purchaseDate = System.currentTimeMillis() - 86400000 * 4,
                productName = "Reactive Red FH",
                lotNumber = "R-RED-1",
                quantity = 10.0,
                price = 350.0,
                currency = "USD"
            )
        )

        // Seed some stock movements
        repository.insertStockMovement(StockMovement(productId = p1LocalId, productName = "Hydrogen Peroxide", lotNumber = "HP001", type = "IN", quantity = 50.0, remarks = "Initial Seed stock"))
        repository.insertStockMovement(StockMovement(productId = p1LocalId, productName = "Hydrogen Peroxide", lotNumber = "HP002", type = "IN", quantity = 70.0, remarks = "Initial Seed stock"))
        repository.insertStockMovement(StockMovement(productId = p1LocalId, productName = "Hydrogen Peroxide", lotNumber = "HP003", type = "IN", quantity = 30.0, remarks = "Initial Seed stock"))
        
        repository.insertStockMovement(StockMovement(productId = p3LocalId, productName = "Reactive Red FH", lotNumber = "R-RED-1", type = "IN", quantity = 10.0, remarks = "Initial seed batch"))
        repository.insertStockMovement(StockMovement(productId = p3LocalId, productName = "Reactive Red FH", lotNumber = "R-RED-1", type = "OUT", quantity = 6.0, remarks = "Manual formulation issue"))

        // Trigger notifications for low seeds on launch!
        triggerLowStockNotification("Sodium Bisulfite", 35.0, 40.0, "KG")
        triggerLowStockNotification("Reactive Red FH", 8.0, 25.0, "KG")
    }

    // --- Product CRUD Actions ---
    fun saveProduct(
        id: Long = 0,
        name: String,
        category: String,
        rackNumber: String,
        lowLimit: Double,
        unit: String,
        packagingType: String,
        dyeColor: String
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                // Insert new product
                repository.insertProduct(
                    Product(
                        name = name,
                        category = category,
                        rackNumber = rackNumber,
                        lowStockLimit = lowLimit,
                        unit = unit,
                        packagingType = packagingType,
                        dyeColor = dyeColor
                    )
                )
            } else {
                // Update
                val existing = repository.getProductById(id)
                if (existing != null) {
                    val updated = existing.copy(
                        name = name,
                        category = category,
                        rackNumber = rackNumber,
                        lowStockLimit = lowLimit,
                        unit = unit,
                        packagingType = packagingType,
                        dyeColor = dyeColor
                    )
                    repository.updateProduct(updated)
                }
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // --- Stock-In Functionality (Store Keeper / Manager Access) ---
    fun performStockIn(
        productId: Long,
        lotNumber: String,
        quantity: Double,
        supplierId: Long? = null,
        invoice: String = "",
        price: Double = 0.0,
        currency: String = "BDT"
    ) {
        viewModelScope.launch {
            val product = repository.getProductById(productId) ?: return@launch
            repository.stockIn(product, lotNumber, quantity)

            // If there's purchase details, store it!
            if (supplierId != null || invoice.isNotBlank()) {
                val supplierName = suppliers.value.find { it.id == supplierId }?.name ?: "Unknown Supplier"
                repository.insertPurchase(
                    Purchase(
                        supplierName = supplierName,
                        invoiceNumber = invoice.ifBlank { "N/A" },
                        purchaseDate = System.currentTimeMillis(),
                        productName = product.name,
                        lotNumber = lotNumber,
                        quantity = quantity,
                        price = price,
                        currency = currency
                    )
                )
            }
        }
    }

    // --- Stock Issue (OUT) using core FIFO engine ---
    fun issueStock(
        productId: Long,
        quantityToIssue: Double,
        remarks: String = ""
    ) {
        viewModelScope.launch {
            val product = repository.getProductById(productId) ?: return@launch
            val consumed = repository.issueStockFIFO(productId, quantityToIssue, remarks)
            
            // Trigger alerts instantly if stock drops
            val lotsForThisProd = lots.value.filter { it.productId == productId }
            // Deduct the consumed amount immediately to predict live status
            val currentStockTotal = lotsForThisProd.sumOf { it.quantity } - quantityToIssue
            if (currentStockTotal < product.lowStockLimit) {
                triggerLowStockNotification(product.name, currentStockTotal, product.lowStockLimit, product.unit)
            }

            // Expose consumption details to trigger the gorgeous FIFO Success Dialog
            fifoConsumptionResult.value = consumed
            showFifoDialog.value = true
        }
    }

    // --- Supplier management ---
    fun addSupplier(name: String, mobile: String, address: String, notes: String) {
        viewModelScope.launch {
            repository.insertSupplier(
                Supplier(name = name, mobile = mobile, address = address, notes = notes)
            )
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
        }
    }

    // --- Vision OCR Recipe Process ---
    fun processOcrRecipeImage(bitmap: Bitmap) {
        ocrStatus.value = "Scanning"
        viewModelScope.launch {
            val items = geminiRepository.performOcrOnImage(bitmap)
            if (items.isNotEmpty()) {
                scannedOcrItems.value = items
                ocrStatus.value = "Success"
            } else {
                ocrStatus.value = "Error"
            }
        }
    }

    // Confirm Ocr Recipe stock deduction line by line
    fun confirmOcrRecipeChanges(itemsToConfirm: List<OcrRecipeItem>) {
        viewModelScope.launch {
            for (item in itemsToConfirm) {
                // Find matching product by name
                val matches = products.value.filter { it.name.contains(item.productName, ignoreCase = true) }
                if (matches.isNotEmpty()) {
                    val matchingProduct = matches[0]
                    if (item.quantity > 0.0) {
                        repository.issueStockFIFO(
                            productId = matchingProduct.id,
                            quantityToIssue = item.quantity,
                            remarks = "Issued via scan: ${matchingProduct.name}"
                        )
                    }
                }
            }
            // Clear scanned buffer
            scannedOcrItems.value = emptyList()
            ocrStatus.value = "Idle"
        }
    }

    // --- AI Assistant Engine ---
    val assistantChatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "AI",
                message = "স্বাগতম! DyeChem Warehouse AI সহকারী প্রস্তুত। " +
                        "আপনি বাংলায় বা ইংরেজিতে স্টক, লট বা র্যাক সম্পর্কিত যেকোনো প্রশ্ন জিজ্ঞাসা করতে পারেন।\n\n" +
                        "Welcome! I am ready to answer your inventory queries.",
                loading = false
            )
        )
    )

    fun sendQuestionToAssistant(questionText: String) {
        if (questionText.isBlank()) return
        
        // Add User Message
        val prevList = assistantChatHistory.value.toMutableList()
        prevList.add(ChatMessage(sender = "User", message = questionText))
        
        // Add a temporary loading response from AI
        val aiPlaceholderIndex = prevList.size
        prevList.add(ChatMessage(sender = "AI", message = "", loading = true))
        assistantChatHistory.value = prevList

        viewModelScope.launch {
            // Build Database State context
            val builder = StringBuilder()
            builder.append("Industrial Dyeing Factory Live Inventory Summary:\n")
            products.value.forEach { p ->
                val prodLots = lots.value.filter { it.productId == p.id }
                val totalStk = prodLots.sumOf { it.quantity }
                builder.append("- ${p.name}: Category=${p.category}, Rack=${p.rackNumber}, Total Stock=$totalStk ${p.unit}, Low Stock Limit=${p.lowStockLimit} ${p.unit}. Lots: ")
                builder.append(prodLots.joinToString(", ") { "${it.lotNumber}=${it.quantity} ${p.unit}" })
                builder.append("\n")
            }
            builder.append("\nLow Stock Alert Counts: ${lowStockCount.value}\n")
            val rackSets = products.value.map { it.rackNumber }.toSet()
            builder.append("Racks active: ${rackSets.joinToString(", ")}")

            val response = geminiRepository.askAssistant(questionText, builder.toString())

            // Replace loading placeholder with actual Gemini response
            val finalHistory = assistantChatHistory.value.toMutableList()
            if (aiPlaceholderIndex < finalHistory.size) {
                finalHistory[aiPlaceholderIndex] = ChatMessage(sender = "AI", message = response, loading = false)
            } else {
                finalHistory.add(ChatMessage(sender = "AI", message = response, loading = false))
            }
            assistantChatHistory.value = finalHistory
        }
    }

    fun clearChat() {
        assistantChatHistory.value = listOf(
            ChatMessage(
                sender = "AI",
                message = "Chat cleared. Ask me about stock status, low quantities, racks, or lots anytime!"
            )
        )
    }

    // Custom racks state for dynamic storage distribution grid additions
    val customRacks = MutableStateFlow<List<CustomRack>>(emptyList())

    fun addCustomRack(name: String, maxCapacity: Double, description: String) {
        customRacks.value = customRacks.value + CustomRack(name, maxCapacity, description)
    }
}

data class CustomRack(
    val name: String,
    val maxCapacity: Double,
    val description: String = ""
)

data class ChatMessage(
    val sender: String, // "User" or "AI"
    val message: String,
    val loading: Boolean = false
)
