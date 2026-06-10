package com.example.data.database

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // --- Product Queries ---
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Long): Product?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    // --- Lot Queries ---
    @Query("SELECT * FROM lots ORDER BY entryDate ASC")
    fun getAllLots(): Flow<List<Lot>>

    @Query("SELECT * FROM lots WHERE productId = :productId ORDER BY entryDate ASC")
    fun getLotsForProduct(productId: Long): Flow<List<Lot>>

    @Query("SELECT * FROM lots WHERE productId = :productId ORDER BY entryDate ASC")
    suspend fun getLotsForProductSync(productId: Long): List<Lot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLot(lot: Lot): Long

    @Update
    suspend fun updateLot(lot: Lot)

    @Delete
    suspend fun deleteLot(lot: Lot)

    @Query("DELETE FROM lots WHERE productId = :productId")
    suspend fun deleteLotsForProduct(productId: Long)

    // --- Supplier Queries ---
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    // --- Purchase Queries ---
    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    fun getAllPurchases(): Flow<List<Purchase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    // --- Stock Movement Queries ---
    @Query("SELECT * FROM stock_movements ORDER BY date DESC")
    fun getAllStockMovements(): Flow<List<StockMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovement): Long
}
