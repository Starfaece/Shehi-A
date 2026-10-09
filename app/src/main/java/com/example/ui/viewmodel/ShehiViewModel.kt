package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.SaleWithItems
import com.example.data.repository.ShoeBusinessRepository
import com.example.util.DateHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    DASHBOARD,
    PRODUCTS,
    SALES,
    REPORTS,
    EXPENSES,
    SETTINGS
}

enum class DateFilterPeriod {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    ALL_TIME,
    CUSTOM
}

data class DashboardMetrics(
    val todaySalesCount: Int = 0,
    val todayRevenue: Double = 0.0,
    val todayCostOfGoods: Double = 0.0,
    val todayGrossProfit: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val todayNetProfit: Double = 0.0,
    val todayCashReceived: Double = 0.0,
    val todayTransferReceived: Double = 0.0,
    val totalInventoryUnits: Int = 0,
    val totalInventoryCostValue: Double = 0.0,
    val totalInventorySalesValue: Double = 0.0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0
)

data class ReportsMetrics(
    val totalSalesCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalCostOfGoods: Double = 0.0,
    val grossProfit: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val cashReceived: Double = 0.0,
    val transferReceived: Double = 0.0,
    val topSellingProducts: List<TopProductStat> = emptyList(),
    val dailyRevenueBreakdown: List<DailyStat> = emptyList()
)

data class TopProductStat(
    val productName: String,
    val quantitySold: Int,
    val totalRevenue: Double,
    val totalProfit: Double
)

data class DailyStat(
    val dayLabel: String,
    val timestamp: Long,
    val revenue: Double,
    val profit: Double
)

data class CartItem(
    val product: ProductEntity,
    val quantity: Int
)

class ShehiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = ShoeBusinessRepository(database)

    // Navigation and Lock State
    private val _currentTab = MutableStateFlow(ScreenTab.DASHBOARD)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    // Message events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Business Profile / Settings
    val profile: StateFlow<BusinessProfileEntity> = repository.profile
        .combine(MutableStateFlow(Unit)) { prof, _ ->
            prof ?: BusinessProfileEntity()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessProfileEntity())

    // All Products
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product Filter State
    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    private val _selectedProductCategory = MutableStateFlow("All")
    val selectedProductCategory: StateFlow<String> = _selectedProductCategory.asStateFlow()

    private val _selectedStockFilter = MutableStateFlow("All") // "All", "In Stock", "Low Stock", "Out of Stock"
    val selectedStockFilter: StateFlow<String> = _selectedStockFilter.asStateFlow()

    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        productSearchQuery,
        selectedProductCategory,
        selectedStockFilter
    ) { products, query, category, stockFilter ->
        products.filter { p ->
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.sku.contains(query, ignoreCase = true) ||
                    p.brand.contains(query, ignoreCase = true) ||
                    p.color.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || p.category.equals(category, ignoreCase = true)

            val matchesStock = when (stockFilter) {
                "In Stock" -> p.quantity > p.lowStockThreshold
                "Low Stock" -> p.isLowStock
                "Out of Stock" -> p.isOutOfStock
                else -> true
            }

            matchesQuery && matchesCategory && matchesStock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sales and Expenses
    val allSales: StateFlow<List<SaleWithItems>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sales Filter State
    private val _salesDateFilter = MutableStateFlow(DateFilterPeriod.ALL_TIME)
    val salesDateFilter: StateFlow<DateFilterPeriod> = _salesDateFilter.asStateFlow()

    private val _salesPaymentFilter = MutableStateFlow("All") // "All", "Cash", "Bank Transfer"
    val salesPaymentFilter: StateFlow<String> = _salesPaymentFilter.asStateFlow()

    private val _salesSearchQuery = MutableStateFlow("")
    val salesSearchQuery: StateFlow<String> = _salesSearchQuery.asStateFlow()

    val filteredSales: StateFlow<List<SaleWithItems>> = combine(
        allSales,
        salesDateFilter,
        salesPaymentFilter,
        salesSearchQuery
    ) { sales, period, paymentFilter, query ->
        val now = System.currentTimeMillis()
        val startTime = when (period) {
            DateFilterPeriod.TODAY -> DateHelper.getStartOfDay(now)
            DateFilterPeriod.THIS_WEEK -> DateHelper.getStartOfWeek()
            DateFilterPeriod.THIS_MONTH -> DateHelper.getStartOfMonth()
            DateFilterPeriod.ALL_TIME, DateFilterPeriod.CUSTOM -> 0L
        }

        sales.filter { saleWithItems ->
            val sale = saleWithItems.sale
            val matchesDate = sale.createdAt >= startTime
            val matchesPayment = paymentFilter == "All" || sale.paymentMethod.equals(paymentFilter, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    sale.transactionNumber.contains(query, ignoreCase = true) ||
                    sale.customerName.contains(query, ignoreCase = true) ||
                    saleWithItems.items.any { it.productName.contains(query, ignoreCase = true) }

            matchesDate && matchesPayment && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        allSales,
        allExpenses,
        allProducts
    ) { sales, expenses, products ->
        val todayStart = DateHelper.getStartOfDay()
        val todayEnd = DateHelper.getEndOfDay()

        val todayActiveSales = sales.filter {
            it.sale.createdAt in todayStart..todayEnd && !it.sale.isReversed
        }
        val todayActiveExpenses = expenses.filter {
            it.date in todayStart..todayEnd
        }

        val todayRev = todayActiveSales.sumOf { it.sale.finalAmount }
        val todayCost = todayActiveSales.sumOf { it.sale.totalCost }
        val todayGross = todayActiveSales.sumOf { it.sale.grossProfit }
        val todayExp = todayActiveExpenses.sumOf { it.amount }
        val todayNet = todayGross - todayExp

        val cash = todayActiveSales.filter { it.sale.paymentMethod.equals("Cash", ignoreCase = true) }
            .sumOf { it.sale.finalAmount }
        val transfer = todayActiveSales.filter { it.sale.paymentMethod.equals("Bank Transfer", ignoreCase = true) }
            .sumOf { it.sale.finalAmount }

        val totalUnits = products.sumOf { it.quantity }
        val totalCostVal = products.sumOf { it.costPrice * it.quantity }
        val totalSalesVal = products.sumOf { it.sellingPrice * it.quantity }
        val lowStock = products.count { it.isLowStock }
        val outOfStock = products.count { it.isOutOfStock }

        DashboardMetrics(
            todaySalesCount = todayActiveSales.size,
            todayRevenue = todayRev,
            todayCostOfGoods = todayCost,
            todayGrossProfit = todayGross,
            todayExpenses = todayExp,
            todayNetProfit = todayNet,
            todayCashReceived = cash,
            todayTransferReceived = transfer,
            totalInventoryUnits = totalUnits,
            totalInventoryCostValue = totalCostVal,
            totalInventorySalesValue = totalSalesVal,
            lowStockCount = lowStock,
            outOfStockCount = outOfStock
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Reports Metrics
    private val _reportsDateFilter = MutableStateFlow(DateFilterPeriod.THIS_MONTH)
    val reportsDateFilter: StateFlow<DateFilterPeriod> = _reportsDateFilter.asStateFlow()

    val reportsMetrics: StateFlow<ReportsMetrics> = combine(
        allSales,
        allExpenses,
        reportsDateFilter
    ) { sales, expenses, period ->
        val now = System.currentTimeMillis()
        val startTime = when (period) {
            DateFilterPeriod.TODAY -> DateHelper.getStartOfDay(now)
            DateFilterPeriod.THIS_WEEK -> DateHelper.getStartOfWeek()
            DateFilterPeriod.THIS_MONTH -> DateHelper.getStartOfMonth()
            DateFilterPeriod.ALL_TIME, DateFilterPeriod.CUSTOM -> 0L
        }

        val filteredSalesList = sales.filter { it.sale.createdAt >= startTime && !it.sale.isReversed }
        val filteredExpensesList = expenses.filter { it.date >= startTime }

        val totalRev = filteredSalesList.sumOf { it.sale.finalAmount }
        val totalCost = filteredSalesList.sumOf { it.sale.totalCost }
        val grossProfit = filteredSalesList.sumOf { it.sale.grossProfit }
        val totalExp = filteredExpensesList.sumOf { it.amount }
        val netProfit = grossProfit - totalExp

        val cash = filteredSalesList.filter { it.sale.paymentMethod.equals("Cash", ignoreCase = true) }
            .sumOf { it.sale.finalAmount }
        val transfer = filteredSalesList.filter { it.sale.paymentMethod.equals("Bank Transfer", ignoreCase = true) }
            .sumOf { it.sale.finalAmount }

        // Top selling footwear products
        val productMap = mutableMapOf<String, Triple<Int, Double, Double>>() // Name -> (Qty, Revenue, Profit)
        for (sale in filteredSalesList) {
            for (item in sale.items) {
                val current = productMap.getOrDefault(item.productName, Triple(0, 0.0, 0.0))
                productMap[item.productName] = Triple(
                    current.first + item.quantity,
                    current.second + item.subtotalAmount,
                    current.third + item.subtotalProfit
                )
            }
        }
        val topProducts = productMap.map { (name, stats) ->
            TopProductStat(
                productName = name,
                quantitySold = stats.first,
                totalRevenue = stats.second,
                totalProfit = stats.third
            )
        }.sortedByDescending { it.quantitySold }.take(5)

        // Daily revenue breakdown (last 7 days or matching period)
        val dailyStats = mutableListOf<DailyStat>()
        val cal = java.util.Calendar.getInstance()
        for (i in 6 downTo 0) {
            cal.timeInMillis = now
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val dayStart = DateHelper.getStartOfDay(cal.timeInMillis)
            val dayEnd = DateHelper.getEndOfDay(cal.timeInMillis)
            val dayLabel = java.text.SimpleDateFormat("EEE", java.util.Locale.US).format(cal.time)

            val daySales = filteredSalesList.filter { it.sale.createdAt in dayStart..dayEnd }
            dailyStats.add(
                DailyStat(
                    dayLabel = dayLabel,
                    timestamp = dayStart,
                    revenue = daySales.sumOf { it.sale.finalAmount },
                    profit = daySales.sumOf { it.sale.grossProfit }
                )
            )
        }

        ReportsMetrics(
            totalSalesCount = filteredSalesList.size,
            totalRevenue = totalRev,
            totalCostOfGoods = totalCost,
            grossProfit = grossProfit,
            totalExpenses = totalExp,
            netProfit = netProfit,
            cashReceived = cash,
            transferReceived = transfer,
            topSellingProducts = topProducts,
            dailyRevenueBreakdown = dailyStats
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsMetrics())

    // Active Cart for Record Sale
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Receipt Dialog / Detail state
    private val _inspectedSale = MutableStateFlow<SaleWithItems?>(null)
    val inspectedSale: StateFlow<SaleWithItems?> = _inspectedSale.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedStarterDataIfEmpty()
            val prof = repository.getProfileSync()
            if (!prof.isPinEnabled) {
                _isUnlocked.value = true
            }
        }
    }

    // ---------------- Actions ----------------

    fun navigateToTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun dismissSplash() {
        _showSplash.value = false
    }

    fun unlockApp(pin: String): Boolean {
        val currentProfile = profile.value
        if (!currentProfile.isPinEnabled || pin == currentProfile.pinCode) {
            _isUnlocked.value = true
            return true
        }
        return false
    }

    fun lockApp() {
        if (profile.value.isPinEnabled) {
            _isUnlocked.value = false
        }
    }

    fun setProductSearchQuery(q: String) {
        _productSearchQuery.value = q
    }

    fun setProductCategory(cat: String) {
        _selectedProductCategory.value = cat
    }

    fun setStockFilter(filter: String) {
        _selectedStockFilter.value = filter
    }

    fun setSalesDateFilter(period: DateFilterPeriod) {
        _salesDateFilter.value = period
    }

    fun setSalesPaymentFilter(method: String) {
        _salesPaymentFilter.value = method
    }

    fun setSalesSearchQuery(query: String) {
        _salesSearchQuery.value = query
    }

    fun setReportsDateFilter(period: DateFilterPeriod) {
        _reportsDateFilter.value = period
    }

    fun inspectSale(saleWithItems: SaleWithItems?) {
        _inspectedSale.value = saleWithItems
    }

    // Cart Management
    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            val newQty = existing.quantity + quantity
            current[index] = existing.copy(quantity = newQty)
        } else {
            current.add(CartItem(product, quantity))
        }
        _cartItems.value = current
    }

    fun updateCartItemQuantity(productId: Long, quantity: Int) {
        val current = _cartItems.value.toMutableList()
        if (quantity <= 0) {
            current.removeAll { it.product.id == productId }
        } else {
            val index = current.indexOfFirst { it.product.id == productId }
            if (index >= 0) {
                current[index] = current[index].copy(quantity = quantity)
            }
        }
        _cartItems.value = current
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Save or update product
    fun saveProduct(
        id: Long = 0,
        name: String,
        sku: String,
        category: String,
        size: String,
        color: String,
        brand: String,
        costPrice: Double,
        sellingPrice: Double,
        quantity: Int,
        lowStockThreshold: Int = 3,
        photoUri: String? = null,
        description: String = "",
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            if (name.isBlank() || sellingPrice <= 0) {
                _userMessage.emit("Please enter valid product name and selling price.")
                onComplete(false)
                return@launch
            }

            val product = ProductEntity(
                id = id,
                name = name.trim(),
                sku = sku.trim(),
                category = category.trim().ifBlank { "General" },
                size = size.trim(),
                color = color.trim(),
                brand = brand.trim(),
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                quantity = quantity,
                lowStockThreshold = lowStockThreshold,
                photoUri = photoUri,
                description = description.trim()
            )

            if (id == 0L) {
                repository.addProduct(product)
                _userMessage.emit("Product '${product.name}' added successfully.")
            } else {
                repository.updateProduct(product)
                _userMessage.emit("Product '${product.name}' updated.")
            }
            onComplete(true)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _userMessage.emit("Deleted '${product.name}'.")
        }
    }

    fun adjustProductStock(productId: Long, delta: Int) {
        viewModelScope.launch {
            repository.adjustStock(productId, delta)
            val action = if (delta > 0) "Added $delta" else "Reduced ${-delta}"
            _userMessage.emit("Stock adjusted: $action units.")
        }
    }

    // Record Sale
    fun recordSale(
        items: List<CartItem>,
        paymentMethod: String,
        discountAmount: Double,
        customerName: String,
        customerPhone: String,
        notes: String,
        allowOverride: Boolean,
        onSuccess: (SaleWithItems) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (items.isEmpty()) {
                onError("Please add at least one footwear item to the sale.")
                return@launch
            }

            val itemsToSell = items.map { it.product to it.quantity }
            val result = repository.recordSale(
                itemsToSell = itemsToSell,
                paymentMethod = paymentMethod,
                discountAmount = discountAmount,
                customerName = customerName,
                customerPhone = customerPhone,
                notes = notes,
                allowNegativeStock = allowOverride
            )

            result.onSuccess { sale ->
                clearCart()
                val lineItems = items.map {
                    SaleItemEntity(
                        saleId = sale.id,
                        productId = it.product.id,
                        productName = it.product.name,
                        productSku = it.product.sku,
                        productSize = it.product.size,
                        productColor = it.product.color,
                        unitCostPrice = it.product.costPrice,
                        unitSellingPrice = it.product.sellingPrice,
                        quantity = it.quantity,
                        subtotalAmount = it.product.sellingPrice * it.quantity,
                        subtotalCost = it.product.costPrice * it.quantity,
                        subtotalProfit = (it.product.sellingPrice - it.product.costPrice) * it.quantity
                    )
                }
                val saleWithItems = SaleWithItems(sale, lineItems)
                _inspectedSale.value = saleWithItems
                _userMessage.emit("Sale ${sale.transactionNumber} recorded!")
                onSuccess(saleWithItems)
            }.onFailure { error ->
                onError(error.message ?: "Failed to record sale.")
            }
        }
    }

    // Reverse / Refund Sale
    fun reverseSale(saleId: Long, reason: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.reverseSale(saleId, reason)
            result.onSuccess {
                _userMessage.emit("Transaction reversed & stock restored.")
                onComplete(true)
            }.onFailure { err ->
                _userMessage.emit("Failed to reverse: ${err.message}")
                onComplete(false)
            }
        }
    }

    // Expense Management
    fun addExpense(
        title: String,
        category: String,
        amount: Double,
        date: Long,
        notes: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            if (title.isBlank() || amount <= 0) {
                _userMessage.emit("Please enter valid expense title and amount.")
                onComplete(false)
                return@launch
            }
            val expense = ExpenseEntity(
                title = title.trim(),
                category = category.trim().ifBlank { "Other" },
                amount = amount,
                date = date,
                notes = notes.trim()
            )
            repository.addExpense(expense)
            _userMessage.emit("Expense of ₦$amount recorded.")
            onComplete(true)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _userMessage.emit("Expense deleted.")
        }
    }

    // Business Profile / Settings
    fun updateBusinessProfile(newProfile: BusinessProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(newProfile)
            _userMessage.emit("Business details updated.")
        }
    }

    fun loadStarterCatalog() {
        viewModelScope.launch {
            repository.loadStarterCatalog()
            _userMessage.emit("Starter Footwear Catalog loaded!")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            clearCart()
            _userMessage.emit("All business records cleared.")
        }
    }

    fun exportBackupJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDatabaseBackupJson()
            onResult(json)
        }
    }

    fun restoreBackupJson(json: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.restoreDatabaseFromJson(json)
            result.onSuccess { msg ->
                _userMessage.emit("Database backup restored successfully.")
                onResult(true, msg)
            }.onFailure { err ->
                _userMessage.emit("Restore failed: ${err.message}")
                onResult(false, err.message ?: "Unknown error")
            }
        }
    }

    fun exportProductsCsv(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.generateProductsCsv()
            onResult(csv)
        }
    }

    fun exportSalesCsv(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.generateSalesCsv()
            onResult(csv)
        }
    }

    fun exportExpensesCsv(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.generateExpensesCsv()
            onResult(csv)
        }
    }
}
