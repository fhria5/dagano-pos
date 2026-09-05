package com.example.domain

import com.example.data.local.dao.UmkmDao
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class UmkmRepository(private val dao: UmkmDao) {
    val products: Flow<List<ProductEntity>> = dao.getAllProducts()
    val lowStock: Flow<List<ProductEntity>> = dao.getLowStockProducts()
    val sales: Flow<List<SaleEntity>> = dao.getAllSales()
    val opname: Flow<List<StockOpnameEntity>> = dao.getAllOpname()
    val metas: Flow<List<AppMetaEntity>> = dao.getAllMeta()

    suspend fun addProduct(p: ProductEntity) = dao.insertProduct(p)
    suspend fun updateProduct(p: ProductEntity) = dao.updateProduct(p.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteProduct(id: Long) = dao.deleteProduct(id)

    suspend fun createSale(productId: Long, qty: Int, note: String = ""): Result<SaleEntity> {
        val prod = dao.getProductById(productId) ?: return Result.failure(IllegalStateException("Produk tidak ditemukan"))
        if (qty <= 0) return Result.failure(IllegalArgumentException("Qty harus >0"))
        if (prod.stock < qty) return Result.failure(IllegalStateException("Stok tidak cukup (${prod.stock})"))
        val deducted = dao.deductStock(productId, qty)
        if (deducted == 0) return Result.failure(IllegalStateException("Stok tidak cukup (race)"))
        val total = prod.priceSell * qty
        val sale = SaleEntity(productId = productId, productName = prod.name, qty = qty, priceSell = prod.priceSell, total = total, note = note)
        dao.insertSale(sale)
        updateLastSyncLocal()
        return Result.success(sale)
    }

    suspend fun voidSale(saleId: Long, productId: Long, qty: Int) {
        dao.deleteSale(saleId)
        dao.addStock(productId, qty)
        updateLastSyncLocal()
    }

    suspend fun opname(productId: Long, physical: Int, reason: String = ""): StockOpnameEntity {
        val prod = dao.getProductById(productId) ?: throw IllegalStateException("Produk tidak ditemukan")
        val system = prod.stock
        val diff = physical - system
        val o = StockOpnameEntity(productId = productId, productName = prod.name, systemStock = system, physicalStock = physical, difference = diff, reason = reason)
        dao.insertOpname(o)
        // set stok ke fisik
        dao.updateProduct(prod.copy(stock = physical, updatedAt = System.currentTimeMillis()))
        updateLastSyncLocal()
        return o
    }

    suspend fun getSalesBetween(from: Long, to: Long) = dao.getSalesBetweenSync(from, to)
    suspend fun getBestSellers(from: Long, to: Long) = dao.getBestSellers(from, to)

    suspend fun getMeta(k: String) = dao.getMeta(k)
    suspend fun setMeta(k: String, v: String) = dao.insertMeta(AppMetaEntity(key = k, value = v))

    private suspend fun updateLastSyncLocal() {
        // indikator lokal — Drive sync akan update ke waktu Drive nanti
        // keep simple: set local timestamp
        dao.updateMeta("last_sync_local", System.currentTimeMillis().toString())
    }
}
