package com.example.domain
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class AccentTheme(val primary: Color, val dark: Color, val light: Color) {
    EMERALD(BentoEmerald, BentoEmeraldDark, BentoEmeraldLight),
    AMBER(Color(0xFFF59E0B), Color(0xFFB45309), Color(0xFFFDE68A)),
    BLUE(Color(0xFF3B82F6), Color(0xFF1E40AF), Color(0xFFDBEAFE)),
    PURPLE(Color(0xFF8B5CF6), Color(0xFF5B21B6), Color(0xFFEDE9FE)),
    ROSE(Color(0xFFF43F5E), Color(0xFF881337), Color(0xFFFFE4E6)),
}
