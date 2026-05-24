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
            list.filter { !it.isExpense && transactionActiveInMonth(it, start, end) }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val currentMonthExpenses: StateFlow<Double> =
        combine(allTransactions, monthOffset) { list, offset ->
            val (start, end) = monthRange(offset)
            list.filter { it.isExpense && transactionActiveInMonth(it, start, end) }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // Cumulative balance: all income minus all expenses from the beginning up to the
    // end of the selected month, counting each recurrence that has actually occurred.
    val totalBalance: StateFlow<Double> =
        combine(allTransactions, monthOffset) { list, offset ->
            val (_, end) = monthRange(offset)
            list.sumOf { t ->
                val sign = if (t.isExpense) -1.0 else 1.0
                sign * t.amount * occurrencesUpToMonthEnd(t, end)
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

    // Returns true if a transaction has an occurrence within [start, end) that has
    // already happened (i.e. the due day has passed for the current month, or the
    // entire month is in the past).
    private fun transactionActiveInMonth(t: TransactionEntity, start: Long, end: Long): Boolean {
        if (t.date >= end) return false
        if (!t.isRecurring) return t.date >= start
        if (t.endDate != null && t.endDate < start) return false

        val origCal = Calendar.getInstance().apply { timeInMillis = t.date }

        // Annual recurrences only apply in the same calendar month as the original
        if (t.recurringFrequency == "Anual") {
            val monthCal = Calendar.getInstance().apply { timeInMillis = start }
            if (origCal.get(Calendar.MONTH) != monthCal.get(Calendar.MONTH)) return false
        }

        // Past month: the full month elapsed, so the recurrence happened
        if (end <= System.currentTimeMillis()) return true

        // Current month: check if the due day has arrived (start-of-day comparison)
        val dueCal = Calendar.getInstance().apply {
            timeInMillis = start
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, minOf(origCal.get(Calendar.DAY_OF_MONTH), maxDay))
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return dueCal.timeInMillis <= System.currentTimeMillis()
    }

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

    // Counts how many times a transaction has actually occurred from its creation
    // up to endOfMonth, capped at today so future occurrences are never counted.
    private fun occurrencesUpToMonthEnd(t: TransactionEntity, endOfMonth: Long): Int {
        if (t.date >= endOfMonth) return 0
        if (!t.isRecurring) return 1

        val effectiveUpTo = minOf(
            t.endDate ?: Long.MAX_VALUE,
            endOfMonth - 1,
            System.currentTimeMillis()
        )
        if (effectiveUpTo < t.date) return 0

        val origCal = Calendar.getInstance().apply { timeInMillis = t.date }
        val origDay = origCal.get(Calendar.DAY_OF_MONTH)
        var count = 0

        if (t.recurringFrequency == "Anual") {
            val cur = Calendar.getInstance().apply {
                timeInMillis = t.date
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            while (cur.timeInMillis <= effectiveUpTo) {
                count++
                cur.add(Calendar.YEAR, 1)
                cur.set(Calendar.DAY_OF_MONTH, minOf(origDay, cur.getActualMaximum(Calendar.DAY_OF_MONTH)))
            }
        } else {
            // Mensal, Semanal, Diário — one occurrence per month on the original day-of-month
            val cur = Calendar.getInstance().apply {
                timeInMillis = t.date
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            while (true) {
                cur.set(Calendar.DAY_OF_MONTH, minOf(origDay, cur.getActualMaximum(Calendar.DAY_OF_MONTH)))
                if (cur.timeInMillis > effectiveUpTo) break
                if (cur.timeInMillis >= t.date) count++
                cur.set(Calendar.DAY_OF_MONTH, 1)
                cur.add(Calendar.MONTH, 1)
            }
        }
        return count
    }
}
