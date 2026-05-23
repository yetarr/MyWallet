package pt.ipcb.mywallet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pt.ipcb.mywallet.data.local.dao.FamilyMemberDao
import pt.ipcb.mywallet.data.local.dao.GoalDao
import pt.ipcb.mywallet.data.local.dao.TransactionDao
import pt.ipcb.mywallet.data.local.dao.UserDao
import pt.ipcb.mywallet.data.local.entity.FamilyMemberEntity
import pt.ipcb.mywallet.data.local.entity.GoalEntity
import pt.ipcb.mywallet.data.local.entity.TransactionEntity
import pt.ipcb.mywallet.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, TransactionEntity::class, GoalEntity::class, FamilyMemberEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun goalDao(): GoalDao
    abstract fun familyMemberDao(): FamilyMemberDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "mywallet_db")
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }

    }
}
