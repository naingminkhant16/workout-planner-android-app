package com.nmk.fitlife.data.weekly_plan

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nmk.fitlife.data.workout.Workout

@Entity(
    tableName = "weekly_plan_workouts",
    foreignKeys = [
        ForeignKey(
            entity = WeeklyPlan::class,
            parentColumns = ["id"],
            childColumns = ["weeklyPlanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("weeklyPlanId"), Index("workoutId")]
)
data class WeeklyPlanWorkout(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val weeklyPlanId: Int,

    val workoutId: Int,

    val dayOfWeek: String
)
