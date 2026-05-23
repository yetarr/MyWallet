package pt.ipcb.mywallet.data.repository

import kotlinx.coroutines.flow.Flow
import pt.ipcb.mywallet.data.local.dao.TransactionDao
import pt.ipcb.mywallet.data.local.entity.TransactionEntity

class TransactionRepository(private val dao: TransactionDao) {
    suspend fun insert(transaction: TransactionEntity): Long = dao.insert(transaction)
    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)
    fun getAllByUser(userId: Int): Flow<List<TransactionEntity>> = dao.getAllByUser(userId)
    fun getRecentByUser(userId: Int, limit: Int = 5): Flow<List<TransactionEntity>> =
        dao.getRecentByUser(userId, limit)
}
