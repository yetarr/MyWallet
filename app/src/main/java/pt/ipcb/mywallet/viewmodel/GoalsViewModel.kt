package pt.ipcb.mywallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.ipcb.mywallet.data.SessionManager
import pt.ipcb.mywallet.data.local.AppDatabase
import pt.ipcb.mywallet.data.local.entity.GoalEntity
import pt.ipcb.mywallet.data.repository.GoalRepository

class GoalsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = GoalRepository(AppDatabase.getInstance(app).goalDao())
    private val session = SessionManager(app)

    val goals: StateFlow<List<GoalEntity>> = repo.getAllByUser(session.userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addGoal(name: String, targetAmount: Double, savedAmount: Double, deadline: Long, iconType: String) {
        viewModelScope.launch {
            repo.insert(
                GoalEntity(
                    userId = session.userId,
                    name = name,
                    targetAmount = targetAmount,
                    savedAmount = savedAmount,
                    deadline = deadline,
                    iconType = iconType,
                )
            )
        }
    }

    fun addSavings(goal: GoalEntity, extra: Double) {
        viewModelScope.launch { repo.update(goal.copy(savedAmount = goal.savedAmount + extra)) }
    }

    fun delete(goal: GoalEntity) {
        viewModelScope.launch { repo.delete(goal) }
    }
}
