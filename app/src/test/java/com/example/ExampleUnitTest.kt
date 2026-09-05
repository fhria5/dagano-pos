package com.example

import com.example.domain.UserRole
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testUserRolePermissions() {
        // Owner has full privileges
        assertTrue(UserRole.OWNER.canAccessReports())
        assertTrue(UserRole.OWNER.canAccessLicense())
        assertTrue(UserRole.OWNER.canPerformDirectVoid())
        assertTrue(UserRole.OWNER.canManageInventory())

        // Admin has operational & management privileges
        assertTrue(UserRole.ADMIN.canAccessReports())
        assertFalse(UserRole.ADMIN.canAccessLicense())
        assertTrue(UserRole.ADMIN.canPerformDirectVoid())
        assertTrue(UserRole.ADMIN.canManageInventory())

        // Cashier has restricted access
        assertFalse(UserRole.CASHIER.canAccessReports())
        assertFalse(UserRole.CASHIER.canAccessLicense())
        assertFalse(UserRole.CASHIER.canPerformDirectVoid())
        assertFalse(UserRole.CASHIER.canManageInventory())
        assertTrue(UserRole.CASHIER.canOperateCashier())

        // Kitchen role only views KDS
        assertFalse(UserRole.KITCHEN.canOperateCashier())
        assertFalse(UserRole.KITCHEN.canAccessReports())
        assertTrue(UserRole.KITCHEN.canOperateKitchen())
    }

    @Test
    fun testSplitBillEvenDistribution() {
        val totalAmount = 150000.0
        val guestCount = 3
        val amountPerGuest = totalAmount / guestCount

        assertEquals(50000.0, amountPerGuest, 0.01)
    }

    @Test
    fun testPinValidationRules() {
        // PIN must be 4 to 6 digits
        val validPin4 = "1234"
        val validPin6 = "123456"
        val invalidPinShort = "12"
        val invalidPinLong = "1234567"
        val invalidPinNonDigit = "12ab"

        fun isValidPin(pin: String): Boolean {
            return pin.length in 4..6 && pin.all { it.isDigit() }
        }

        assertTrue(isValidPin(validPin4))
        assertTrue(isValidPin(validPin6))
        assertFalse(isValidPin(invalidPinShort))
        assertFalse(isValidPin(invalidPinLong))
        assertFalse(isValidPin(invalidPinNonDigit))
    }
}
