package pt.ipcb.mywallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val name: String,
    val amount: Double,
    val isExpense: Boolean,
    val category: String,
    val isRecurring: Boolean = false,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val locationName: String? = null,
    val endDate: Long? = null,
)
