package com.yuki.baynote.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppTheme(val label: String, val previewColor: Color) {
    DEFAULT("Default", Purple40),
    OCEAN("Ocean", OceanPrimary),
    FOREST("Forest", ForestPrimary),
    SUNSET("Sunset", SunsetPrimary),
    MONOCHROME("Mono", MonoPrimary),
    ROSE("Rose", RosePrimary)
}


private val DefaultLightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = PurpleGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Pink40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFEEE8F2),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFEEE8F2),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFDDD6E4),
    onSurfaceVariant = Color(0xFF49454F),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAF8FC),
    surfaceContainer = Color(0xFFEBE4EF),
    surfaceContainerHigh = Color(0xFFE2DBE8),
    surfaceContainerHighest = Color(0xFFD9D2E0),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

private val DefaultDarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = PurpleGrey80,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Pink80,
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainerLowest = Color(0xFF110F13),
    surfaceContainerLow = Color(0xFF1D1B20),
    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2B2930),
    surfaceContainerHighest = Color(0xFF36343B),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)


private val OceanLightColorScheme = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0A2E4D),
    secondary = OceanSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC3D8F0),
    onSecondaryContainer = Color(0xFF0B2540),
    tertiary = OceanTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2EBF2),
    onTertiaryContainer = Color(0xFF003E45),
    background = Color(0xFFD6E4F2),
    onBackground = Color(0xFF141C24),
    surface = Color(0xFFD6E4F2),
    onSurface = Color(0xFF141C24),
    surfaceVariant = Color(0xFFBACDE2),
    onSurfaceVariant = Color(0xFF374555),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F8FC),
    surfaceContainer = Color(0xFFD0DEF0),
    surfaceContainerHigh = Color(0xFFC4D6EA),
    surfaceContainerHighest = Color(0xFFB8CEE4),
    outline = Color(0xFF5A7289),
    outlineVariant = Color(0xFFADBFD1)
)

private val OceanDarkColorScheme = darkColorScheme(
    primary = OceanPrimaryDark,
    onPrimary = Color(0xFF0A3A6B),
    primaryContainer = Color(0xFF1A4C80),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = OceanSecondaryDark,
    onSecondary = Color(0xFF082E5A),
    secondaryContainer = Color(0xFF143D6E),
    onSecondaryContainer = Color(0xFFC3D8F0),
    tertiary = OceanTertiaryDark,
    onTertiary = Color(0xFF004D56),
    tertiaryContainer = Color(0xFF006874),
    onTertiaryContainer = Color(0xFFB2EBF2),
    background = Color(0xFF0E1620),
    onBackground = Color(0xFFD4DEE8),
    surface = Color(0xFF0E1620),
    onSurface = Color(0xFFD4DEE8),
    surfaceVariant = Color(0xFF2A3A4E),
    onSurfaceVariant = Color(0xFFB0C2D6),
    surfaceContainerLowest = Color(0xFF08101A),
    surfaceContainerLow = Color(0xFF121C28),
    surfaceContainer = Color(0xFF182430),
    surfaceContainerHigh = Color(0xFF1F2D3C),
    surfaceContainerHighest = Color(0xFF273747),
    outline = Color(0xFF7B92A8),
    outlineVariant = Color(0xFF374E64)
)


private val ForestLightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF0A3012),
    secondary = ForestSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB8DABC),
    onSecondaryContainer = Color(0xFF062810),
    tertiary = ForestTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCE8B4),
    onTertiaryContainer = Color(0xFF1A2E08),
    background = Color(0xFFD6E6D6),
    onBackground = Color(0xFF161D16),
    surface = Color(0xFFD6E6D6),
    onSurface = Color(0xFF161D16),
    surfaceVariant = Color(0xFFBBCEBB),
    onSurfaceVariant = Color(0xFF3A4A3A),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5FAF5),
    surfaceContainer = Color(0xFFCEDFCE),
    surfaceContainerHigh = Color(0xFFC2D6C2),
    surfaceContainerHighest = Color(0xFFB6CCB6),
    outline = Color(0xFF5C725C),
    outlineVariant = Color(0xFFABC1AB)
)

