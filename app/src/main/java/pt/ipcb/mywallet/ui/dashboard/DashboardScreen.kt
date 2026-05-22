package pt.ipcb.mywallet.ui.dashboard

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import pt.ipcb.mywallet.ui.components.BottomNavBar
import pt.ipcb.mywallet.ui.components.SectionHeader
import pt.ipcb.mywallet.ui.components.StatCard
import pt.ipcb.mywallet.ui.theme.AmberLight
import pt.ipcb.mywallet.ui.theme.CoralLight
import pt.ipcb.mywallet.ui.theme.CoralMid
import pt.ipcb.mywallet.ui.theme.MyWalletTheme
import pt.ipcb.mywallet.ui.theme.Neutral
import pt.ipcb.mywallet.ui.theme.TealDark
import pt.ipcb.mywallet.ui.theme.TealLight
import pt.ipcb.mywallet.ui.theme.TealMid
import pt.ipcb.mywallet.ui.theme.TextHint
import pt.ipcb.mywallet.ui.theme.TextPrimary

@Composable
fun DashboardScreen(
    navController: NavController,
    onAddClick: () -> Unit = {},
    onSeeAllClick: () -> Unit = {},
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Teal header ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TealDark)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = "Bom dia",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Text(
                        text = "Luís Silva",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TealMid),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "LS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Balance card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(14.dp),
            ) {
                Column {
                    Text(
                        text = "Saldo disponível · Abril",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1 840 €",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Badge(text = "▲ +300 € superávit", containerColor = TealMid, contentColor = Color.White)
                        Badge(text = "Abril 2026", containerColor = Color.White.copy(alpha = 0.15f), contentColor = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }

        // ── Scrollable body ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Neutral)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
        ) {
            InsightBanner(text = "Gastaste +28% em lazer vs março. Continuas no caminho para as férias de verão.")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCard(label = "Rendimentos", value = "1 500 €", isPositive = true, modifier = Modifier.weight(1f))
                StatCard(label = "Despesas", value = "1 200 €", isPositive = false, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            SectionHeader(title = "Últimas transações", linkText = "ver tudo", onLinkClick = onSeeAllClick)

            Spacer(modifier = Modifier.height(8.dp))

            TransactionItem(icon = Icons.Default.ShoppingCart, iconBgColor = TealLight, name = "Pingo Doce", category = "Alimentação · hoje", amount = "–63 €", isExpense = true)
            TransactionItem(icon = Icons.Default.LocalGasStation, iconBgColor = AmberLight, name = "Galp", category = "Transporte · ontem", amount = "–45 €", isExpense = true)
            TransactionItem(icon = Icons.Default.AccountBalance, iconBgColor = TealLight, name = "Salário", category = "Rendimento · 25 abr", amount = "+1 500 €", isExpense = false)
            TransactionItem(icon = Icons.Default.PlayCircle, iconBgColor = CoralLight, name = "Netflix", category = "Subscrições · 22 abr", amount = "–18 €", isExpense = true)
        }

        BottomNavBar(navController = navController, onAddClick = onAddClick)
    }
}

// ── Local helpers ─────────────────────────────────────────────────────────────

@Composable
private fun InsightBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AmberLight)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            tint = pt.ipcb.mywallet.ui.theme.Amber,
            modifier = Modifier.size(14.dp).padding(top = 1.dp),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Column {
            Text(text = "Insight do mês", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = pt.ipcb.mywallet.ui.theme.Amber)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = text, fontSize = 12.sp, color = pt.ipcb.mywallet.ui.theme.AmberText, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun Badge(
    text: String,
    containerColor: Color,
    contentColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .padding(horizontal = 9.dp, vertical = 3.dp),
    ) {
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = contentColor)
    }
}

@Composable
fun TransactionItem(
    icon: ImageVector,
    iconBgColor: Color,
    name: String,
    category: String,
    amount: String,
    isExpense: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (iconBgColor == TealLight) TealDark else if (iconBgColor == AmberLight) pt.ipcb.mywallet.ui.theme.Amber else CoralMid,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(text = category, fontSize = 10.sp, color = TextHint)
        }
        Text(
            text = amount,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isExpense) CoralMid else TealDark,
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    MyWalletTheme { DashboardScreen(navController = rememberNavController()) }
}
