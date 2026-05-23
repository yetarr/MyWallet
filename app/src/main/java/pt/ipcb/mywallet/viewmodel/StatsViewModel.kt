package pt.ipcb.mywallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pt.ipcb.mywallet.data.SessionManager
import pt.ipcb.mywallet.data.local.AppDatabase
import pt.ipcb.mywallet.data.repository.TransactionRepository
import pt.ipcb.mywallet.utils.Formatters
import java.util.Calendar

data class BarData(val label: String, val amount: Double)
data class CategoryData(val category: String, val amount: Double, val percentage: Float)

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = TransactionRepository(AppDatabase.getInstance(app).transactionDao())
    private val session = SessionManager(app)

    private val allTransactions = repo.getAllByUser(session.userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthlyExpenses: StateFlow<List<BarData>> = allTransactions.map { list ->
        (4 downTo 0).map { offset ->
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -offset) }
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            val label = Formatters.monthLabel(cal.timeInMillis)
            val total = list.filter { t ->
                val tc = Calendar.getInstance().apply { timeInMillis = t.date }
                tc.get(Calendar.MONTH) == month && tc.get(Calendar.YEAR) == year && t.isExpense
            }.sumOf { it.amount }
            BarData(label, total)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categoryBreakdown: StateFlow<List<CategoryData>> = allTransactions.map { list ->
        val start = monthStart()
        val expenses = list.filter { it.isExpense && it.date >= start }
        val total = expenses.sumOf { it.amount }
        if (total == 0.0) emptyList()
        else expenses.groupBy { it.category }.map { (cat, txns) ->
            val catTotal = txns.sumOf { it.amount }
            CategoryData(cat, catTotal, (catTotal / total).toFloat())
        }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentMonthIncome: StateFlow<Double> = allTransactions
        .map { list -> list.filter { !it.isExpense && it.date >= monthStart() }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val currentMonthExpenses: StateFlow<Double> = allTransactions
        .map { list -> list.filter { it.isExpense && it.date >= monthStart() }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    private fun monthStart(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
