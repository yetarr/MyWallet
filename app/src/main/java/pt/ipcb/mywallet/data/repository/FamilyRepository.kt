package pt.ipcb.mywallet.data.repository

import kotlinx.coroutines.flow.Flow
import pt.ipcb.mywallet.data.local.dao.FamilyMemberDao
import pt.ipcb.mywallet.data.local.entity.FamilyMemberEntity

class FamilyRepository(private val dao: FamilyMemberDao) {
    suspend fun insert(member: FamilyMemberEntity): Long = dao.insert(member)
    suspend fun update(member: FamilyMemberEntity) = dao.update(member)
    suspend fun delete(member: FamilyMemberEntity) = dao.delete(member)
    fun getAllByOwner(ownerId: Int): Flow<List<FamilyMemberEntity>> = dao.getAllByOwner(ownerId)
}
