package com.example.data

import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val dao: InventoryDao) {

    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val allConsumptions: Flow<List<ConsumptionEntity>> = dao.getAllConsumptions()
    val allPurchases: Flow<List<PurchaseEntity>> = dao.getAllPurchases()
    val allSuppliers: Flow<List<SupplierEntity>> = dao.getAllSuppliers()
    val allTransfers: Flow<List<TransferEntity>> = dao.getAllTransfers()
    val allRecipeIssues: Flow<List<RecipeIssueEntity>> = dao.getAllRecipeIssues()

    fun searchProducts(query: String): Flow<List<ProductEntity>> = dao.searchProducts(query)

    suspend fun getProductById(id: Int): ProductEntity? = dao.getProductById(id)
    suspend fun getProductByCode(code: String): ProductEntity? = dao.getProductByCode(code)

    suspend fun insertProduct(product: ProductEntity): Long = dao.insertProduct(product)
    
    suspend fun updateProduct(product: ProductEntity) = dao.updateProduct(product)

    suspend fun deleteProduct(product: ProductEntity) = dao.deleteProduct(product)

    /**
     * Records a new consumption log and automatically updates the product's Outward Stock inventory count
     */
    suspend fun recordConsumption(consumption: ConsumptionEntity): Boolean {
        val product = dao.getProductById(consumption.productId) ?: return false
        
        // Compute updated fields
        val updatedStockOut = product.stockOut + consumption.quantityUsed
        val updatedCurrentStock = product.openingStock + product.stockIn - updatedStockOut
        
        val updatedProduct = product.copy(
            stockOut = updatedStockOut,
            currentStock = updatedCurrentStock
        )
        
        dao.insertConsumption(consumption)
        dao.updateProduct(updatedProduct)
        return true
    }

    /**
     * Records an incoming purchase order and automatically updates the product's Inward Stock inventory count
     */
    suspend fun recordPurchase(purchase: PurchaseEntity): Boolean {
        val product = dao.getProductById(purchase.productId) ?: return false
        
        // Compute updated fields
        val updatedStockIn = product.stockIn + purchase.quantity
        val updatedCurrentStock = product.openingStock + updatedStockIn - product.stockOut
        
        val updatedProduct = product.copy(
            stockIn = updatedStockIn,
            currentStock = updatedCurrentStock,
            // Track price change if applicable
            purchasePrice = purchase.unitPrice,
            currency = purchase.currency
        )
        
        dao.insertPurchase(purchase)
        dao.updateProduct(updatedProduct)
        return true
    }

    suspend fun insertSupplier(supplier: SupplierEntity): Long = dao.insertSupplier(supplier)
    suspend fun insertTransfer(transfer: TransferEntity): Long = dao.insertTransfer(transfer)
    suspend fun insertRecipeIssue(issue: RecipeIssueEntity): Long = dao.insertRecipeIssue(issue)
    suspend fun getRecipeIssuesCount(): Int = dao.getRecipeIssuesCount()
}
