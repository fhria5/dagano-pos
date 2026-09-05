package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "products", indices = [Index(value = ["category"]), Index(value = ["sku"], unique = false)])
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "Umum",
    val sku: String = "", // kosong = auto
    val priceBuy: Double = 0.0, // harga beli (modal)
    val priceSell: Double = 0.0, // harga jual
    val stock: Int = 0,
    val minStock: Int = 5, // batas alert menipis per produk
    val isAvailable: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sales", indices = [Index("productId"), Index("date")])
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String = "",
    val date: Long = System.currentTimeMillis(), // epoch millis, filter per tanggal
    val qty: Int,
    val priceSell: Double, // snapshot harga jual saat transaksi
    val total: Double, // qty * priceSell
    val note: String = ""
)

@Entity(tableName = "stock_opname", indices = [Index("productId"), Index("date")])
data class StockOpnameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String = "",
    val date: Long = System.currentTimeMillis(),
    val systemStock: Int,
    val physicalStock: Int,
    val difference: Int, // physical - system
    val reason: String = ""
)

@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

// Keep simple: no BOM, no meja, no kitchen — UMKM fokus kasir + stok
