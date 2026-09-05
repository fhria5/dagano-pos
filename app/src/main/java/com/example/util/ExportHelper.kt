package com.example.util

import android.content.Context
import android.os.Environment
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import java.io.File

object ExportHelper {
    fun exportCsv(context: Context, products: List<ProductEntity>, sales: List<SaleEntity>): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val file = File(dir, "umkm_laporan_${System.currentTimeMillis()}.csv")
        file.writeText(buildString {
            appendLine("Produk, Kategori, SKU, Harga Beli, Harga Jual, Stok, MinStok")
            products.forEach { p ->
                appendLine("${p.name},${p.category},${p.sku},${p.priceBuy},${p.priceSell},${p.stock},${p.minStock}")
            }
            appendLine("")
            appendLine("Penjualan, Produk, Qty, Total, Tanggal")
            sales.forEach { s ->
                appendLine("${s.id},${s.productName},${s.qty},${s.total},${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(s.date))}")
            }
        })
        return file
    }
    // TODO: PDF via iText, upload ke Drive App Data Folder + Sheets via Sheets API
}
