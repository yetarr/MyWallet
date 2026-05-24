package pt.ipcb.mywallet.ui.dashboard

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import pt.ipcb.mywallet.data.local.entity.TransactionEntity
import pt.ipcb.mywallet.navigation.Screen
import pt.ipcb.mywallet.ui.components.BottomNavBar
import pt.ipcb.mywallet.ui.components.SectionHeader
import pt.ipcb.mywallet.ui.components.StatCard
import pt.ipcb.mywallet.ui.theme.Amber
import pt.ipcb.mywallet.ui.theme.AmberLight
import pt.ipcb.mywallet.ui.theme.AmberText
import pt.ipcb.mywallet.ui.theme.CoralLight
import pt.ipcb.mywallet.ui.theme.CoralMid
import pt.ipcb.mywallet.ui.theme.MyWalletTheme
import pt.ipcb.mywallet.ui.theme.Neutral
import pt.ipcb.mywallet.ui.theme.NeutralMid
import pt.ipcb.mywallet.ui.theme.TealDark
import pt.ipcb.mywallet.ui.theme.TealLight
import pt.ipcb.mywallet.ui.theme.TealMid
import pt.ipcb.mywallet.ui.theme.TextHint
import pt.ipcb.mywallet.ui.theme.TextPrimary
import pt.ipcb.mywallet.ui.theme.TextSecondary
import pt.ipcb.mywallet.utils.Formatters
import pt.ipcb.mywallet.viewmodel.DashboardViewModel
import java.util.Calendar

@Composable
fun DashboardScreen(
    navController: NavController,
    onAddClick: () -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    vm: DashboardViewModel = viewModel(),
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val user by vm.user.collectAsState()
    val income by vm.currentMonthIncome.collectAsState()
    val expenses by vm.currentMonthExpenses.collectAsState()
    val totalBalance by vm.totalBalance.collectAsState()
    val transactions by vm.recentTransactions.collectAsState()
    val monthOffset by vm.monthOffset.collectAsState()
    val monthLabel by vm.selectedMonthLabel.collectAsState()
    val selectedMonthInYear by vm.selectedMonthInYear.collectAsState()
    val availableMonthCount = vm.availableMonthCount

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when { hour < 12 -> "Bom dia"; hour < 18 -> "Boa tarde"; else -> "Boa noite" }
    val displayName = user?.let { "${it.firstName} ${it.lastName}" } ?: ""
    val initials = user?.let { "${it.firstName.firstOrNull() ?: ""}${it.lastName.firstOrNull() ?: ""}" }?.uppercase() ?: ""
    val currency = user?.currency ?: "EUR"
    val monthlyNet = income - expenses

    var swipeAccum by remember { mutableFloatStateOf(0f) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedTxn by remember { mutableStateOf<TransactionEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Teal header ────────────────────────────────────────────────────────
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
                    Text(text = greeting, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(text = displayName, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                // Tap avatar → logout dialog
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TealMid)
                        .clickable { showLogoutDialog = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = initials, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Swipeable balance card ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                when {
                                    swipeAccum < -80f -> vm.prevMonth()
                                    swipeAccum > 80f -> vm.nextMonth()
                                }
                                swipeAccum = 0f
                            },
                            onDragCancel = { swipeAccum = 0f },
                            onHorizontalDrag = { _, delta -> swipeAccum += delta },
                        )
                    }
                    .padding(14.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val canGoPrev = monthOffset > -(availableMonthCount - 1)
                        val canGoNext = monthOffset < 0
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Mês anterior",
                                tint = if (canGoPrev) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(18.dp).clickable(enabled = canGoPrev) { vm.prevMonth() },
                            )
                            Text(text = "Saldo · $monthLabel", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Mês seguinte",
                            tint = if (canGoNext) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable(enabled = canGoNext) { vm.nextMonth() },
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Formatters.formatCurrency(totalBalance, currency),
                        fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White,
                        letterSpacing = (-0.5).sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        val surplusText = if (monthlyNet >= 0)
                            "▲ +${Formatters.formatCurrency(monthlyNet, currency)} superávit"
                        else
                            "▼ ${Formatters.formatCurrency(-monthlyNet, currency)} défice"
                        Badge(text = surplusText, containerColor = TealMid, contentColor = Color.White)
                        Badge(text = monthLabel, containerColor = Color.White.copy(alpha = 0.15f), contentColor = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // ── Month dot indicators ───────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(availableMonthCount) { i ->
                    val isSelected = i == selectedMonthInYear
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.35f)
                            )
                            .clickable { vm.goToMonthIndex(i) },
                    )
                }
            }
        }

        // ── Scrollable body ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Neutral)
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding(),
        ) {
            InsightBanner(income = income, expenses = expenses, currency = currency)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(label = "Rendimentos", value = Formatters.formatCurrency(income, currency), isPositive = true, modifier = Modifier.weight(1f))
                StatCard(label = "Despesas", value = Formatters.formatCurrency(expenses, currency), isPositive = false, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))
            SectionHeader(title = "Transações · $monthLabel", linkText = "ver tudo", onLinkClick = onSeeAllClick)
            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Text(text = "Sem transações neste mês.", fontSize = 12.sp, color = TextHint, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                transactions.forEach { txn ->
                    val (icon, bg) = categoryIconAndColor(txn.category)
                    TransactionItem(
                        icon = icon, iconBgColor = bg,
                        name = txn.name,
                        category = "${txn.category} · ${Formatters.formatRelativeDate(txn.date)}",
                        amount = if (txn.isExpense) "–${Formatters.formatCurrency(txn.amount, currency)}" else "+${Formatters.formatCurrency(txn.amount, currency)}",
                        isExpense = txn.isExpense,
                        onClick = { selectedTxn = txn },
                    )
                }
            }
        }

        BottomNavBar(navController = navController, onAddClick = onAddClick)
    }

    // ── Logout dialog ──────────────────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = displayName, fontWeight = FontWeight.SemiBold) },
            text = { Text("Terminar sessão e voltar ao ecrã de login?", fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = {
                    vm.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }) { Text("Sair", color = CoralMid) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") } },
            containerColor = Color.White,
        )
    }

    // ── Transaction detail dialog ──────────────────────────────────────────────
    selectedTxn?.let { txn ->
        TransactionDetailDialog(
            txn = txn,
            currency = currency,
            onDismiss = { selectedTxn = null },
            onEditClick = {
                navController.navigate(Screen.AddExpense.editRoute(txn.id))
            },
        )
    }
}

