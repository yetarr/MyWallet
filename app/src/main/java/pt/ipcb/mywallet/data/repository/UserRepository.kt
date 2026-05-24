package pt.ipcb.mywallet.data.repository

import kotlinx.coroutines.flow.Flow
import pt.ipcb.mywallet.data.local.dao.UserDao
import pt.ipcb.mywallet.data.local.entity.UserEntity

class UserRepository(private val dao: UserDao) {
    suspend fun insert(user: UserEntity): Long = dao.insert(user)
    suspend fun update(user: UserEntity) = dao.update(user)
    suspend fun findByEmailAndPassword(email: String, password: String): UserEntity? =
        dao.findByEmailAndPassword(email, password)
    suspend fun findByEmail(email: String): UserEntity? = dao.findByEmail(email)
    fun getById(id: Int): Flow<UserEntity?> = dao.getById(id)
}
