package pt.ipcb.mywallet.ui.transactions

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import pt.ipcb.mywallet.data.local.entity.TransactionEntity
import pt.ipcb.mywallet.ui.components.TealBackHeader
import pt.ipcb.mywallet.ui.dashboard.TransactionDetailDialog
import pt.ipcb.mywallet.ui.dashboard.TransactionItem
import pt.ipcb.mywallet.ui.dashboard.categoryIconAndColor
import pt.ipcb.mywallet.ui.theme.Neutral
import pt.ipcb.mywallet.ui.theme.TextHint
import pt.ipcb.mywallet.utils.Formatters
import pt.ipcb.mywallet.viewmodel.DashboardViewModel

@Composable
fun TransactionsScreen(
    onBackClick: () -> Unit = {},
    onEditClick: (Int) -> Unit = {},
    vm: DashboardViewModel,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val transactions by vm.allTransactionsForMonth.collectAsState()
    val monthLabel by vm.selectedMonthLabel.collectAsState()
    val user by vm.user.collectAsState()
    val currency = user?.currency ?: "EUR"

    var selectedTxn by remember { mutableStateOf<TransactionEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TealBackHeader(
            title = "Transações · $monthLabel",
            onBackClick = onBackClick,
            horizontalPadding = 16,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Neutral)
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding(),
        ) {
            if (transactions.isEmpty()) {
                Text(
                    text = "Sem transações neste mês.",
                    fontSize = 12.sp,
                    color = TextHint,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                transactions.forEach { txn ->
                    val (icon, bg) = categoryIconAndColor(txn.category)
                    TransactionItem(
                        icon = icon,
                        iconBgColor = bg,
                        name = txn.name,
                        category = "${txn.category} · ${Formatters.formatRelativeDate(txn.date)}",
                        amount = if (txn.isExpense)
                            "–${Formatters.formatCurrency(txn.amount, currency)}"
                        else
                            "+${Formatters.formatCurrency(txn.amount, currency)}",
                        isExpense = txn.isExpense,
                        onClick = { selectedTxn = txn },
                    )
                }
            }
        }
    }

    selectedTxn?.let { txn ->
        TransactionDetailDialog(
            txn = txn,
            currency = currency,
            onDismiss = { selectedTxn = null },
            onEditClick = { onEditClick(txn.id) },
            onDeleteClick = { vm.deleteTransaction(txn); selectedTxn = null },
        )
    }
}