private val ForestDarkColorScheme = darkColorScheme(
    primary = ForestPrimaryDark,
    onPrimary = Color(0xFF0A3A12),
    primaryContainer = Color(0xFF1A5420),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = ForestSecondaryDark,
    onSecondary = Color(0xFF073310),
    secondaryContainer = Color(0xFF124A18),
    onSecondaryContainer = Color(0xFFB8DABC),
    tertiary = ForestTertiaryDark,
    onTertiary = Color(0xFF2A4A10),
    tertiaryContainer = Color(0xFF3D6420),
    onTertiaryContainer = Color(0xFFDCE8B4),
    background = Color(0xFF0E160E),
    onBackground = Color(0xFFD3E0D3),
    surface = Color(0xFF0E160E),
    onSurface = Color(0xFFD3E0D3),
    surfaceVariant = Color(0xFF2A3C2A),
    onSurfaceVariant = Color(0xFFAFC4AF),
    surfaceContainerLowest = Color(0xFF081008),
    surfaceContainerLow = Color(0xFF121C12),
    surfaceContainer = Color(0xFF18241A),
    surfaceContainerHigh = Color(0xFF1F2E22),
    surfaceContainerHighest = Color(0xFF27382A),
    outline = Color(0xFF7C947C),
    outlineVariant = Color(0xFF3A5A3C)
)


private val SunsetLightColorScheme = lightColorScheme(
    primary = SunsetPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDDB8),
    onPrimaryContainer = Color(0xFF4A1E00),
    secondary = SunsetSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFCCBC),
    onSecondaryContainer = Color(0xFF3D1406),
    tertiary = SunsetTertiary,
    onTertiary = Color(0xFF3E2E00),
    tertiaryContainer = Color(0xFFFFF0C2),
    onTertiaryContainer = Color(0xFF3A2D00),
    background = Color(0xFFF0DFCC),
    onBackground = Color(0xFF241A10),
    surface = Color(0xFFF0DFCC),
    onSurface = Color(0xFF241A10),
    surfaceVariant = Color(0xFFE2CCAF),
    onSurfaceVariant = Color(0xFF55422E),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFCF6EF),
    surfaceContainer = Color(0xFFE8D8C4),
    surfaceContainerHigh = Color(0xFFE0CCB5),
    surfaceContainerHighest = Color(0xFFD8C2A8),
    outline = Color(0xFF8C7560),
    outlineVariant = Color(0xFFD4B898)
)

private val SunsetDarkColorScheme = darkColorScheme(
    primary = SunsetPrimaryDark,
    onPrimary = Color(0xFF6E2F00),
    primaryContainer = Color(0xFF8A3D00),
    onPrimaryContainer = Color(0xFFFFDDB8),
    secondary = SunsetSecondaryDark,
    onSecondary = Color(0xFF5E1F06),
    secondaryContainer = Color(0xFF742A0C),
    onSecondaryContainer = Color(0xFFFFCCBC),
    tertiary = SunsetTertiaryDark,
    onTertiary = Color(0xFF3E2E00),
    tertiaryContainer = Color(0xFF5A4800),
    onTertiaryContainer = Color(0xFFFFF0C2),
    background = Color(0xFF1C1208),
    onBackground = Color(0xFFE8D8C4),
    surface = Color(0xFF1C1208),
    onSurface = Color(0xFFE8D8C4),
    surfaceVariant = Color(0xFF44362A),
    onSurfaceVariant = Color(0xFFD4BFA8),
    surfaceContainerLowest = Color(0xFF140E04),
    surfaceContainerLow = Color(0xFF201810),
    surfaceContainer = Color(0xFF281E14),
    surfaceContainerHigh = Color(0xFF30261C),
    surfaceContainerHighest = Color(0xFF3C3024),
    outline = Color(0xFF9C8870),
    outlineVariant = Color(0xFF544434)
)


private val MonoLightColorScheme = lightColorScheme(
    primary = MonoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6D6D6),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = MonoSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCECECE),
    onSecondaryContainer = Color(0xFF222222),
    tertiary = MonoTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0E0E0),
    onTertiaryContainer = Color(0xFF2A2A2A),
    background = Color(0xFFE0E0E0),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFE0E0E0),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFCECECE),
    onSurfaceVariant = Color(0xFF3A3A3A),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F8F8),
    surfaceContainer = Color(0xFFD6D6D6),
    surfaceContainerHigh = Color(0xFFCCCCCC),
    surfaceContainerHighest = Color(0xFFC2C2C2),
    outline = Color(0xFF6E6E6E),
    outlineVariant = Color(0xFFB0B0B0)
)

