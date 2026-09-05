package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.domain.UmkmRepository
import com.example.worker.DriveSyncWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class UmkmViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = try { AppDatabase.getDatabase(app, viewModelScope).umkmDao() } catch (e: Throwable) {
        android.util.Log.e("UmkmViewModel", "DB init failed, delete & retry", e)
        try { app.deleteDatabase("umkm_pos.db") } catch (_: Throwable) {}
        try { app.deleteDatabase("umkm_pos.db-shm") } catch (_: Throwable) {}
        try { app.deleteDatabase("umkm_pos.db-wal") } catch (_: Throwable) {}
        AppDatabase.getDatabase(app, viewModelScope).umkmDao()
    }
    private val repo = UmkmRepository(dao)

    val products: StateFlow<List<ProductEntity>> = repo.products.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lowStock: StateFlow<List<ProductEntity>> = repo.lowStock.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sales: StateFlow<List<SaleEntity>> = repo.sales.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val opnames: StateFlow<List<StockOpnameEntity>> = repo.opname.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct

    private val _qty = MutableStateFlow(1)
    val qty: StateFlow<Int> = _qty

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search

    private val _filterFrom = MutableStateFlow(startOfDay(System.currentTimeMillis()))
    private val _filterTo = MutableStateFlow(endOfDay(System.currentTimeMillis()))
    val filterFrom: StateFlow<Long> = _filterFrom
    val filterTo: StateFlow<Long> = _filterTo

    private val _lastSyncText = MutableStateFlow("Tersimpan lokal")
    val lastSyncText: StateFlow<String> = _lastSyncText

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast

    fun setSearch(v: String) { _search.value = v }
    fun selectProduct(p: ProductEntity?) { _selectedProduct.value = p; _qty.value = 1 }
    fun incQty() { _qty.value = (_qty.value + 1).coerceAtMost(999) }
    fun decQty() { _qty.value = (_qty.value - 1).coerceAtLeast(1) }
    fun setQty(v: Int) { _qty.value = v.coerceIn(1, 999) }

    fun setDateFilter(from: Long, to: Long) { _filterFrom.value = from; _filterTo.value = to }

    fun addProduct(name: String, category: String, sku: String, buy: Double, sell: Double, stock: Int, minStock: Int) {
        viewModelScope.launch {
            repo.addProduct(ProductEntity(name = name.trim(), category = category.ifBlank { "Umum" }, sku = sku, priceBuy = buy, priceSell = sell, stock = stock, minStock = minStock))
            _toast.value = "Produk ditambahkan"
            DriveSyncWorker.enqueue(getApplication())
            refreshSyncText()
        }
    }
    fun updateProduct(p: ProductEntity) { viewModelScope.launch { repo.updateProduct(p); _toast.value = "Produk diupdate"; DriveSyncWorker.enqueue(getApplication()); refreshSyncText() } }
    fun deleteProduct(id: Long) { viewModelScope.launch { repo.deleteProduct(id); _toast.value = "Produk dihapus"; DriveSyncWorker.enqueue(getApplication()); refreshSyncText() } }

    fun createSale(note: String = "") {
        val p = _selectedProduct.value ?: run { _toast.value = "Pilih produk dulu"; return }
        val q = _qty.value
        viewModelScope.launch {
            val res = repo.createSale(p.id, q, note)
            if (res.isSuccess) {
                _toast.value = "Transaksi tercatat: ${p.name} x$q"
                _selectedProduct.value = null; _qty.value = 1
                DriveSyncWorker.enqueue(getApplication())
            } else {
                _toast.value = res.exceptionOrNull()?.message ?: "Gagal"
            }
            refreshSyncText()
        }
    }

    fun doOpname(productId: Long, physical: Int, reason: String) {
        viewModelScope.launch {
            try {
                val o = repo.opname(productId, physical, reason)
                _toast.value = "Opname: selisih ${o.difference}"
                DriveSyncWorker.enqueue(getApplication())
                refreshSyncText()
            } catch (e: Exception) { _toast.value = e.message }
        }
    }

    fun clearToast() { _toast.value = null }

    private fun refreshSyncText() {
        viewModelScope.launch {
            val meta = repo.getMeta("last_sync")
            val t = meta?.value?.toLongOrNull() ?: 0L
            _lastSyncText.value = if (t == 0L) "Tersimpan lokal" else "Terakhir sync: ${java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(t))}"
        }
    }

    // Laporan helpers
    suspend fun getSalesBetween(from: Long, to: Long) = repo.getSalesBetween(from, to)
    suspend fun getBestSellers(from: Long, to: Long) = repo.getBestSellers(from, to)
    fun profitOf(sales: List<SaleEntity>, products: Map<Long, ProductEntity>): Double {
        var profit = 0.0
        for (s in sales) {
            val prod = products[s.productId]
            if (prod != null) profit += (s.priceSell - prod.priceBuy) * s.qty
            else profit += s.total // fallback
        }
        return profit
    }

    init { refreshSyncText() }

    companion object {
        fun startOfDay(t: Long): Long {
            val c = Calendar.getInstance(); c.timeInMillis = t; c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0); return c.timeInMillis
        }
        fun endOfDay(t: Long): Long {
            val c = Calendar.getInstance(); c.timeInMillis = t; c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999); return c.timeInMillis
        }
    }
}
