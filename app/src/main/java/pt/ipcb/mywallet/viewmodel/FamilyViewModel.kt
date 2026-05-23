package pt.ipcb.mywallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.ipcb.mywallet.data.SessionManager
import pt.ipcb.mywallet.data.local.AppDatabase
import pt.ipcb.mywallet.data.local.entity.FamilyMemberEntity
import pt.ipcb.mywallet.data.repository.FamilyRepository

class FamilyViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FamilyRepository(AppDatabase.getInstance(app).familyMemberDao())
    private val session = SessionManager(app)

    val members: StateFlow<List<FamilyMemberEntity>> = repo.getAllByOwner(session.userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalIncome: StateFlow<Double> = members
        .map { list -> list.sumOf { it.income } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalExpenses: StateFlow<Double> = members
        .map { list -> list.sumOf { it.expenses } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun addMember(firstName: String, lastName: String, role: String, income: Double, expenses: Double, colorHex: String) {
        viewModelScope.launch {
            repo.insert(
                FamilyMemberEntity(
                    ownerId = session.userId,
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    role = role.trim(),
                    income = income,
                    expenses = expenses,
                    colorHex = colorHex,
                )
            )
        }
    }

    fun delete(member: FamilyMemberEntity) {
        viewModelScope.launch { repo.delete(member) }
    }
}