private val MonoDarkColorScheme = darkColorScheme(
    primary = MonoPrimaryDark,
    onPrimary = Color(0xFF2A2A2A),
    primaryContainer = Color(0xFF4A4A4A),
    onPrimaryContainer = Color(0xFFD6D6D6),
    secondary = MonoSecondaryDark,
    onSecondary = Color(0xFF353535),
    secondaryContainer = Color(0xFF525252),
    onSecondaryContainer = Color(0xFFCECECE),
    tertiary = MonoTertiaryDark,
    onTertiary = Color(0xFF404040),
    tertiaryContainer = Color(0xFF5E5E5E),
    onTertiaryContainer = Color(0xFFE0E0E0),
    background = Color(0xFF141414),
    onBackground = Color(0xFFDCDCDC),
    surface = Color(0xFF141414),
    onSurface = Color(0xFFDCDCDC),
    surfaceVariant = Color(0xFF383838),
    onSurfaceVariant = Color(0xFFBEBEBE),
    surfaceContainerLowest = Color(0xFF0C0C0C),
    surfaceContainerLow = Color(0xFF1A1A1A),
    surfaceContainer = Color(0xFF222222),
    surfaceContainerHigh = Color(0xFF2C2C2C),
    surfaceContainerHighest = Color(0xFF363636),
    outline = Color(0xFF8A8A8A),
    outlineVariant = Color(0xFF444444)
)


private val RoseLightColorScheme = lightColorScheme(
    primary = RosePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCDBE5),
    onPrimaryContainer = Color(0xFF3E0720),
    secondary = RoseSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5D0DC),
    onSecondaryContainer = Color(0xFF32051A),
    tertiary = RoseTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD6),
    onTertiaryContainer = Color(0xFF410002),
    background = Color(0xFFEED6DE),
    onBackground = Color(0xFF241418),
    surface = Color(0xFFEED6DE),
    onSurface = Color(0xFF241418),
    surfaceVariant = Color(0xFFE2BBCA),
    onSurfaceVariant = Color(0xFF553842),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFCF2F5),
    surfaceContainer = Color(0xFFE6CCD6),
    surfaceContainerHigh = Color(0xFFDEC0CC),
    surfaceContainerHighest = Color(0xFFD6B4C2),
    outline = Color(0xFF8E6B76),
    outlineVariant = Color(0xFFD4AEBC)
)

private val RoseDarkColorScheme = darkColorScheme(
    primary = RosePrimaryDark,
    onPrimary = Color(0xFF5E0A30),
    primaryContainer = Color(0xFF7A1042),
    onPrimaryContainer = Color(0xFFFCDBE5),
    secondary = RoseSecondaryDark,
    onSecondary = Color(0xFF4A0728),
    secondaryContainer = Color(0xFF650C38),
    onSecondaryContainer = Color(0xFFF5D0DC),
    tertiary = RoseTertiaryDark,
    onTertiary = Color(0xFF601414),
    tertiaryContainer = Color(0xFF7D1C1C),
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1E0E14),
    onBackground = Color(0xFFE8D0D8),
    surface = Color(0xFF1E0E14),
    onSurface = Color(0xFFE8D0D8),
    surfaceVariant = Color(0xFF44282E),
    onSurfaceVariant = Color(0xFFD4B0BA),
    surfaceContainerLowest = Color(0xFF16080C),
    surfaceContainerLow = Color(0xFF221418),
    surfaceContainer = Color(0xFF2A1A1E),
    surfaceContainerHigh = Color(0xFF342228),
    surfaceContainerHighest = Color(0xFF3E2C32),
    outline = Color(0xFF9E7886),
    outlineVariant = Color(0xFF543840)
)


fun colorSchemeFor(theme: AppTheme, dark: Boolean): ColorScheme = when (theme) {
    AppTheme.DEFAULT    -> if (dark) DefaultDarkColorScheme else DefaultLightColorScheme
    AppTheme.OCEAN      -> if (dark) OceanDarkColorScheme else OceanLightColorScheme
    AppTheme.FOREST     -> if (dark) ForestDarkColorScheme else ForestLightColorScheme
    AppTheme.SUNSET     -> if (dark) SunsetDarkColorScheme else SunsetLightColorScheme
    AppTheme.MONOCHROME -> if (dark) MonoDarkColorScheme else MonoLightColorScheme
    AppTheme.ROSE       -> if (dark) RoseDarkColorScheme else RoseLightColorScheme
}

@Composable
fun BaynoteTheme(
    appTheme: AppTheme = AppTheme.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        appTheme == AppTheme.DEFAULT && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> colorSchemeFor(appTheme, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
