package com.nmk.fitlife.data.weekly_plan

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nmk.fitlife.data.user.User

@Entity(
    tableName = "weekly_plans",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class WeeklyPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,

    val startDate: String,

    val endDate: String
)
