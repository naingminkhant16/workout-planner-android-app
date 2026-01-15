package com.nmk.fitlife.data.equipment

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nmk.fitlife.data.workout.Workout

@Entity(
    tableName = "equipments",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutId")]
)
data class Equipment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val remark: String?,

    val workoutId: Int
)
