package pt.ipcb.mywallet.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pt.ipcb.mywallet.data.local.entity.FamilyMemberEntity

@Dao
interface FamilyMemberDao {
    @Insert
    suspend fun insert(member: FamilyMemberEntity): Long

    @Update
    suspend fun update(member: FamilyMemberEntity)

    @Delete
    suspend fun delete(member: FamilyMemberEntity)

    @Query("SELECT * FROM family_members WHERE ownerId = :ownerId")
    fun getAllByOwner(ownerId: Int): Flow<List<FamilyMemberEntity>>
}
