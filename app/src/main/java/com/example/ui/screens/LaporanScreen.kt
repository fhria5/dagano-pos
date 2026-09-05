package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UmkmViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanScreen(vm: UmkmViewModel) {
    val sales by vm.sales.collectAsState()
    val products by vm.products.collectAsState()
    val prodMap = remember(products) { products.associateBy { it.id } }
    var range by remember { mutableStateOf("Harian") } // Harian/Mingguan/Bulanan
    val scope = rememberCoroutineScope()
    var best by remember { mutableStateOf<List<com.example.data.local.dao.BestSeller>>(emptyList()) }
    var totalOmzet by remember { mutableStateOf(0.0) }
    var totalProfit by remember { mutableStateOf(0.0) }

    fun calc() {
        scope.launch {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            val from = when (range) {
                "Mingguan" -> { cal.add(Calendar.DAY_OF_YEAR, -7); cal.timeInMillis }
                "Bulanan" -> { cal.add(Calendar.MONTH, -1); cal.timeInMillis }
                else -> UmkmViewModel.startOfDay(now)
            }
            val to = UmkmViewModel.endOfDay(now)
            val list = vm.getSalesBetween(from, to)
            totalOmzet = list.sumOf { it.total }
            totalProfit = vm.profitOf(list, prodMap)
            best = vm.getBestSellers(from, to)
        }
    }
    LaunchedEffect(range, sales) { calc() }

    Scaffold(topBar = { TopAppBar(title = { Text("Laporan", fontWeight = FontWeight.Black) }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Harian","Mingguan","Bulanan").forEach { r ->
                        FilterChip(selected = range == r, onClick = { range = r }, label = { Text(r) })
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(12.dp)) { Text("Omzet", fontSize = 11.sp); Text("Rp ${"%,.0f".format(totalOmzet)}", fontWeight = FontWeight.Black) }
                    }
                    Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                        Column(Modifier.padding(12.dp)) { Text("Laba", fontSize = 11.sp); Text("Rp ${"%,.0f".format(totalProfit)}", fontWeight = FontWeight.Black) }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Grafik Penjualan (7 hari)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        // bar chart sederhana tanpa lib
                        val last7 = (0..6).map { i ->
                            val cal = Calendar.getInstance(); cal.add(Calendar.DAY_OF_YEAR, -i)
                            val dayStart = UmkmViewModel.startOfDay(cal.timeInMillis)
                            val dayEnd = UmkmViewModel.endOfDay(cal.timeInMillis)
                            val daySales = sales.filter { it.date in dayStart..dayEnd }
                            daySales.sumOf { it.total }
                        }.reversed()
                        val max = (last7.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
                        Row(Modifier.fillMaxWidth().height(80.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            last7.forEach { v ->
                                val h = ((v / max) * 70).toInt().coerceAtLeast(4)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Bottom) {
                                    Box(Modifier.fillMaxWidth().height(h.dp).padding(horizontal = 2.dp), contentAlignment = androidx.compose.ui.Alignment.BottomCenter) {
                                        Surface(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth().height(h.dp)) {}
                                    }
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val fmt = SimpleDateFormat("dd/MM", Locale.getDefault())
                            (0..6).forEach { i ->
                                val cal = Calendar.getInstance(); cal.add(Calendar.DAY_OF_YEAR, -6 + i)
                                Text(fmt.format(Date(cal.timeInMillis)), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Terlaris", fontWeight = FontWeight.Bold)
                        if (best.isEmpty()) Text("Belum ada data", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        best.forEachIndexed { idx, b ->
                            Text("${idx + 1}. ${b.productName} • ${b.totalQty} pcs • Rp ${"%,.0f".format(b.totalSales)}", fontSize = 13.sp)
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {}) { Text("Export PDF") }
                    OutlinedButton(onClick = {}) { Text("Export CSV") }
                }
            }
        }
    }
}
