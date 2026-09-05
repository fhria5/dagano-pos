package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UmkmDao {
    // Products
    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock <= minStock ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(p: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(list: List<ProductEntity>)

    @Update
    suspend fun updateProduct(p: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)

    @Query("UPDATE products SET stock = stock - :qty WHERE id = :id AND stock >= :qty")
    suspend fun deductStock(id: Long, qty: Int): Int

    @Query("UPDATE products SET stock = stock + :qty WHERE id = :id")
    suspend fun addStock(id: Long, qty: Int)

    // Sales
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun getSalesBetween(from: Long, to: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    suspend fun getSalesBetweenSync(from: Long, to: Long): List<SaleEntity>

    @Query("SELECT productId, productName, SUM(qty) as totalQty, SUM(total) as totalSales FROM sales WHERE date BETWEEN :from AND :to GROUP BY productId ORDER BY totalQty DESC LIMIT 5")
    suspend fun getBestSellers(from: Long, to: Long): List<BestSeller>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(s: SaleEntity): Long

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSale(id: Long)

    // Opname
    @Query("SELECT * FROM stock_opname ORDER BY date DESC")
    fun getAllOpname(): Flow<List<StockOpnameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpname(o: StockOpnameEntity): Long

    // Meta (last_sync, store_name)
    @Query("SELECT * FROM app_meta WHERE `key` = :k LIMIT 1")
    suspend fun getMeta(k: String): AppMetaEntity?

    @Query("SELECT * FROM app_meta")
    fun getAllMeta(): Flow<List<AppMetaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeta(m: AppMetaEntity)

    @Query("UPDATE app_meta SET value = :v, updatedAt = :t WHERE `key` = :k")
    suspend fun updateMeta(k: String, v: String, t: Long = System.currentTimeMillis())
}

data class BestSeller(
    val productId: Long,
    val productName: String,
    val totalQty: Int,
    val totalSales: Double
)
