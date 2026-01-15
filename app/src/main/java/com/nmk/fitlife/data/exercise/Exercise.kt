package com.nmk.fitlife.data.exercise

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nmk.fitlife.data.workout.Workout

@Entity(
    tableName = "exercises",
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
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val workoutId: Int,
    
    val name: String,

    val sets: Int?,

    val reps: Int?,

    val instructions: String?,

    val imageUrl: String?,

    val isCompleted: Boolean = false
)
