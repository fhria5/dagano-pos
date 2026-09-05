package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.AccentTheme
import com.example.ui.UmkmViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class UmkmScreen(val title: String) {
    KASIR("Kasir"),
    STOK("Stok"),
    OPNAME("Opname"),
    LAPORAN("Laporan"),
    PENGATURAN("Pengaturan")
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val def = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try { android.util.Log.e("UmkmCrash", "Uncaught ${t.name}", e) } catch (_: Throwable) {}
            def?.uncaughtException(t, e)
        }
        try { enableEdgeToEdge() } catch (_: Throwable) {}
        setContent {
            val windowSize = calculateWindowSizeClass(this)
            val isTablet = windowSize.widthSizeClass != WindowWidthSizeClass.Compact
            val config = LocalConfiguration.current
            val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            val vm: UmkmViewModel = viewModel()
            val isDark = false
            BistroMateTheme(darkTheme = isDark, accent = AccentTheme.EMERALD) {
                UmkmApp(vm = vm, isTablet = isTablet, isLandscape = isLandscape)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UmkmApp(vm: UmkmViewModel, isTablet: Boolean, isLandscape: Boolean) {
    var screen by remember { mutableStateOf(UmkmScreen.KASIR) }
    val lastSync by vm.lastSyncText.collectAsState()
    val useRail = isTablet || isLandscape

    if (useRail) {
        Row(Modifier.fillMaxSize()) {
            NavigationRail(
                containerColor = adaptiveBackground(),
                modifier = Modifier.testTag("nav_rail")
            ) {
                Spacer(Modifier.height(12.dp))
                NavRailItem(UmkmScreen.KASIR, Icons.Default.PointOfSale, screen) { screen = it }
                NavRailItem(UmkmScreen.STOK, Icons.Default.Inventory2, screen) { screen = it }
                NavRailItem(UmkmScreen.OPNAME, Icons.Default.Compare, screen) { screen = it }
                NavRailItem(UmkmScreen.LAPORAN, Icons.Default.BarChart, screen) { screen = it }
                NavRailItem(UmkmScreen.PENGATURAN, Icons.Default.Settings, screen) { screen = it }
            }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                UmkmContent(vm, screen, isTablet, isLandscape, lastSync)
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = adaptiveBackground(), modifier = Modifier.testTag("nav_bar")) {
                    NavBarItem(UmkmScreen.KASIR, Icons.Default.PointOfSale, screen) { screen = it }
                    NavBarItem(UmkmScreen.STOK, Icons.Default.Inventory2, screen) { screen = it }
                    NavBarItem(UmkmScreen.OPNAME, Icons.Default.Compare, screen) { screen = it }
                    NavBarItem(UmkmScreen.LAPORAN, Icons.Default.BarChart, screen) { screen = it }
                    NavBarItem(UmkmScreen.PENGATURAN, Icons.Default.Settings, screen) { screen = it }
                }
            }
        ) { pad ->
            Box(Modifier.padding(pad)) {
                UmkmContent(vm, screen, isTablet, isLandscape, lastSync)
            }
        }
    }
}

@Composable
private fun UmkmContent(vm: UmkmViewModel, screen: UmkmScreen, isTablet: Boolean, isLandscape: Boolean, lastSync: String) {
    when (screen) {
        UmkmScreen.KASIR -> KasirScreen(vm = vm, isTablet = isTablet, isLandscape = isLandscape, lastSync = lastSync)
        UmkmScreen.STOK -> StokScreen(vm = vm, isTablet = isTablet)
        UmkmScreen.OPNAME -> OpnameScreen(vm = vm)
        UmkmScreen.LAPORAN -> LaporanScreen(vm = vm)
        UmkmScreen.PENGATURAN -> PengaturanScreen(vm = vm, lastSync = lastSync)
    }
}

@Composable
private fun ColumnScope.NavRailItem(s: UmkmScreen, icon: androidx.compose.ui.graphics.vector.ImageVector, current: UmkmScreen, onClick: (UmkmScreen) -> Unit) {
    NavigationRailItem(
        selected = s == current,
        onClick = { onClick(s) },
        icon = { Icon(icon, contentDescription = s.title) },
        label = { Text(s.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
        colors = NavigationRailItemDefaults.colors(selectedIconColor = BentoEmeraldLight, selectedTextColor = BentoEmeraldLight, indicatorColor = BentoEmerald.copy(alpha = 0.2f))
    )
}

@Composable
private fun RowScope.NavBarItem(s: UmkmScreen, icon: androidx.compose.ui.graphics.vector.ImageVector, current: UmkmScreen, onClick: (UmkmScreen) -> Unit) {
    NavigationBarItem(
        selected = s == current,
        onClick = { onClick(s) },
        icon = { Icon(icon, contentDescription = s.title) },
        label = { Text(s.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
    )
}
