package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Bento Grid Signature Palette: Obsidian Tech Slate & Glowing Emerald
val BentoEmerald = Color(0xFF10B981)
val BentoEmeraldLight = Color(0xFF34D399)
val BentoEmeraldDark = Color(0xFF047857)
val BentoEmeraldGlow = Color(0xFF6EE7B7)
val BentoEmeraldAlpha = Color(0x2610B981)
val BentoEmeraldBadgeBg = Color(0x3310B981)

// Backward compatible aliases mapped to Bento Emerald
val AmberPrimary = BentoEmerald
val AmberSecondary = BentoEmeraldDark
val AmberAccent = BentoEmeraldLight
val AmberGlow = BentoEmeraldGlow

// Bento Grid Surfaces & Containers (from HTML #0F1115 and #16191F)
val BentoBackground = Color(0xFF0F1115)
val BentoSurface = Color(0xFF16191F)
val BentoCard = Color(0xFF181D26)
val BentoCardElevated = Color(0xFF1E2430)
val BentoBorder = Color(0xFF2A3342)
val BentoBorderSubtle = Color(0x66334155) // border-slate-700/50
val BentoTextPrimary = Color(0xFFF1F5F9)
val BentoTextSecondary = Color(0xFF94A3B8)
val BentoTextMuted = Color(0xFF64748B)

// Dark Theme Surfaces
val DarkBackground = BentoBackground
val DarkSurface = BentoSurface
val DarkSurfaceElevated = BentoCardElevated
val DarkSurfaceVariant = BentoCard
val DarkBorder = BentoBorder
val DarkTextPrimary = BentoTextPrimary
val DarkTextSecondary = BentoTextSecondary

// Light Theme Surfaces
val LightBackground = Color(0xFFF4F6F8)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFEAEFF5)
val LightSurfaceVariant = Color(0xFFDFE6EE)
val LightBorder = Color(0xFFCBD5E1)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)

// Bento Category Accent Badges (Coffee orange, Milk emerald, Pastry blue, Matcha purple, etc.)
val BentoBadgeOrange = Color(0xFFFB923C)
val BentoBadgeOrangeBg = Color(0x33FB923C)
val BentoBadgeBlue = Color(0xFF60A5FA)
val BentoBadgeBlueBg = Color(0x333B82F6)
val BentoBadgePurple = Color(0xFFC084FC)
val BentoBadgePurpleBg = Color(0x33A855F7)
val BentoBadgeRose = Color(0xFFFB7185)
val BentoBadgeRoseBg = Color(0x33F43F5E)

// F&B Functional Status Tokens
val StatusAvailable = Color(0xFF10B981) // Emerald Green
val StatusOccupied = Color(0xFFF43F5E)  // Rose Red
val StatusReserved = Color(0xFFF59E0B)  // Amber Orange
val StatusCooking = Color(0xFF38BDF8)   // Sky Blue
val StatusReady = Color(0xFF34D399)     // Light Emerald
val StatusVoid = Color(0xFFE11D48)      // Crimson Rose
val LowStockAlert = Color(0xFFF43F5E)   // Rose Alert

