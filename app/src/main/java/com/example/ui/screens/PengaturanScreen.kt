package com.example.ui.screens

import androidx.compose.foundation.layout.*
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UmkmViewModel
import com.example.util.ExportHelper
import com.example.worker.DriveSyncWorker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengaturanScreen(vm: UmkmViewModel, lastSync: String) {
    var showDriveInfo by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val products by vm.products.collectAsState()
    val sales by vm.sales.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Pengaturan", fontWeight = FontWeight.Black) }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDone, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(lastSync, fontWeight = FontWeight.Bold)
                            Text("Offline-first • Data tersimpan lokal, sync berkala ke Drive user (App Data Folder)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Google Drive (milik user)", fontWeight = FontWeight.Bold)
                    Text("Login pakai akun Google Anda. Backup otomatis ke Drive folder aplikasi (hidden, tidak terlihat di My Drive). HP baru login akun sama → data pulih otomatis.", fontSize = 12.sp)
                    Button(onClick = { showDriveInfo = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Login, null); Spacer(Modifier.width(8.dp)); Text("Login Google (Drive milik user)")
                    }
                    OutlinedButton(onClick = {
                        DriveSyncWorker.enqueue(ctx)
                        Toast.makeText(ctx, "Backup dijadwalkan (perlu internet)", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Backup, null); Spacer(Modifier.width(8.dp)); Text("Backup Sekarang") }
                    OutlinedButton(onClick = {
                        Toast.makeText(ctx, "Pulihkan: login Google sama di HP baru → otomatis", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Restore, null); Spacer(Modifier.width(8.dp)); Text("Pulihkan dari Drive") }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Export", fontWeight = FontWeight.Bold)
                    Text("Simpan laporan lokal + opsi upload ke Drive user. Sheets opsional.", fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            scope.launch {
                                val f = ExportHelper.exportCsv(ctx, products, sales)
                                Toast.makeText(ctx, "CSV: ${f.name}", Toast.LENGTH_SHORT).show()
                            }
                        }) { Text("Export CSV") }
                        OutlinedButton(onClick = { Toast.makeText(ctx, "PDF: segera", Toast.LENGTH_SHORT).show() }) { Text("Export PDF") }
                        OutlinedButton(onClick = { Toast.makeText(ctx, "Sheets: butuh login Drive", Toast.LENGTH_SHORT).show() }) { Text("Ke Sheets") }
                    }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tentang", fontWeight = FontWeight.Bold)
                    Text("UMKM Kasir v1.0-umkm • Offline-first 100% • minSdk 24 • Data milik user di Drive mereka sendiri", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    if (showDriveInfo) {
        AlertDialog(
            onDismissRequest = { showDriveInfo = false },
            title = { Text("Drive App Data Folder") },
            text = { Text("Scope: drive.appdata (least privilege). File backup tidak terlihat di Drive UI, hanya di Settings > Manage Apps. Data terhapus jika app di-uninstall (user bisa clear manual). Untuk Sheets/Csv terlihat, perlu drive.file scope terpisah (opsional).") },
            confirmButton = { TextButton(onClick = { showDriveInfo = false }) { Text("OK") } }
        )
    }
}
