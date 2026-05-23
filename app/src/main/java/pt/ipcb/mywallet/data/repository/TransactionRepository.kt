package pt.ipcb.mywallet.data.repository

import kotlinx.coroutines.flow.Flow
import pt.ipcb.mywallet.data.local.dao.TransactionDao
import pt.ipcb.mywallet.data.local.entity.TransactionEntity

class TransactionRepository(private val dao: TransactionDao) {
    suspend fun insert(transaction: TransactionEntity): Long = dao.insert(transaction)
    suspend fun update(transaction: TransactionEntity) = dao.update(transaction)
    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)
    suspend fun getById(id: Int): TransactionEntity? = dao.getById(id)
    fun getAllByUser(userId: Int): Flow<List<TransactionEntity>> = dao.getAllByUser(userId)
    fun getRecentByUser(userId: Int, limit: Int = 5): Flow<List<TransactionEntity>> =
        dao.getRecentByUser(userId, limit)
}
