package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class InventoryTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: InventoryDao
    private lateinit var repository: InventoryRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.inventoryDao()
        repository = InventoryRepository(dao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testProductInsertionAndAutomaticConsumptionDeduction() = runBlocking {
        // 1. Create product row
        val product = ProductEntity(
            id = 1,
            name = "Yellow Reactive Dye",
            code = "D-YE-200",
            category = "Dye",
            brand = "Dystar",
            lotNumber = "LOT-10",
            batchNumber = "B-2",
            rackNumber = "Rack A-1",
            warehouseLocation = "Dyeing Store 1",
            unit = "KG",
            openingStock = 500.0,
            stockIn = 0.0,
            stockOut = 0.0,
            currentStock = 500.0,
            lowStockThreshold = 100.0,
            purchasePrice = 450.0,
            currency = "BDT",
            iconName = "color_bag"
        )
        repository.insertProduct(product)

        // 2. Double-check item is saved
        val saved = repository.getProductById(1)
        assertEquals("Yellow Reactive Dye", saved?.name)
        assertEquals(500.0, saved?.currentStock)

        // 3. Record a store consumption
        val consumption = ConsumptionEntity(
            productId = 1,
            productName = "Yellow Reactive Dye",
            date = "2026-06-09",
            quantityUsed = 120.0,
            department = "Bulk Unit 4",
            operator = "Hossain",
            notes = "Test run"
        )
        repository.recordConsumption(consumption)

        // 4. Verify stock decreases automatically
        val updated = repository.getProductById(1)
        assertEquals(120.0, updated?.stockOut)
        assertEquals(380.0, updated?.currentStock)
    }
}
