package com.example.util
object ReceiptFormatter { fun formatRupiah(v: Double): String = "Rp ${"%,.0f".format(v)}" }
