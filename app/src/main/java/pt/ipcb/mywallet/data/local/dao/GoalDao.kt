package pt.ipcb.mywallet.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pt.ipcb.mywallet.data.local.entity.GoalEntity

@Dao
interface GoalDao {
    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY deadline ASC")
    fun getAllByUser(userId: Int): Flow<List<GoalEntity>>
}
