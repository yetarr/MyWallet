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
import pt.ipcb.mywallet.data.local.entity.TransactionEntity
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
            val (start, end) = monthRange(offset)
            val label = Formatters.monthLabel(start)
            val total = list.filter { t -> t.isExpense && transactionActiveInMonth(t, start, end) }
                .sumOf { it.amount }
            BarData(label, total)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categoryBreakdown: StateFlow<List<CategoryData>> = allTransactions.map { list ->
        val (start, end) = monthRange(0)
        val expenses = list.filter { it.isExpense && transactionActiveInMonth(it, start, end) }
        val total = expenses.sumOf { it.amount }
        if (total == 0.0) emptyList()
        else expenses.groupBy { it.category }.map { (cat, txns) ->
            val catTotal = txns.sumOf { it.amount }
            CategoryData(cat, catTotal, (catTotal / total).toFloat())
        }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentMonthIncome: StateFlow<Double> = allTransactions
        .map { list ->
            val (start, end) = monthRange(0)
            list.filter { !it.isExpense && transactionActiveInMonth(it, start, end) }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val currentMonthExpenses: StateFlow<Double> = allTransactions
        .map { list ->
            val (start, end) = monthRange(0)
            list.filter { it.isExpense && transactionActiveInMonth(it, start, end) }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // offset: 0 = current month, 1 = 1 month ago (positive = past, for the downTo loop)
    private fun monthRange(offset: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, -offset)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        return start to cal.timeInMillis
    }

    private fun transactionActiveInMonth(t: TransactionEntity, start: Long, end: Long): Boolean {
        if (t.date >= end) return false
        if (!t.isRecurring) return t.date >= start
        if (t.endDate != null && t.endDate < start) return false

        val origCal = Calendar.getInstance().apply { timeInMillis = t.date }

        if (t.recurringFrequency == "Anual") {
            val monthCal = Calendar.getInstance().apply { timeInMillis = start }
            if (origCal.get(Calendar.MONTH) != monthCal.get(Calendar.MONTH)) return false
        }

        if (end <= System.currentTimeMillis()) return true

        val dueCal = Calendar.getInstance().apply {
            timeInMillis = start
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, minOf(origCal.get(Calendar.DAY_OF_MONTH), maxDay))
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return dueCal.timeInMillis <= System.currentTimeMillis()
    }
}
