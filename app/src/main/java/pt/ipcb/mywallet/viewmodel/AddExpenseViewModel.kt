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

    private val _existingTransaction = MutableStateFlow<TransactionEntity?>(null)
    val existingTransaction: StateFlow<TransactionEntity?> = _existingTransaction

    private var editId: Int? = null

    fun loadTransaction(id: Int) {
        editId = id
        viewModelScope.launch {
            _existingTransaction.value = repo.getById(id)
        }
    }

    fun save(
        name: String,
        amountStr: String,
        isExpense: Boolean,
        category: String,
        isRecurring: Boolean,
        recurringFrequency: String?,
        endDate: Long?,
        description: String,
        date: Long,
        locationName: String?,
    ) {
        val amount = amountStr.replace(",", ".").toDoubleOrNull() ?: return
        viewModelScope.launch {
            val entity = TransactionEntity(
                id = editId ?: 0,
                userId = session.userId,
                name = name.ifBlank { category },
                amount = amount,
                isExpense = isExpense,
                category = category,
                isRecurring = isRecurring,
                recurringFrequency = if (isRecurring) recurringFrequency else null,
                endDate = endDate,
                description = description,
                date = date,
                locationName = locationName,
            )
            if (editId != null) repo.update(entity) else repo.insert(entity)
            _saved.value = true
        }
    }

    fun resetSaved() { _saved.value = false }
}
