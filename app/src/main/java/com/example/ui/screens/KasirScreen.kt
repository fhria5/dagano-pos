package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.ui.UmkmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasirScreen(vm: UmkmViewModel, isTablet: Boolean, isLandscape: Boolean, lastSync: String) {
    val products by vm.products.collectAsState()
    val lowStock by vm.lowStock.collectAsState()
    val selected by vm.selectedProduct.collectAsState()
    val qty by vm.qty.collectAsState()
    val search by vm.search.collectAsState()
    val toast by vm.toast.collectAsState()

    val filtered = remember(products, search) {
        if (search.isBlank()) products else products.filter { it.name.contains(search, true) || it.sku.contains(search, true) || it.category.contains(search, true) }
    }

    val columns = when {
        isTablet && isLandscape -> 4
        isTablet -> 3
        isLandscape -> 3
        else -> 2
    }

    LaunchedEffect(toast) {
        // auto clear handled in VM
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Kasir", fontWeight = FontWeight.Black); Text(lastSync, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
                actions = {
                    if (lowStock.isNotEmpty()) Badge { Text("${lowStock.size}") }
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(12.dp)) {
            OutlinedTextField(
                value = search,
                onValueChange = { vm.setSearch(it) },
                placeholder = { Text("Cari produk/SKU/kategori") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            if (lowStock.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("⚠️ Stok menipis: ${lowStock.take(3).joinToString { it.name }}", modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
            }
            // Produk grid adaptif
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtered.size, key = { filtered[it].id }) { idx ->
                    val p = filtered[idx]
                    val low = p.stock <= p.minStock
                    Card(
                        onClick = { vm.selectProduct(p) },
                        colors = CardDefaults.cardColors(containerColor = if (p.id == selected?.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                        border = if (low) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            Text(p.name, fontWeight = FontWeight.Bold, maxLines = 2, fontSize = 13.sp)
                            Text(p.category, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text("Rp ${"%,.0f".format(p.priceSell)}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text("Stok ${p.stock} ${if (low) "• Menipis!" else ""}", fontSize = 11.sp, color = if (low) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            // Cart 3-tap: pilih → qty → simpan
            if (selected != null) {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Dipilih: ${selected!!.name}", fontWeight = FontWeight.Bold)
                        Text("Harga Rp ${"%,.0f".format(selected!!.priceSell)} • Stok ${selected!!.stock}", fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { vm.decQty() }) { Icon(Icons.Default.Remove, null) }
                            Text("$qty", fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 16.dp))
                            IconButton(onClick = { vm.incQty() }) { Icon(Icons.Default.Add, null) }
                            Spacer(Modifier.weight(1f))
                            val total = selected!!.priceSell * qty
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total", fontSize = 11.sp)
                                Text("Rp ${"%,.0f".format(total)}", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.createSale() }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Check, null); Spacer(Modifier.width(8.dp)); Text("Simpan Transaksi • 3 tap")
                        }
                        OutlinedButton(onClick = { vm.selectProduct(null) }, modifier = Modifier.fillMaxWidth()) { Text("Batal") }
                    }
                }
            }
            toast?.let {
                Snackbar { Text(it) }
                LaunchedEffect(it) { kotlinx.coroutines.delay(2000); vm.clearToast() }
            }
        }
    }
}
