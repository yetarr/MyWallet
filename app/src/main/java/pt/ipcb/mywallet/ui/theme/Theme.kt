package pt.ipcb.mywallet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = TealDark,
    onPrimary = Color.White,
    primaryContainer = TealLight,
    onPrimaryContainer = TealText,
    secondary = Amber,
    onSecondary = Color.White,
    secondaryContainer = AmberLight,
    onSecondaryContainer = AmberText,
    tertiary = CoralMid,
    onTertiary = Color.White,
    tertiaryContainer = CoralLight,
    onTertiaryContainer = Coral,
    background = Neutral,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = Neutral,
    onSurfaceVariant = TextSecondary,
    outline = NeutralMid,
    outlineVariant = NeutralDark,
)

@Composable
fun MyWalletTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content,
    )
}
