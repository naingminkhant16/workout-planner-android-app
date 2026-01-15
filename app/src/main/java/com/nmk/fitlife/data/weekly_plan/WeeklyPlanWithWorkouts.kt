package com.nmk.fitlife.data.weekly_plan

import androidx.room.Embedded
import androidx.room.Relation
import com.nmk.fitlife.data.workout.Workout

// Many To Many Joint Table
data class WeeklyPlanWithWorkouts(
    @Embedded
    val weeklyPlan: WeeklyPlan,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = androidx.room.Junction(
            value = WeeklyPlanWorkout::class,
            parentColumn = "weeklyPlanId",
            entityColumn = "workoutId"
        )
    )
    val workouts: List<Workout>
)
