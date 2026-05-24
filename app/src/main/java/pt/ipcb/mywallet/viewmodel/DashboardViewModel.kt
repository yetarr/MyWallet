package pt.ipcb.mywallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.ipcb.mywallet.data.SessionManager
import pt.ipcb.mywallet.data.local.AppDatabase
import pt.ipcb.mywallet.data.local.entity.TransactionEntity
import pt.ipcb.mywallet.data.local.entity.UserEntity
import pt.ipcb.mywallet.data.repository.TransactionRepository
import pt.ipcb.mywallet.data.repository.UserRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)
    private val transactionRepo = TransactionRepository(db.transactionDao())
    private val userRepo = UserRepository(db.userDao())
    private val session = SessionManager(app)
    val userId = session.userId

    val user: StateFlow<UserEntity?> = userRepo.getById(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // 0 = current month, -1 = one month ago, etc.
    val monthOffset = MutableStateFlow(0)

    private val allTransactions: StateFlow<List<TransactionEntity>> =
        transactionRepo.getAllByUser(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentTransactions: StateFlow<List<TransactionEntity>> =
        combine(allTransactions, monthOffset) { list, offset ->
            val (start, end) = monthRange(offset)
            list.filter { transactionActiveInMonth(it, start, end) }
                .map { t -> if (t.isRecurring && t.date < start) t.copy(date = projectedDate(t.date, start)) else t }
                .sortedByDescending { it.date }
                .take(5)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allTransactionsForMonth: StateFlow<List<TransactionEntity>> =
        combine(allTransactions, monthOffset) { list, offset ->
            val (start, end) = monthRange(offset)
            list.filter { transactionActiveInMonth(it, start, end) }
                .map { t -> if (t.isRecurring && t.date < start) t.copy(date = projectedDate(t.date, start)) else t }
                .sortedByDescending { it.date }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentMonthIncome: StateFlow<Double> =
        combine(allTransactions, monthOffset) { list, offset ->
            val (start, end) = monthRange(offset)
            list.filter { !it.isExpense }.sumOf { it.amount * countOccurrences(it, start, end) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val currentMonthExpenses: StateFlow<Double> =
        combine(allTransactions, monthOffset) { list, offset ->
            val (start, end) = monthRange(offset)
            list.filter { it.isExpense }.sumOf { it.amount * countOccurrences(it, start, end) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // Cumulative balance: all income minus all expenses from the beginning up to the
    // end of the selected month, counting each recurrence that has actually occurred.
    val totalBalance: StateFlow<Double> =
        combine(allTransactions, monthOffset) { list, offset ->
            val (_, end) = monthRange(offset)
            list.sumOf { t ->
                val sign = if (t.isExpense) -1.0 else 1.0
                sign * t.amount * countOccurrences(t, t.date, end)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val selectedMonthLabel: StateFlow<String> = monthOffset.map { offset ->
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, offset) }
        SimpleDateFormat("MMMM yyyy", Locale("pt")).format(cal.time)
            .replaceFirstChar { it.uppercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    // 0 = January … 11 = December of the current year
    private val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)

    // How many months are available in the current year (Jan → current month)
    val availableMonthCount = currentMonthIndex + 1

    // Which dot to highlight (0-based index within the year)
    val selectedMonthInYear: StateFlow<Int> = monthOffset.map { currentMonthIndex + it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), currentMonthIndex)

    fun prevMonth() { if (monthOffset.value > -currentMonthIndex) monthOffset.value-- }
    fun nextMonth() { if (monthOffset.value < 0) monthOffset.value++ }
    fun goToMonthIndex(idx: Int) { monthOffset.value = idx - currentMonthIndex }
    fun logout() = session.logout()

    fun deleteTransaction(txn: TransactionEntity) {
        viewModelScope.launch { transactionRepo.delete(txn) }
    }

    // Single-calendar implementation — avoids end-of-month edge cases
    private fun monthRange(offset: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, offset)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        return start to cal.timeInMillis
    }

    private fun transactionActiveInMonth(t: TransactionEntity, start: Long, end: Long): Boolean =
        countOccurrences(t, start, end) > 0

    // Returns the timestamp of this transaction projected onto the equivalent day
    // within monthStart's month (clamped to the last day of that month).
    private fun projectedDate(originalDate: Long, monthStart: Long): Long {
        val origCal = Calendar.getInstance().apply { timeInMillis = originalDate }
        return Calendar.getInstance().apply {
            timeInMillis = monthStart
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, minOf(origCal.get(Calendar.DAY_OF_MONTH), maxDay))
            set(Calendar.HOUR_OF_DAY, origCal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, origCal.get(Calendar.MINUTE))
            set(Calendar.SECOND, origCal.get(Calendar.SECOND))
            set(Calendar.MILLISECOND, origCal.get(Calendar.MILLISECOND))
        }.timeInMillis
    }

    // Normalizes a timestamp to midnight (00:00:00.000) in the local timezone.
    private fun midnightOf(ts: Long): Long = Calendar.getInstance().apply {
        timeInMillis = ts
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Counts how many occurrences of [t] fall within [from, to), capped at end-of-today
    // and at the transaction's own endDate so future/expired occurrences are never counted.
    private fun countOccurrences(t: TransactionEntity, from: Long, to: Long): Int {
        val tMidnight = midnightOf(t.date)
        // Exclusive upper bound: earliest of (to, tomorrow's midnight, day-after-endDate)
        val endCap = minOf(
            to,
            midnightOf(System.currentTimeMillis()) + 86_400_000L,
            t.endDate?.let { midnightOf(it) + 86_400_000L } ?: Long.MAX_VALUE
        )
        // Inclusive lower bound: latest of (from-midnight, creation-midnight)
        val startFrom = maxOf(midnightOf(from), tMidnight)
        if (startFrom >= endCap) return 0

        if (!t.isRecurring) return if (tMidnight >= midnightOf(from) && tMidnight < to) 1 else 0

        val origDay = Calendar.getInstance().apply { timeInMillis = t.date }.get(Calendar.DAY_OF_MONTH)
        var count = 0

        when (t.recurringFrequency) {
            "Diário" -> {
                // Every calendar day from startFrom up to (but not including) endCap
                val cur = Calendar.getInstance().apply { timeInMillis = startFrom }
                while (cur.timeInMillis < endCap) {
                    count++
                    cur.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            "Semanal" -> {
                // First occurrence at tMidnight, then every 7 days; skip forward to startFrom
                val cur = Calendar.getInstance().apply { timeInMillis = tMidnight }
                while (cur.timeInMillis < startFrom) cur.add(Calendar.WEEK_OF_YEAR, 1)
                while (cur.timeInMillis < endCap) {
                    count++
                    cur.add(Calendar.WEEK_OF_YEAR, 1)
                }
            }
            "Mensal" -> {
                // One occurrence per month on origDay; iterate month by month from creation month
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
                // One occurrence per year on the same month/day; skip forward to startFrom
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
