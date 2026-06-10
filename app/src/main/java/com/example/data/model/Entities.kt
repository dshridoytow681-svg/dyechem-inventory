package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "Dyes", "Chemicals", "Liquid Colors", "Powder Colors", "Auxiliaries"
    val rackNumber: String, // e.g., "A01", "B03"
    val lowStockLimit: Double, // e.g., 20.0
    val unit: String, // "KG", "Gram", "Liter", "ML", "Piece", "Meter"
    val packagingType: String, // "Bag", "Drum", "Jar", "Bottle", "Carton", "Container", "Bucket"
    val dyeColor: String = "None" // "Red", "Blue", "Black", "Yellow" or "None"
)

@Entity(
    tableName = "lots",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class Lot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val lotNumber: String, // e.g., "HP001", "HP002"
    val quantity: Double, // Current quantity in stock
    val initialQuantity: Double, // Initial quantity when purchased
    val entryDate: Long = System.currentTimeMillis() // Oldest lot consumed first (FIFO!)
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mobile: String,
    val address: String,
    val notes: String
)

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierName: String,
    val invoiceNumber: String,
    val purchaseDate: Long,
    val productName: String,
    val lotNumber: String,
    val quantity: Double,
    val price: Double,
    val currency: String // "USD", "BDT"
)

@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val lotNumber: String,
    val type: String, // "IN" or "OUT" (Issue)
    val quantity: Double, // Quantity in the product's unit
    val date: Long = System.currentTimeMillis(),
    val remarks: String = "" // e.g., "Issued via Recipe OCR", "Manual Issue", "Purchased"
)
