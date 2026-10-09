package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.SaleWithItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShoeBusinessRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val saleDao = database.saleDao()
    private val expenseDao = database.expenseDao()
    private val businessProfileDao = database.businessProfileDao()

    // ---------------- Products ----------------
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)

    fun getProductByIdFlow(id: Long): Flow<ProductEntity?> = productDao.getProductByIdFlow(id)

    suspend fun addProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun adjustStock(productId: Long, delta: Int) = withContext(Dispatchers.IO) {
        productDao.adjustProductStock(productId, delta, System.currentTimeMillis())
    }

    // ---------------- Sales ----------------
    val allSales: Flow<List<SaleWithItems>> = saleDao.getAllSalesWithItems()

    fun getSaleWithItemsById(id: Long): Flow<SaleWithItems?> = saleDao.getSaleWithItemsById(id)

    fun getSalesBetweenDates(startTime: Long, endTime: Long): Flow<List<SaleWithItems>> =
        saleDao.getSalesBetweenDates(startTime, endTime)

    suspend fun recordSale(
        itemsToSell: List<Pair<ProductEntity, Int>>, // Product and quantity
        paymentMethod: String,
        discountAmount: Double = 0.0,
        customerName: String = "",
        customerPhone: String = "",
        notes: String = "",
        allowNegativeStock: Boolean = false,
        saleTimestamp: Long = System.currentTimeMillis()
    ): Result<SaleEntity> = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                // 1. Validate stock
                if (!allowNegativeStock) {
                    for ((product, qty) in itemsToSell) {
                        val current = productDao.getProductById(product.id)
                            ?: throw IllegalStateException("Product '${product.name}' was not found.")
                        if (current.quantity < qty) {
                            throw IllegalStateException(
                                "Insufficient stock for '${current.name}'. Available: ${current.quantity}, Requested: $qty"
                            )
                        }
                    }
                }

                // 2. Generate unique transaction number (SAF-yyyyMMdd-HHmmss-xxx)
                val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
                val randomSuffix = (100..999).random()
                val transactionNumber = "SAF-${dateFormat.format(Date(saleTimestamp))}-$randomSuffix"

                var totalSelling = 0.0
                var totalCost = 0.0

                for ((product, qty) in itemsToSell) {
                    totalSelling += (product.sellingPrice * qty)
                    totalCost += (product.costPrice * qty)
                }

                val finalPayable = (totalSelling - discountAmount).coerceAtLeast(0.0)
                val grossProfit = finalPayable - totalCost

                val saleEntity = SaleEntity(
                    transactionNumber = transactionNumber,
                    totalAmount = totalSelling,
                    totalCost = totalCost,
                    discountAmount = discountAmount,
                    finalAmount = finalPayable,
                    grossProfit = grossProfit,
                    paymentMethod = paymentMethod,
                    customerName = customerName.trim(),
                    customerPhone = customerPhone.trim(),
                    notes = notes.trim(),
                    isReversed = false,
                    createdAt = saleTimestamp
                )

                val saleId = saleDao.insertSale(saleEntity)

                // 3. Create line items and deduct inventory
                val lineItems = itemsToSell.map { (product, qty) ->
                    val lineSelling = product.sellingPrice * qty
                    val lineCost = product.costPrice * qty
                    val lineProfit = lineSelling - lineCost

                    // Deduct stock
                    productDao.adjustProductStock(product.id, -qty, saleTimestamp)

                    SaleItemEntity(
                        saleId = saleId,
                        productId = product.id,
                        productName = product.name,
                        productSku = product.sku,
                        productSize = product.size,
                        productColor = product.color,
                        unitCostPrice = product.costPrice,
                        unitSellingPrice = product.sellingPrice,
                        quantity = qty,
                        subtotalAmount = lineSelling,
                        subtotalCost = lineCost,
                        subtotalProfit = lineProfit
                    )
                }

                saleDao.insertSaleItems(lineItems)

                Result.success(saleEntity.copy(id = saleId))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reverseSale(saleId: Long, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                val saleWithItems = saleDao.getSaleWithItemsById(saleId).firstOrNull()
                    ?: throw IllegalStateException("Sale not found.")

                if (saleWithItems.sale.isReversed) {
                    throw IllegalStateException("Transaction is already marked as reversed/refunded.")
                }

                // Restore stock for all items
                for (item in saleWithItems.items) {
                    productDao.adjustProductStock(item.productId, item.quantity, System.currentTimeMillis())
                }

                // Mark sale as reversed
                val updatedSale = saleWithItems.sale.copy(
                    isReversed = true,
                    reverseReason = reason.ifBlank { "Reversed / Customer Refund" },
                    reversedAt = System.currentTimeMillis()
                )
                saleDao.updateSale(updatedSale)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------------- Expenses ----------------
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getExpensesBetweenDates(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesBetweenDates(startTime, endTime)

    suspend fun addExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense)
    }

    // ---------------- Business Profile & Settings ----------------
    val profile: Flow<BusinessProfileEntity?> = businessProfileDao.getProfile()

    suspend fun getProfileSync(): BusinessProfileEntity = withContext(Dispatchers.IO) {
        businessProfileDao.getProfileSync() ?: BusinessProfileEntity().also {
            businessProfileDao.insertOrUpdate(it)
        }
    }

    suspend fun updateProfile(profile: BusinessProfileEntity) = withContext(Dispatchers.IO) {
        businessProfileDao.insertOrUpdate(profile)
    }

    // ---------------- Starter Data Initialization ----------------
    suspend fun seedStarterDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentProducts = productDao.getAllProducts().firstOrNull()
        if (currentProducts.isNullOrEmpty()) {
            loadStarterCatalog()
        }
        val currentProfile = businessProfileDao.getProfileSync()
        if (currentProfile == null) {
            businessProfileDao.insertOrUpdate(BusinessProfileEntity())
        }
    }

    suspend fun loadStarterCatalog() = withContext(Dispatchers.IO) {
        val sampleProducts = listOf(
            ProductEntity(
                name = "Men's Italian Leather Loafers",
                sku = "SAF-ML-01",
                category = "Loafers",
                size = "42, 43, 44",
                color = "Classic Black",
                brand = "Shehi Exclusive",
                costPrice = 18000.0,
                sellingPrice = 28000.0,
                quantity = 15,
                lowStockThreshold = 4,
                description = "Handmade premium Italian calfskin leather slip-on loafers with durable sole."
            ),
            ProductEntity(
                name = "Northern Senator Velvet Half Shoes",
                sku = "SAF-SH-02",
                category = "Slippers & Half Shoes",
                size = "41, 42, 43, 44",
                color = "Midnight Navy",
                brand = "Shehi Crafted",
                costPrice = 12000.0,
                sellingPrice = 19500.0,
                quantity = 22,
                lowStockThreshold = 5,
                description = "Custom embroidered velvet half-shoes matching traditional Senator and Agbada wear."
            ),
            ProductEntity(
                name = "Gentleman Brogue Oxford Shoes",
                sku = "SAF-OX-03",
                category = "Men's Shoes",
                size = "43, 44, 45",
                color = "Oxblood Brown",
                brand = "Clarks Craft",
                costPrice = 22000.0,
                sellingPrice = 35000.0,
                quantity = 8,
                lowStockThreshold = 3,
                description = "Formal winged-tip brogue shoes crafted for corporate executives and special events."
            ),
            ProductEntity(
                name = "Urban Air Cushion Walking Sneakers",
                sku = "SAF-SN-04",
                category = "Sneakers",
                size = "40, 41, 42, 43, 44",
                color = "Triple White",
                brand = "Stride Max",
                costPrice = 14000.0,
                sellingPrice = 22500.0,
                quantity = 18,
                lowStockThreshold = 4,
                description = "Lightweight breathable sport sneakers with shock-absorbing air cushioning."
            ),
            ProductEntity(
                name = "Women's Block Heel Suede Pumps",
                sku = "SAF-WP-05",
                category = "Women's",
                size = "38, 39, 40",
                color = "Nude Beige",
                brand = "Bella Moda",
                costPrice = 9500.0,
                sellingPrice = 16000.0,
                quantity = 12,
                lowStockThreshold = 3,
                description = "Comfortable 2.5-inch block heel pumps suitable for all-day office wear."
            ),
            ProductEntity(
                name = "Kano Handcrafted Leather Slides",
                sku = "SAF-SD-06",
                category = "Sandals & Slides",
                size = "41, 42, 43, 44, 45",
                color = "Tan Brown",
                brand = "Shehi Heritage",
                costPrice = 6500.0,
                sellingPrice = 11000.0,
                quantity = 25,
                lowStockThreshold = 6,
                description = "100% genuine hide leather slides with reinforced stitching for daily casual comfort."
            ),
            ProductEntity(
                name = "Rugged Suede Desert Chukka Boots",
                sku = "SAF-BT-07",
                category = "Boots",
                size = "42, 43, 44",
                color = "Sand Camel",
                brand = "Terra Walk",
                costPrice = 21000.0,
                sellingPrice = 32000.0,
                quantity = 2, // low stock demonstration
                lowStockThreshold = 3,
                description = "Sturdy suede ankle chukka boots with crepe sole for smart casual outings."
            ),
            ProductEntity(
                name = "Kids School Formal Black Shoes",
                sku = "SAF-KD-08",
                category = "Kids",
                size = "32, 33, 34, 35",
                color = "Glossy Black",
                brand = "Junior Walk",
                costPrice = 6000.0,
                sellingPrice = 9500.0,
                quantity = 0, // out of stock demonstration
                lowStockThreshold = 5,
                description = "Durable school shoes with velcro strap and scuff-resistant toe protection."
            )
        )
        productDao.insertAll(sampleProducts)

        // Also seed initial sample expenses for demonstration of Gross vs Net profit
        val sampleExpenses = listOf(
            ExpenseEntity(
                title = "Generator Fuel (PMS) for Shop",
                category = "Generator & Fuel",
                amount = 8500.0,
                notes = "Shop power supply during business hours",
                date = System.currentTimeMillis() - 86400000L
            ),
            ExpenseEntity(
                title = "Branded Footwear Shopping Bags",
                category = "Packaging & Bags",
                amount = 15000.0,
                notes = "100 pieces Shehi A. Footwears carrier bags",
                date = System.currentTimeMillis() - 172800000L
            )
        )
        expenseDao.insertAll(sampleExpenses)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        database.withTransaction {
            saleDao.clearAllSaleItems()
            saleDao.clearAllSales()
            productDao.clearAll()
            expenseDao.clearAll()
        }
    }

    // ---------------- Backup & Restore (JSON) ----------------
    suspend fun exportDatabaseBackupJson(): String = withContext(Dispatchers.IO) {
        val products = productDao.getAllProducts().firstOrNull() ?: emptyList()
        val sales = saleDao.getAllSalesWithItems().firstOrNull() ?: emptyList()
        val expenses = expenseDao.getAllExpenses().firstOrNull() ?: emptyList()
        val profile = getProfileSync()

        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Shehi A. Footwears")
        root.put("exportedAt", System.currentTimeMillis())

        // Profile
        val profileObj = JSONObject().apply {
            put("businessName", profile.businessName)
            put("ownerName", profile.ownerName)
            put("phoneNumber", profile.phoneNumber)
            put("email", profile.email)
            put("address", profile.address)
            put("receiptFooterNote", profile.receiptFooterNote)
            put("currencySymbol", profile.currencySymbol)
            put("isPinEnabled", profile.isPinEnabled)
            put("allowNegativeStockSale", profile.allowNegativeStockSale)
        }
        root.put("businessProfile", profileObj)

        // Products
        val productsArray = JSONArray()
        for (p in products) {
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("sku", p.sku)
                put("category", p.category)
                put("size", p.size)
                put("color", p.color)
                put("brand", p.brand)
                put("costPrice", p.costPrice)
                put("sellingPrice", p.sellingPrice)
                put("quantity", p.quantity)
                put("lowStockThreshold", p.lowStockThreshold)
                put("photoUri", p.photoUri ?: "")
                put("description", p.description)
                put("createdAt", p.createdAt)
            }
            productsArray.put(pObj)
        }
        root.put("products", productsArray)

        // Expenses
        val expensesArray = JSONArray()
        for (e in expenses) {
            val eObj = JSONObject().apply {
                put("id", e.id)
                put("title", e.title)
                put("category", e.category)
                put("amount", e.amount)
                put("date", e.date)
                put("notes", e.notes)
            }
            expensesArray.put(eObj)
        }
        root.put("expenses", expensesArray)

        // Sales
        val salesArray = JSONArray()
        for (s in sales) {
            val sObj = JSONObject().apply {
                put("id", s.sale.id)
                put("transactionNumber", s.sale.transactionNumber)
                put("totalAmount", s.sale.totalAmount)
                put("totalCost", s.sale.totalCost)
                put("discountAmount", s.sale.discountAmount)
                put("finalAmount", s.sale.finalAmount)
                put("grossProfit", s.sale.grossProfit)
                put("paymentMethod", s.sale.paymentMethod)
                put("customerName", s.sale.customerName)
                put("customerPhone", s.sale.customerPhone)
                put("notes", s.sale.notes)
                put("isReversed", s.sale.isReversed)
                put("reverseReason", s.sale.reverseReason)
                put("createdAt", s.sale.createdAt)

                val itemsArray = JSONArray()
                for (item in s.items) {
                    val itemObj = JSONObject().apply {
                        put("productId", item.productId)
                        put("productName", item.productName)
                        put("productSku", item.productSku)
                        put("productSize", item.productSize)
                        put("productColor", item.productColor)
                        put("unitCostPrice", item.unitCostPrice)
                        put("unitSellingPrice", item.unitSellingPrice)
                        put("quantity", item.quantity)
                        put("subtotalAmount", item.subtotalAmount)
                        put("subtotalCost", item.subtotalCost)
                        put("subtotalProfit", item.subtotalProfit)
                    }
                    itemsArray.put(itemObj)
                }
                put("items", itemsArray)
            }
            salesArray.put(sObj)
        }
        root.put("sales", salesArray)

        root.toString(2)
    }

    suspend fun restoreDatabaseFromJson(jsonString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            database.withTransaction {
                // Clear existing records
                saleDao.clearAllSaleItems()
                saleDao.clearAllSales()
                productDao.clearAll()
                expenseDao.clearAll()

                // Restore Profile
                if (root.has("businessProfile")) {
                    val pObj = root.getJSONObject("businessProfile")
                    val profile = BusinessProfileEntity(
                        businessName = pObj.optString("businessName", "Shehi A. Footwears"),
                        ownerName = pObj.optString("ownerName", ""),
                        phoneNumber = pObj.optString("phoneNumber", ""),
                        email = pObj.optString("email", ""),
                        address = pObj.optString("address", ""),
                        receiptFooterNote = pObj.optString("receiptFooterNote", ""),
                        currencySymbol = pObj.optString("currencySymbol", "₦"),
                        isPinEnabled = pObj.optBoolean("isPinEnabled", false),
                        allowNegativeStockSale = pObj.optBoolean("allowNegativeStockSale", false)
                    )
                    businessProfileDao.insertOrUpdate(profile)
                }

                // Restore Products
                if (root.has("products")) {
                    val pArray = root.getJSONArray("products")
                    val productsToInsert = mutableListOf<ProductEntity>()
                    for (i in 0 until pArray.length()) {
                        val pObj = pArray.getJSONObject(i)
                        productsToInsert.add(
                            ProductEntity(
                                id = pObj.optLong("id", 0L),
                                name = pObj.getString("name"),
                                sku = pObj.optString("sku", ""),
                                category = pObj.optString("category", "General"),
                                size = pObj.optString("size", ""),
                                color = pObj.optString("color", ""),
                                brand = pObj.optString("brand", ""),
                                costPrice = pObj.optDouble("costPrice", 0.0),
                                sellingPrice = pObj.optDouble("sellingPrice", 0.0),
                                quantity = pObj.optInt("quantity", 0),
                                lowStockThreshold = pObj.optInt("lowStockThreshold", 3),
                                photoUri = pObj.optString("photoUri").ifBlank { null },
                                description = pObj.optString("description", ""),
                                createdAt = pObj.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                    productDao.insertAll(productsToInsert)
                }

                // Restore Expenses
                if (root.has("expenses")) {
                    val eArray = root.getJSONArray("expenses")
                    val expensesToInsert = mutableListOf<ExpenseEntity>()
                    for (i in 0 until eArray.length()) {
                        val eObj = eArray.getJSONObject(i)
                        expensesToInsert.add(
                            ExpenseEntity(
                                id = eObj.optLong("id", 0L),
                                title = eObj.getString("title"),
                                category = eObj.optString("category", "Other"),
                                amount = eObj.optDouble("amount", 0.0),
                                date = eObj.optLong("date", System.currentTimeMillis()),
                                notes = eObj.optString("notes", "")
                            )
                        )
                    }
                    expenseDao.insertAll(expensesToInsert)
                }

                // Restore Sales
                if (root.has("sales")) {
                    val sArray = root.getJSONArray("sales")
                    for (i in 0 until sArray.length()) {
                        val sObj = sArray.getJSONObject(i)
                        val sale = SaleEntity(
                            id = sObj.optLong("id", 0L),
                            transactionNumber = sObj.getString("transactionNumber"),
                            totalAmount = sObj.optDouble("totalAmount", 0.0),
                            totalCost = sObj.optDouble("totalCost", 0.0),
                            discountAmount = sObj.optDouble("discountAmount", 0.0),
                            finalAmount = sObj.optDouble("finalAmount", 0.0),
                            grossProfit = sObj.optDouble("grossProfit", 0.0),
                            paymentMethod = sObj.optString("paymentMethod", "Cash"),
                            customerName = sObj.optString("customerName", ""),
                            customerPhone = sObj.optString("customerPhone", ""),
                            notes = sObj.optString("notes", ""),
                            isReversed = sObj.optBoolean("isReversed", false),
                            reverseReason = sObj.optString("reverseReason", ""),
                            createdAt = sObj.optLong("createdAt", System.currentTimeMillis())
                        )
                        val newSaleId = saleDao.insertSale(sale)

                        if (sObj.has("items")) {
                            val itemsArray = sObj.getJSONArray("items")
                            val itemsToInsert = mutableListOf<SaleItemEntity>()
                            for (j in 0 until itemsArray.length()) {
                                val itObj = itemsArray.getJSONObject(j)
                                itemsToInsert.add(
                                    SaleItemEntity(
                                        saleId = if (sale.id > 0) sale.id else newSaleId,
                                        productId = itObj.optLong("productId", 0L),
                                        productName = itObj.getString("productName"),
                                        productSku = itObj.optString("productSku", ""),
                                        productSize = itObj.optString("productSize", ""),
                                        productColor = itObj.optString("productColor", ""),
                                        unitCostPrice = itObj.optDouble("unitCostPrice", 0.0),
                                        unitSellingPrice = itObj.optDouble("unitSellingPrice", 0.0),
                                        quantity = itObj.optInt("quantity", 1),
                                        subtotalAmount = itObj.optDouble("subtotalAmount", 0.0),
                                        subtotalCost = itObj.optDouble("subtotalCost", 0.0),
                                        subtotalProfit = itObj.optDouble("subtotalProfit", 0.0)
                                    )
                                )
                            }
                            saleDao.insertSaleItems(itemsToInsert)
                        }
                    }
                }
            }
            Result.success("Backup restored successfully.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------------- CSV Export ----------------
    suspend fun generateProductsCsv(): String = withContext(Dispatchers.IO) {
        val products = productDao.getAllProducts().firstOrNull() ?: emptyList()
        val sb = StringBuilder()
        sb.append("ID,Product Name,SKU,Category,Size,Color,Brand,Cost Price (NGN),Selling Price (NGN),Quantity,Unit Profit (NGN),Status\n")
        for (p in products) {
            val status = when {
                p.isOutOfStock -> "Out of Stock"
                p.isLowStock -> "Low Stock"
                else -> "In Stock"
            }
            sb.append("\"${p.id}\",")
            sb.append("\"${escapeCsv(p.name)}\",")
            sb.append("\"${escapeCsv(p.sku)}\",")
            sb.append("\"${escapeCsv(p.category)}\",")
            sb.append("\"${escapeCsv(p.size)}\",")
            sb.append("\"${escapeCsv(p.color)}\",")
            sb.append("\"${escapeCsv(p.brand)}\",")
            sb.append("${p.costPrice},")
            sb.append("${p.sellingPrice},")
            sb.append("${p.quantity},")
            sb.append("${p.profitPerUnit},")
            sb.append("\"$status\"\n")
        }
        sb.toString()
    }

    suspend fun generateSalesCsv(): String = withContext(Dispatchers.IO) {
        val sales = saleDao.getAllSalesWithItems().firstOrNull() ?: emptyList()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val sb = StringBuilder()
        sb.append("Transaction ID,Date,Items Summary,Total Qty,Total Selling (NGN),Discount (NGN),Final Amount (NGN),COGS (NGN),Gross Profit (NGN),Payment Method,Customer,Status,Notes\n")
        for (s in sales) {
            val dateStr = dateFormat.format(Date(s.sale.createdAt))
            val itemsSummary = s.items.joinToString("; ") { "${it.productName} (x${it.quantity})" }
            val totalQty = s.items.sumOf { it.quantity }
            val status = if (s.sale.isReversed) "REVERSED (${s.sale.reverseReason})" else "COMPLETED"
            sb.append("\"${s.sale.transactionNumber}\",")
            sb.append("\"$dateStr\",")
            sb.append("\"${escapeCsv(itemsSummary)}\",")
            sb.append("$totalQty,")
            sb.append("${s.sale.totalAmount},")
            sb.append("${s.sale.discountAmount},")
            sb.append("${s.sale.finalAmount},")
            sb.append("${s.sale.totalCost},")
            sb.append("${s.sale.grossProfit},")
            sb.append("\"${s.sale.paymentMethod}\",")
            sb.append("\"${escapeCsv(s.sale.customerName)}\",")
            sb.append("\"$status\",")
            sb.append("\"${escapeCsv(s.sale.notes)}\"\n")
        }
        sb.toString()
    }

    suspend fun generateExpensesCsv(): String = withContext(Dispatchers.IO) {
        val expenses = expenseDao.getAllExpenses().firstOrNull() ?: emptyList()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sb = StringBuilder()
        sb.append("ID,Date,Title,Category,Amount (NGN),Notes\n")
        for (e in expenses) {
            val dateStr = dateFormat.format(Date(e.date))
            sb.append("\"${e.id}\",")
            sb.append("\"$dateStr\",")
            sb.append("\"${escapeCsv(e.title)}\",")
            sb.append("\"${escapeCsv(e.category)}\",")
            sb.append("${e.amount},")
            sb.append("\"${escapeCsv(e.notes)}\"\n")
        }
        sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }
}
