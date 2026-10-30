package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Palette
val SagePrimaryLight = Color(0xFF13775A)
val SageOnPrimaryLight = Color(0xFFFFFFFF)
val SagePrimaryContainerLight = Color(0xFFD1F4E5)
val SageOnPrimaryContainerLight = Color(0xFF003827)

val IndigoSecondaryLight = Color(0xFF4F5ED8)
val IndigoOnSecondaryLight = Color(0xFFFFFFFF)
val IndigoSecondaryContainerLight = Color(0xFFE0E5FF)
val IndigoOnSecondaryContainerLight = Color(0xFF0F1768)

val CoralTertiaryLight = Color(0xFFB54D22)
val CoralOnTertiaryLight = Color(0xFFFFFFFF)
val CoralTertiaryContainerLight = Color(0xFFFFDBD0)
val CoralOnTertiaryContainerLight = Color(0xFF3B1002)

val BackgroundLight = Color(0xFFF8FAFC)
val OnBackgroundLight = Color(0xFF0F172A)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF0F172A)
val SurfaceVariantLight = Color(0xFFE2E8F0)
val OnSurfaceVariantLight = Color(0xFF334155)
val OutlineLight = Color(0xFF94A3B8)

// Dark Palette
val SagePrimaryDark = Color(0xFF34D399)
val SageOnPrimaryDark = Color(0xFF003827)
val SagePrimaryContainerDark = Color(0xFF065F46)
val SageOnPrimaryContainerDark = Color(0xFFD1F4E5)

val IndigoSecondaryDark = Color(0xFF94A3F8)
val IndigoOnSecondaryDark = Color(0xFF1A237E)
val IndigoSecondaryContainerDark = Color(0xFF313FA8)
val IndigoOnSecondaryContainerDark = Color(0xFFE0E5FF)

val CoralTertiaryDark = Color(0xFFFB923C)
val CoralOnTertiaryDark = Color(0xFF5A1E07)
val CoralTertiaryContainerDark = Color(0xFF8A300E)
val CoralOnTertiaryContainerDark = Color(0xFFFFDBD0)

val BackgroundDark = Color(0xFF0B0F17)
val OnBackgroundDark = Color(0xFFF1F5F9)
val SurfaceDark = Color(0xFF151C26)
val OnSurfaceDark = Color(0xFFF1F5F9)
val SurfaceVariantDark = Color(0xFF222B38)
val OnSurfaceVariantDark = Color(0xFF94A3B8)
val OutlineDark = Color(0xFF475569)

// Status & Semantic Colors (High contrast in both light and dark modes)
val ScoreLow = Color(0xFF059669)
val ScoreMedium = Color(0xFFD97706)
val ScoreHigh = Color(0xFFEA580C)
val ScoreSevere = Color(0xFFDC2626)
val ScreenTimeColor = Color(0xFF6366F1)

fun getScoreColor(score: Int): Color {
    return when {
        score >= 76 -> ScoreSevere
        score >= 51 -> ScoreHigh
        score >= 26 -> ScoreMedium
        else -> ScoreLow
    }
}
