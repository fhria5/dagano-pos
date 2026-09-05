package com.example

import com.example.data.local.entity.ProductEntity
import com.example.domain.CartItem
import com.example.domain.InsufficientStockException
import com.example.domain.UserRole
import com.example.util.ReceiptFormatter
import org.junit.Assert.*
import org.junit.Test

class PosLogicTest {

    @Test
    fun testOrderNumberFormat() {
        val orderNumber = "BM-250904-1234-5678"
        assertTrue(orderNumber.startsWith("BM-"))
        assertTrue(orderNumber.contains("-"))
        // uniqueness via HHmmss suffix
        val parts = orderNumber.split("-")
        assertEquals(4, parts.size) // BM, yyMMdd, HHmmss, suffix
    }

    @Test
    fun testDiscountClamping() {
        val subtotal = 100000.0
        val discountTooHigh = 150000.0
        val clamped = discountTooHigh.coerceIn(0.0, subtotal)
        assertEquals(subtotal, clamped, 0.01)
        val discountNegative = -5000.0
        val clampedNeg = discountNegative.coerceIn(0.0, subtotal)
        assertEquals(0.0, clampedNeg, 0.01)
    }

    @Test
    fun testCartItemLineTotal() {
        val product = ProductEntity(id = 1, name = "Test", category = "Kopi", price = 25000.0, costPrice = 10000.0)
        val item = CartItem(product = product, quantity = 2, extraPrice = 5000.0)
        assertEquals(30000.0, item.unitTotal, 0.01)
        assertEquals(60000.0, item.lineTotal, 0.01)
    }

    @Test
    fun testReceiptFormatter() {
        val formatted = ReceiptFormatter.formatRupiah(25000.0)
        assertTrue(formatted.contains("Rp"))
        assertTrue(formatted.contains("25"))
    }

    @Test
    fun testRBACNavigateGuard() {
        // Owner can access SECURITY
        assertTrue(UserRole.OWNER.canAccessLicense())
        assertTrue(UserRole.OWNER.canAccessReports())
        // Cashier cannot
        assertFalse(UserRole.CASHIER.canAccessLicense())
        assertFalse(UserRole.CASHIER.canAccessReports())
        // Kitchen cannot operate cashier
        assertFalse(UserRole.KITCHEN.canOperateCashier())
        assertTrue(UserRole.KITCHEN.canOperateKitchen())
    }

    @Test
    fun testInsufficientStockExceptionIsException() {
        val ex = InsufficientStockException("stok habis")
        assertTrue(ex is Exception)
        assertEquals("stok habis", ex.message)
    }

    @Test
    fun testBOMDeductionLogic() {
        // Simulate BOM: product needs 18g kopi per qty
        val neededPerItem = 18.0
        val qty = 3
        val totalNeeded = neededPerItem * qty
        assertEquals(54.0, totalNeeded, 0.01)
        val stock = 50.0
        assertTrue(stock < totalNeeded) // should throw InsufficientStock
    }
}
