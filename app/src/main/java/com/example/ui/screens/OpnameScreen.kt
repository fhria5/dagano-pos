package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UmkmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpnameScreen(vm: UmkmViewModel) {
    val products by vm.products.collectAsState()
    val opnames by vm.opnames.collectAsState()
    var physicalMap by remember { mutableStateOf(mapOf<Long, String>()) }
    var reasonMap by remember { mutableStateOf(mapOf<Long, String>()) }

    Scaffold(topBar = { TopAppBar(title = { Text("Opname", fontWeight = FontWeight.Black) }) }) { pad ->
        Column(Modifier.padding(pad).padding(12.dp)) {
            Text("Bandingkan stok sistem vs fisik, catat selisih", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(products, key = { it.id }) { p ->
                    val physStr = physicalMap[p.id] ?: ""
                    val phys = physStr.toIntOrNull()
                    val diff = if (phys != null) phys - p.stock else null
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(p.name, fontWeight = FontWeight.Bold)
                            Text("Sistem ${p.stock} • SKU ${p.sku.ifBlank { "-" }}", fontSize = 11.sp)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = physStr, onValueChange = { physicalMap = physicalMap + (p.id to it) }, label = { Text("Fisik") }, modifier = Modifier.weight(1f), singleLine = true)
                                OutlinedTextField(value = reasonMap[p.id] ?: "", onValueChange = { reasonMap = reasonMap + (p.id to it) }, label = { Text("Alasan") }, modifier = Modifier.weight(1f), singleLine = true)
                            }
                            if (diff != null) {
                                val color = when {
                                    diff == 0 -> MaterialTheme.colorScheme.primary
                                    diff > 0 -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.error
                                }
                                Text("Selisih: $diff", color = color, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val ph = phys ?: return@Button
                                    vm.doOpname(p.id, ph, reasonMap[p.id] ?: "")
                                    physicalMap = physicalMap - p.id
                                    reasonMap = reasonMap - p.id
                                },
                                enabled = phys != null
                            ) { Text("Simpan Opname") }
                        }
                    }
                }
            }
            if (opnames.isNotEmpty()) {
                Divider(Modifier.padding(vertical = 8.dp))
                Text("Riwayat Opname", fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.height(160.dp)) {
                    items(opnames.take(5)) { o ->
                        Text("${o.productName}: sistem ${o.systemStock} → fisik ${o.physicalStock} (selisih ${o.difference}) • ${o.reason}", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
