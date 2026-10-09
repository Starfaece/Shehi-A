package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.AppTopBar
import com.example.ui.components.BottomNavBar
import com.example.ui.screens.AddEditProductScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.ProductListScreen
import com.example.ui.screens.ReceiptDialog
import com.example.ui.screens.RecordSaleScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesHistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.ShehiFootwearsTheme
import com.example.ui.viewmodel.ScreenTab
import com.example.ui.viewmodel.ShehiViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class SubScreen {
    object None : SubScreen()
    data class AddEditProduct(val product: ProductEntity? = null) : SubScreen()
    object RecordSale : SubScreen()
    object Expenses : SubScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShehiFootwearsTheme {
                ShehiFootwearsApp()
            }
        }
    }
}

@Composable
fun ShehiFootwearsApp(viewModel: ShehiViewModel = viewModel()) {
    val showSplash by viewModel.showSplash.collectAsStateWithLifecycle()
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    val dashboardMetrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val reportsMetrics by viewModel.reportsMetrics.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allSales by viewModel.allSales.collectAsStateWithLifecycle()
    val filteredSales by viewModel.filteredSales.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val inspectedSale by viewModel.inspectedSale.collectAsStateWithLifecycle()

    val productSearchQuery by viewModel.productSearchQuery.collectAsStateWithLifecycle()
    val selectedProductCategory by viewModel.selectedProductCategory.collectAsStateWithLifecycle()
    val selectedStockFilter by viewModel.selectedStockFilter.collectAsStateWithLifecycle()

    val salesDateFilter by viewModel.salesDateFilter.collectAsStateWithLifecycle()
    val salesPaymentFilter by viewModel.salesPaymentFilter.collectAsStateWithLifecycle()
    val salesSearchQuery by viewModel.salesSearchQuery.collectAsStateWithLifecycle()
    val reportsDateFilter by viewModel.reportsDateFilter.collectAsStateWithLifecycle()

    var activeSubScreen by remember { mutableStateOf<SubScreen>(SubScreen.None) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle Toast / Snackbar messages from ViewModel
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // 1. Splash Screen
    if (showSplash) {
        SplashScreen(
            onContinue = {
                viewModel.dismissSplash()
            }
        )
        return
    }

    // 2. PIN Lock Screen (if enabled & not yet unlocked)
    if (profile.isPinEnabled && !isUnlocked) {
        PinLockScreen(
            expectedPin = profile.pinCode,
            onUnlocked = {
                viewModel.unlockApp(profile.pinCode)
            },
            onResetPinConfirmed = {
                val updated = profile.copy(isPinEnabled = false, pinCode = "")
                viewModel.updateBusinessProfile(updated)
                viewModel.unlockApp("")
            }
        )
        return
    }

    // 3. Sub-screens (Add/Edit Product, Record Sale, Expenses)
    when (val sub = activeSubScreen) {
        is SubScreen.AddEditProduct -> {
            BackHandler { activeSubScreen = SubScreen.None }
            AddEditProductScreen(
                initialProduct = sub.product,
                currencySymbol = profile.currencySymbol,
                onBack = { activeSubScreen = SubScreen.None },
                onSaveProduct = { id, name, sku, category, size, color, brand, cost, sell, qty, threshold, photo, desc ->
                    viewModel.saveProduct(
                        id = id,
                        name = name,
                        sku = sku,
                        category = category,
                        size = size,
                        color = color,
                        brand = brand,
                        costPrice = cost,
                        sellingPrice = sell,
                        quantity = qty,
                        lowStockThreshold = threshold,
                        photoUri = photo,
                        description = desc
                    ) { success ->
                        if (success) {
                            activeSubScreen = SubScreen.None
                        }
                    }
                }
            )
            return
        }

        is SubScreen.RecordSale -> {
            BackHandler { activeSubScreen = SubScreen.None }
            RecordSaleScreen(
                catalogProducts = allProducts,
                cartItems = cartItems,
                profile = profile,
                onBack = { activeSubScreen = SubScreen.None },
                onAddToCart = { prod, qty -> viewModel.addToCart(prod, qty) },
                onUpdateQuantity = { prodId, qty -> viewModel.updateCartItemQuantity(prodId, qty) },
                onRemoveFromCart = { prodId -> viewModel.removeFromCart(prodId) },
                onClearCart = { viewModel.clearCart() },
                onCompleteSale = { paymentMethod, discount, customerName, customerPhone, notes, allowOverride ->
                    viewModel.recordSale(
                        items = cartItems,
                        paymentMethod = paymentMethod,
                        discountAmount = discount,
                        customerName = customerName,
                        customerPhone = customerPhone,
                        notes = notes,
                        allowOverride = allowOverride,
                        onSuccess = {
                            activeSubScreen = SubScreen.None
                        },
                        onError = { errMsg ->
                            scope.launch {
                                snackbarHostState.showSnackbar(errMsg)
                            }
                        }
                    )
                }
            )
            return
        }

        is SubScreen.Expenses -> {
            BackHandler { activeSubScreen = SubScreen.None }
            Scaffold(
                topBar = {
                    AppTopBar(
                        title = "Business Expenses",
                        subtitle = profile.businessName
                    )
                }
            ) { pad ->
                Box(modifier = Modifier.padding(pad)) {
                    ExpensesScreen(
                        expenses = allExpenses,
                        currencySymbol = profile.currencySymbol,
                        onAddExpense = { title, category, amount, date, notes ->
                            viewModel.addExpense(title, category, amount, date, notes) {}
                        },
                        onDeleteExpense = { exp ->
                            viewModel.deleteExpense(exp)
                        }
                    )
                }
            }
            return
        }

        SubScreen.None -> {
            // Main tabs layout below
        }
    }

    // Back handling for main tabs: return to Dashboard first
    if (currentTab != ScreenTab.DASHBOARD) {
        BackHandler {
            viewModel.navigateToTab(ScreenTab.DASHBOARD)
        }
    }

    // 4. Main Tab Navigation
    Scaffold(
        topBar = {
            AppTopBar(
                title = when (currentTab) {
                    ScreenTab.DASHBOARD -> profile.businessName
                    ScreenTab.PRODUCTS -> "Footwear Inventory"
                    ScreenTab.SALES -> "Sales Records"
                    ScreenTab.REPORTS -> "Financial Reports"
                    ScreenTab.EXPENSES -> "Expenses"
                    ScreenTab.SETTINGS -> "Settings & Data"
                },
                subtitle = when (currentTab) {
                    ScreenTab.DASHBOARD -> "Dashboard & Profit Overview"
                    ScreenTab.PRODUCTS -> "${allProducts.size} styles in catalog"
                    ScreenTab.SALES -> "${allSales.size} transactions"
                    ScreenTab.REPORTS -> "P&L, COGS and Net Profit"
                    ScreenTab.EXPENSES -> "Operating costs"
                    ScreenTab.SETTINGS -> "Store profile & backup"
                },
                isPinEnabled = profile.isPinEnabled,
                onLockClick = { viewModel.lockApp() }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { tab -> viewModel.navigateToTab(tab) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.DASHBOARD -> {
                    DashboardScreen(
                        metrics = dashboardMetrics,
                        profile = profile,
                        lowStockItems = lowStockProducts,
                        recentSales = allSales,
                        onNavigateTab = { tab -> viewModel.navigateToTab(tab) },
                        onQuickRecordSale = { activeSubScreen = SubScreen.RecordSale },
                        onQuickAddProduct = { activeSubScreen = SubScreen.AddEditProduct() },
                        onQuickAddExpense = { activeSubScreen = SubScreen.Expenses },
                        onInspectSale = { sale -> viewModel.inspectSale(sale) }
                    )
                }

                ScreenTab.PRODUCTS -> {
                    ProductListScreen(
                        products = filteredProducts,
                        currencySymbol = profile.currencySymbol,
                        searchQuery = productSearchQuery,
                        onSearchQueryChange = { q -> viewModel.setProductSearchQuery(q) },
                        selectedCategory = selectedProductCategory,
                        onCategoryChange = { c -> viewModel.setProductCategory(c) },
                        stockFilter = selectedStockFilter,
                        onStockFilterChange = { s -> viewModel.setStockFilter(s) },
                        onAddProductClick = { activeSubScreen = SubScreen.AddEditProduct() },
                        onEditProductClick = { prod -> activeSubScreen = SubScreen.AddEditProduct(prod) },
                        onDeleteProductClick = { prod -> viewModel.deleteProduct(prod) },
                        onAdjustStock = { prodId, delta -> viewModel.adjustProductStock(prodId, delta) }
                    )
                }

                ScreenTab.SALES -> {
                    SalesHistoryScreen(
                        sales = filteredSales,
                        currencySymbol = profile.currencySymbol,
                        dateFilter = salesDateFilter,
                        onDateFilterChange = { d -> viewModel.setSalesDateFilter(d) },
                        paymentFilter = salesPaymentFilter,
                        onPaymentFilterChange = { p -> viewModel.setSalesPaymentFilter(p) },
                        searchQuery = salesSearchQuery,
                        onSearchQueryChange = { q -> viewModel.setSalesSearchQuery(q) },
                        onViewReceipt = { sale -> viewModel.inspectSale(sale) },
                        onReverseSale = { saleId, reason ->
                            viewModel.reverseSale(saleId, reason) {}
                        }
                    )
                }

                ScreenTab.REPORTS -> {
                    ReportsScreen(
                        metrics = reportsMetrics,
                        profile = profile,
                        selectedPeriod = reportsDateFilter,
                        onPeriodSelected = { p -> viewModel.setReportsDateFilter(p) }
                    )
                }

                ScreenTab.EXPENSES -> {
                    ExpensesScreen(
                        expenses = allExpenses,
                        currencySymbol = profile.currencySymbol,
                        onAddExpense = { title, category, amount, date, notes ->
                            viewModel.addExpense(title, category, amount, date, notes) {}
                        },
                        onDeleteExpense = { exp ->
                            viewModel.deleteExpense(exp)
                        }
                    )
                }

                ScreenTab.SETTINGS -> {
                    SettingsScreen(
                        profile = profile,
                        onSaveProfile = { p -> viewModel.updateBusinessProfile(p) },
                        onExportBackupJson = { cb -> viewModel.exportBackupJson(cb) },
                        onRestoreBackupJson = { json, cb -> viewModel.restoreBackupJson(json, cb) },
                        onExportProductsCsv = { cb -> viewModel.exportProductsCsv(cb) },
                        onExportSalesCsv = { cb -> viewModel.exportSalesCsv(cb) },
                        onExportExpensesCsv = { cb -> viewModel.exportExpensesCsv(cb) },
                        onLoadStarterCatalog = { viewModel.loadStarterCatalog() },
                        onClearAllData = { viewModel.clearAllData() }
                    )
                }
            }
        }

        // Receipt Inspection Dialog
        if (inspectedSale != null) {
            ReceiptDialog(
                saleWithItems = inspectedSale!!,
                profile = profile,
                onDismiss = { viewModel.inspectSale(null) }
            )
        }
    }
}
