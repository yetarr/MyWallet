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
            list.filter { it.date in start..<end }.take(5)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allTransactionsForMonth: StateFlow<List<TransactionEntity>> =
        combine(allTransactions, monthOffset) { list, offset ->
            val (start, end) = monthRange(offset)
            list.filter { it.date in start..<end }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentMonthIncome: StateFlow<Double> =
        combine(allTransactions, monthOffset) { list, offset ->
            val (start, end) = monthRange(offset)
            list.filter { !it.isExpense && it.date in start..<end }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val currentMonthExpenses: StateFlow<Double> =
        combine(allTransactions, monthOffset) { list, offset ->
            val (start, end) = monthRange(offset)
            list.filter { it.isExpense && it.date in start..<end }.sumOf { it.amount }
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

    // Single-calendar implementation — avoids end-of-month edge cases across year boundaries
    private fun monthRange(offset: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, offset)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)          // advance the same instance by exactly 1 month
        return start to cal.timeInMillis
    }
}
