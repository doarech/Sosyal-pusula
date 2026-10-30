package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SagePrimaryDark,
    onPrimary = SageOnPrimaryDark,
    primaryContainer = SagePrimaryContainerDark,
    onPrimaryContainer = SageOnPrimaryContainerDark,
    secondary = IndigoSecondaryDark,
    onSecondary = IndigoOnSecondaryDark,
    secondaryContainer = IndigoSecondaryContainerDark,
    onSecondaryContainer = IndigoOnSecondaryContainerDark,
    tertiary = CoralTertiaryDark,
    onTertiary = CoralOnTertiaryDark,
    tertiaryContainer = CoralTertiaryContainerDark,
    onTertiaryContainer = CoralOnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = SagePrimaryLight,
    onPrimary = SageOnPrimaryLight,
    primaryContainer = SagePrimaryContainerLight,
    onPrimaryContainer = SageOnPrimaryContainerLight,
    secondary = IndigoSecondaryLight,
    onSecondary = IndigoOnSecondaryLight,
    secondaryContainer = IndigoSecondaryContainerLight,
    onSecondaryContainer = IndigoOnSecondaryContainerLight,
    tertiary = CoralTertiaryLight,
    onTertiary = CoralOnTertiaryLight,
    tertiaryContainer = CoralTertiaryContainerLight,
    onTertiaryContainer = CoralOnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

@Composable
fun SosyalPusulaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
