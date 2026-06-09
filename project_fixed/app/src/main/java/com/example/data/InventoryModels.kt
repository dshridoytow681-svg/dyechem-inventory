package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val code: String,
    val category: String, // "Dye" or "Chemical"
    val brand: String,
    val lotNumber: String,
    val batchNumber: String,
    val rackNumber: String,
    val warehouseLocation: String,
    val unit: String, // "KG", "Gram", "Bag", "Bottle", "Drum", "Liter"
    val openingStock: Double,
    val stockIn: Double,
    val stockOut: Double,
    val currentStock: Double, // Calculated automatically
    val reservedStock: Double = 0.0,
    val damagedStock: Double = 0.0,
    val lowStockThreshold: Double,
    val purchasePrice: Double,
    val currency: String, // "BDT" or "USD"
    val iconName: String, // E.g., "color_bag", "dye_packet", "drum", "bottle", etc.
    val entryDate: String = "2026-06-01",
    val expiryDate: String = "2029-06-01",
    val packageType: String = "Bag" // "Bag", "Drum", "Jar", "Carton", "Bottle"
) {
    val totalValue: Double
        get() = currentStock * purchasePrice
}

@Entity(tableName = "consumptions")
data class ConsumptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val productName: String,
    val date: String, // YYYY-MM-DD
    val quantityUsed: Double,
    val department: String,
    val operator: String,
    val notes: String
)

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val supplierName: String,
    val invoiceNumber: String,
    val purchaseDate: String, // YYYY-MM-DD
    val productId: Int,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double,
    val currency: String
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val mobile: String,
    val address: String,
    val notes: String
)

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lotNumber: String,
    val productName: String,
    val fromRack: String,
    val toRack: String,
    val date: String, // YYYY-MM-DD
    val operator: String
)

@Entity(tableName = "recipe_issues")
data class RecipeIssueEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeIssueId: String,
    val date: String, // YYYY-MM-DD
    val itemsSummary: String
)

@Dao
interface InventoryDao {
    // Products
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE code = :code LIMIT 1")
    suspend fun getProductByCode(code: String): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' OR lotNumber LIKE '%' || :query || '%' OR rackNumber LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // Consumptions
    @Query("SELECT * FROM consumptions ORDER BY id DESC")
    fun getAllConsumptions(): Flow<List<ConsumptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsumption(consumption: ConsumptionEntity): Long

    // Purchases
    @Query("SELECT * FROM purchases ORDER BY id DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    // Suppliers
    @Query("SELECT * FROM suppliers ORDER BY id DESC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    // Transfers
    @Query("SELECT * FROM transfers ORDER BY id DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity): Long

    // Recipe Issues
    @Query("SELECT * FROM recipe_issues ORDER BY id DESC")
    fun getAllRecipeIssues(): Flow<List<RecipeIssueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIssue(issue: RecipeIssueEntity): Long

    @Query("SELECT COUNT(*) FROM recipe_issues")
    suspend fun getRecipeIssuesCount(): Int
}

@Database(
    entities = [
        ProductEntity::class,
        ConsumptionEntity::class,
        PurchaseEntity::class,
        SupplierEntity::class,
        TransferEntity::class,
        RecipeIssueEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
}
