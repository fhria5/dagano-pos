package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.UmkmDao
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        StockOpnameEntity::class,
        AppMetaEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun umkmDao(): UmkmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val instance = try {
                        Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "umkm_pos.db"
                        )
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .addCallback(DatabaseCallback(scope))
                            .build()
                    } catch (e: Throwable) {
                        try { context.deleteDatabase("umkm_pos.db") } catch (_: Throwable) {}
                        Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "umkm_pos.db"
                        )
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .addCallback(DatabaseCallback(scope))
                            .build()
                    }
                    INSTANCE = instance
                    instance
                }
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.umkmDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: UmkmDao) {
                // Seed produk UMKM contoh (5 produk)
                dao.insertProducts(
                    listOf(
                        ProductEntity(id = 1, name = "Beras 5kg", category = "Sembako", sku = "SEM-BERAS5", priceBuy = 55000.0, priceSell = 62000.0, stock = 20, minStock = 5),
                        ProductEntity(id = 2, name = "Minyak Goreng 2L", category = "Sembako", sku = "SEM-MINYAK2", priceBuy = 28000.0, priceSell = 32000.0, stock = 15, minStock = 3),
                        ProductEntity(id = 3, name = "Kopi Sachet 10s", category = "Minuman", sku = "MIN-KOPI10", priceBuy = 12000.0, priceSell = 15000.0, stock = 40, minStock = 10),
                        ProductEntity(id = 4, name = "Snack Kentang 100g", category = "Snack", sku = "SNK-KENTANG", priceBuy = 8000.0, priceSell = 11000.0, stock = 30, minStock = 8),
                        ProductEntity(id = 5, name = "Sabun Cuci 800ml", category = "Kebutuhan", sku = "KEB-SABUN", priceBuy = 15000.0, priceSell = 18000.0, stock = 12, minStock = 4)
                    )
                )
                dao.insertMeta(AppMetaEntity(key = "last_sync", value = "0"))
                dao.insertMeta(AppMetaEntity(key = "store_name", value = "Toko UMKM"))
            }
        }
    }
}
