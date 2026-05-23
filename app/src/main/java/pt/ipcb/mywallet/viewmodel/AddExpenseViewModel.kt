package pt.ipcb.mywallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ipcb.mywallet.data.SessionManager
import pt.ipcb.mywallet.data.local.AppDatabase
import pt.ipcb.mywallet.data.local.entity.TransactionEntity
import pt.ipcb.mywallet.data.repository.TransactionRepository

class AddExpenseViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = TransactionRepository(AppDatabase.getInstance(app).transactionDao())
    private val session = SessionManager(app)

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    fun save(
        name: String,
        amountStr: String,
        isExpense: Boolean,
        category: String,
        isRecurring: Boolean,
        endDate: Long?,
        description: String,
        date: Long,
        locationName: String?,
    ) {
        val amount = amountStr.replace(",", ".").toDoubleOrNull() ?: return
        viewModelScope.launch {
            repo.insert(
                TransactionEntity(
                    userId = session.userId,
                    name = name.ifBlank { category },
                    amount = amount,
                    isExpense = isExpense,
                    category = category,
                    isRecurring = isRecurring,
                    endDate = endDate,
                    description = description,
                    date = date,
                    locationName = locationName,
                )
            )
            _saved.value = true
        }
    }

    fun resetSaved() { _saved.value = false }
}