// ── Transaction detail dialog ─────────────────────────────────────────────────

@Composable
fun TransactionDetailDialog(
    txn: TransactionEntity,
    currency: String,
    onDismiss: () -> Unit,
    onEditClick: (() -> Unit)? = null,
) {
    val (icon, bg) = categoryIconAndColor(txn.category)
    val iconTint = when (bg) { TealLight -> TealDark; AmberLight -> Amber; else -> CoralMid }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(bg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                }
                Text(text = txn.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (txn.isExpense) "–${Formatters.formatCurrency(txn.amount, currency)}" else "+${Formatters.formatCurrency(txn.amount, currency)}",
                    fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = if (txn.isExpense) CoralMid else TealDark,
                )
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(NeutralMid))
                DetailRow(label = "Categoria", value = txn.category)
                DetailRow(label = "Data", value = Formatters.formatDate(txn.date))
                if (txn.description.isNotBlank()) DetailRow(label = "Descrição", value = txn.description)
                val typeLabel = when {
                    txn.isRecurring && txn.endDate != null -> "Recorrente até ${Formatters.formatDate(txn.endDate)}"
                    txn.isRecurring -> "Recorrente"
                    else -> "Única vez"
                }
                DetailRow(label = "Tipo", value = typeLabel)
                if (txn.locationName != null) DetailRow(
                    label = "Localização",
                    value = txn.locationName,
                )
            }
        },
        confirmButton = {
            if (onEditClick != null) {
                TextButton(onClick = { onDismiss(); onEditClick() }) { Text("Editar", color = TealDark) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
        containerColor = Color.White,
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextHint,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.weight(0.6f),
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            textAlign = TextAlign.End
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

fun categoryIconAndColor(category: String): Pair<ImageVector, Color> = when (category) {
    "Alimentação" -> Icons.Default.ShoppingCart to TealLight
    "Transporte" -> Icons.Default.DirectionsCar to AmberLight
    "Lazer" -> Icons.Default.SportsEsports to CoralLight
    "Casa" -> Icons.Default.Home to Neutral
    "Saúde" -> Icons.Default.MedicalServices to TealLight
    "Subscr." -> Icons.Default.Subscriptions to AmberLight
    "Compras" -> Icons.Default.ShoppingBag to CoralLight
    "Rendimento" -> Icons.Default.AccountBalance to TealLight
    else -> Icons.Default.Category to Neutral
}

@Composable
private fun InsightBanner(income: Double, expenses: Double, currency: String) {
    val text = when {
        expenses == 0.0 && income == 0.0 -> "Ainda não tens transações este mês. Começa a registar!"
        income >= expenses -> "Estás no verde este mês! Poupaste ${Formatters.formatCurrency(income - expenses, currency)}."
        else -> "Gastaste mais do que recebeste. Diferença de ${Formatters.formatCurrency(expenses - income, currency)}."
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AmberLight)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = Amber, modifier = Modifier.size(14.dp).padding(top = 1.dp))
        Spacer(modifier = Modifier.size(6.dp))
        Column {
            Text(text = "Insight do mês", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Amber)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = text, fontSize = 12.sp, color = AmberText, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun Badge(text: String, containerColor: Color, contentColor: Color) {
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
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(iconBgColor),
            contentAlignment = Alignment.Center,
        ) {
            val iconTint = when (iconBgColor) { TealLight -> TealDark; AmberLight -> Amber; else -> CoralMid }
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(text = category, fontSize = 10.sp, color = TextHint)
        }
        Text(text = amount, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isExpense) CoralMid else TealDark)
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    MyWalletTheme { DashboardScreen(navController = rememberNavController()) }
}
