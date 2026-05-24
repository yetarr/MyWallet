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
            val total = list.filter { it.isExpense }
                .sumOf { it.amount * countOccurrences(it, start, end) }
            BarData(Formatters.monthLabel(start), total)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categoryBreakdown: StateFlow<List<CategoryData>> = allTransactions.map { list ->
        val (start, end) = monthRange(0)
        val categoryTotals = mutableMapOf<String, Double>()
        list.filter { it.isExpense }.forEach { t ->
            val c = countOccurrences(t, start, end)
            if (c > 0) categoryTotals[t.category] = (categoryTotals[t.category] ?: 0.0) + t.amount * c
        }
        val total = categoryTotals.values.sum()
        if (total == 0.0) emptyList()
        else categoryTotals.map { (cat, catTotal) ->
            CategoryData(cat, catTotal, (catTotal / total).toFloat())
        }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentMonthIncome: StateFlow<Double> = allTransactions.map { list ->
        val (start, end) = monthRange(0)
        list.filter { !it.isExpense }.sumOf { it.amount * countOccurrences(it, start, end) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val currentMonthExpenses: StateFlow<Double> = allTransactions.map { list ->
        val (start, end) = monthRange(0)
        list.filter { it.isExpense }.sumOf { it.amount * countOccurrences(it, start, end) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

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

    private fun midnightOf(ts: Long): Long = Calendar.getInstance().apply {
        timeInMillis = ts
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun countOccurrences(t: TransactionEntity, from: Long, to: Long): Int {
        val tMidnight = midnightOf(t.date)
        val endCap = minOf(
            to,
            midnightOf(System.currentTimeMillis()) + 86_400_000L,
            t.endDate?.let { midnightOf(it) + 86_400_000L } ?: Long.MAX_VALUE
        )
        val startFrom = maxOf(midnightOf(from), tMidnight)
        if (startFrom >= endCap) return 0

        if (!t.isRecurring) return if (tMidnight >= midnightOf(from) && tMidnight < to) 1 else 0

        val origDay = Calendar.getInstance().apply { timeInMillis = t.date }.get(Calendar.DAY_OF_MONTH)
        var count = 0

        when (t.recurringFrequency) {
            "Diário" -> {
                val cur = Calendar.getInstance().apply { timeInMillis = startFrom }
                while (cur.timeInMillis < endCap) {
                    count++
                    cur.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            "Semanal" -> {
                val cur = Calendar.getInstance().apply { timeInMillis = tMidnight }
                while (cur.timeInMillis < startFrom) cur.add(Calendar.WEEK_OF_YEAR, 1)
                while (cur.timeInMillis < endCap) {
                    count++
                    cur.add(Calendar.WEEK_OF_YEAR, 1)
                }
            }
            "Mensal" -> {
                val cur = Calendar.getInstance().apply {
                    timeInMillis = tMidnight
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                while (true) {
                    cur.set(Calendar.DAY_OF_MONTH, minOf(origDay, cur.getActualMaximum(Calendar.DAY_OF_MONTH)))
                    if (cur.timeInMillis >= endCap) break
                    if (cur.timeInMillis >= startFrom) count++
                    cur.set(Calendar.DAY_OF_MONTH, 1)
                    cur.add(Calendar.MONTH, 1)
                }
            }
            "Anual" -> {
                val cur = Calendar.getInstance().apply { timeInMillis = tMidnight }
                while (cur.timeInMillis < startFrom) {
                    cur.add(Calendar.YEAR, 1)
                    cur.set(Calendar.DAY_OF_MONTH, minOf(origDay, cur.getActualMaximum(Calendar.DAY_OF_MONTH)))
                }
                while (cur.timeInMillis < endCap) {
                    count++
                    cur.add(Calendar.YEAR, 1)
                    cur.set(Calendar.DAY_OF_MONTH, minOf(origDay, cur.getActualMaximum(Calendar.DAY_OF_MONTH)))
                }
            }
            else -> if (tMidnight >= startFrom && tMidnight < endCap) count = 1
        }
        return count
    }
}
