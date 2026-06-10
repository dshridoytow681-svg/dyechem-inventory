package com.example.data.repository

import com.example.data.database.InventoryDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class InventoryRepository(private val dao: InventoryDao) {

    // --- Product Methods ---
    val allProducts: Flow<List<Product>> = dao.getAllProducts()

    suspend fun getProductById(id: Long): Product? = dao.getProductById(id)

    fun searchProducts(query: String): Flow<List<Product>> = dao.searchProducts(query)

    suspend fun insertProduct(product: Product): Long = dao.insertProduct(product)

    suspend fun updateProduct(product: Product) = dao.updateProduct(product)

    suspend fun deleteProduct(product: Product) {
        dao.deleteLotsForProduct(product.id)
        dao.deleteProduct(product)
    }

    // --- Lot Methods ---
    val allLots: Flow<List<Lot>> = dao.getAllLots()

    fun getLotsForProduct(productId: Long): Flow<List<Lot>> = dao.getLotsForProduct(productId)

    suspend fun insertLot(lot: Lot): Long = dao.insertLot(lot)

    suspend fun updateLot(lot: Lot) = dao.updateLot(lot)

    suspend fun deleteLot(lot: Lot) = dao.deleteLot(lot)

    // --- Supplier Methods ---
    val allSuppliers: Flow<List<Supplier>> = dao.getAllSuppliers()

    suspend fun insertSupplier(supplier: Supplier): Long = dao.insertSupplier(supplier)

    suspend fun deleteSupplier(supplier: Supplier) = dao.deleteSupplier(supplier)

    // --- Purchase Methods ---
    val allPurchases: Flow<List<Purchase>> = dao.getAllPurchases()

    suspend fun insertPurchase(purchase: Purchase): Long = dao.insertPurchase(purchase)

    // --- Stock Movements ---
    val allStockMovements: Flow<List<StockMovement>> = dao.getAllStockMovements()

    suspend fun insertStockMovement(movement: StockMovement): Long = dao.insertStockMovement(movement)

    // --- FIFO stock movement reduction logic ---
    /**
     * Consumes quantity from the oldest lots first.
     * Returns a list of Triple(Lot, doubleConsumeAmount, lotNumber) that were consumed.
     */
    suspend fun issueStockFIFO(
        productId: Long,
        quantityToIssue: Double,
        remarks: String = ""
    ): List<Triple<Lot, Double, String>> {
        val product = dao.getProductById(productId) ?: return emptyList()
        val lots = dao.getLotsForProductSync(productId) // Sorted by entryDate ASC (FIFO!)
        
        var remainingToIssue = quantityToIssue
        val consumedLots = mutableListOf<Triple<Lot, Double, String>>()

        for (lot in lots) {
            if (remainingToIssue <= 0.0) break
            if (lot.quantity > 0.0) {
                val consume = minOf(lot.quantity, remainingToIssue)
                val newQty = lot.quantity - consume
                val updatedLot = lot.copy(quantity = newQty)
                
                dao.updateLot(updatedLot)
                
                // Add to consumed tracker
                consumedLots.add(Triple(updatedLot, consume, lot.lotNumber))
                
                // Track stock movement
                dao.insertStockMovement(
                    StockMovement(
                        productId = productId,
                        productName = product.name,
                        lotNumber = lot.lotNumber,
                        type = "OUT",
                        quantity = consume,
                        remarks = remarks.ifEmpty { "Manual Issue (FIFO)" }
                    )
                )

                remainingToIssue -= consume
            }
        }
        
        return consumedLots
    }

    /**
     * Adds stock for a specific lot. If the lot doesn't exist, creates it.
     */
    suspend fun stockIn(
        product: Product,
        lotNumber: String,
        qty: Double,
        remarks: String = "Stock In"
    ) {
        val lots = dao.getLotsForProductSync(product.id)
        val existingLot = lots.find { it.lotNumber.equals(lotNumber, ignoreCase = true) }
        
        if (existingLot != null) {
            val updatedLot = existingLot.copy(
                quantity = existingLot.quantity + qty,
                initialQuantity = existingLot.initialQuantity + qty
            )
            dao.updateLot(updatedLot)
        } else {
            dao.insertLot(
                Lot(
                    productId = product.id,
                    lotNumber = lotNumber,
                    quantity = qty,
                    initialQuantity = qty
                )
            )
        }

        dao.insertStockMovement(
            StockMovement(
                productId = product.id,
                productName = product.name,
                lotNumber = lotNumber,
                type = "IN",
                quantity = qty,
                remarks = remarks
            )
        )
    }
}
