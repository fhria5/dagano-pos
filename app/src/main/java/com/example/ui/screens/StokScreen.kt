package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.ui.UmkmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StokScreen(vm: UmkmViewModel, isTablet: Boolean) {
    val products by vm.products.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<ProductEntity?>(null) }
    var search by remember { mutableStateOf("") }
    val filtered = if (search.isBlank()) products else products.filter { it.name.contains(search,true) || it.sku.contains(search,true) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Stok", fontWeight = FontWeight.Black) }) },
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, null) } }
    ) { pad ->
        Column(Modifier.padding(pad).padding(12.dp)) {
            OutlinedTextField(value = search, onValueChange = { search = it }, placeholder = { Text("Cari stok") }, leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { p ->
                    val low = p.stock <= p.minStock
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (low) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(p.name, fontWeight = FontWeight.Bold)
                                    Text("${p.category} • SKU ${p.sku.ifBlank { "-" }}", fontSize = 11.sp)
                                    Text("Beli Rp ${"%,.0f".format(p.priceBuy)} • Jual Rp ${"%,.0f".format(p.priceSell)}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                                Badge(containerColor = if (low) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) { Text("Stok ${p.stock}") }
                            }
                            if (low) Text("⚠️ Menipis! Batas ${p.minStock}", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { editTarget = p }) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Edit") }
                                OutlinedButton(onClick = { vm.deleteProduct(p.id) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp)); Text("Hapus") }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showAdd || editTarget != null) {
        ProductDialog(
            initial = editTarget,
            onDismiss = { showAdd = false; editTarget = null },
            onSave = { name, cat, sku, buy, sell, stock, min ->
                if (editTarget == null) vm.addProduct(name, cat, sku, buy, sell, stock, min)
                else vm.updateProduct(editTarget!!.copy(name = name, category = cat, sku = sku, priceBuy = buy, priceSell = sell, stock = stock, minStock = min))
                showAdd = false; editTarget = null
            }
        )
    }
}

@Composable
private fun ProductDialog(initial: ProductEntity?, onDismiss: () -> Unit, onSave: (String,String,String,Double,Double,Int,Int)->Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var cat by remember { mutableStateOf(initial?.category ?: "Umum") }
    var sku by remember { mutableStateOf(initial?.sku ?: "") }
    var buy by remember { mutableStateOf(initial?.priceBuy?.toString() ?: "") }
    var sell by remember { mutableStateOf(initial?.priceSell?.toString() ?: "") }
    var stock by remember { mutableStateOf(initial?.stock?.toString() ?: "0") }
    var minStock by remember { mutableStateOf(initial?.minStock?.toString() ?: "5") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Tambah Produk" else "Edit Produk") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama*") }, singleLine = true)
                OutlinedTextField(value = cat, onValueChange = { cat = it }, label = { Text("Kategori") }, singleLine = true)
                OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU (kosong=auto)") }, singleLine = true)
                OutlinedTextField(value = buy, onValueChange = { buy = it }, label = { Text("Harga Beli") }, singleLine = true)
                OutlinedTextField(value = sell, onValueChange = { sell = it }, label = { Text("Harga Jual*") }, singleLine = true)
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stok") }, singleLine = true)
                OutlinedTextField(value = minStock, onValueChange = { minStock = it }, label = { Text("Batas Menipis") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank() || sell.toDoubleOrNull() == null) return@Button
                onSave(name, cat, sku, buy.toDoubleOrNull() ?: 0.0, sell.toDouble(), stock.toIntOrNull() ?: 0, minStock.toIntOrNull() ?: 5)
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
