package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [Product::class, Lot::class, Supplier::class, Purchase::class, StockMovement::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dyechem_smart_inventory_pro_db"
                )
                .fallbackToDestructiveMigration() // safe for prototyping / local SQLite
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
