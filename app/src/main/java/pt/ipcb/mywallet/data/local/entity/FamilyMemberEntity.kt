package pt.ipcb.mywallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerId: Int,
    val firstName: String,
    val lastName: String,
    val role: String,
    val income: Double = 0.0,
    val expenses: Double = 0.0,
    val colorHex: String = "teal",
)
