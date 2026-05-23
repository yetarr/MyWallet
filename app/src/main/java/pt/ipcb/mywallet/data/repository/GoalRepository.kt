package pt.ipcb.mywallet.data.repository

import kotlinx.coroutines.flow.Flow
import pt.ipcb.mywallet.data.local.dao.GoalDao
import pt.ipcb.mywallet.data.local.entity.GoalEntity

class GoalRepository(private val dao: GoalDao) {
    suspend fun insert(goal: GoalEntity): Long = dao.insert(goal)
    suspend fun update(goal: GoalEntity) = dao.update(goal)
    suspend fun delete(goal: GoalEntity) = dao.delete(goal)
    fun getAllByUser(userId: Int): Flow<List<GoalEntity>> = dao.getAllByUser(userId)
}
