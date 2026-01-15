package com.nmk.fitlife.data.workout

import androidx.room.Embedded
import androidx.room.Relation
import com.nmk.fitlife.data.equipment.Equipment
import com.nmk.fitlife.data.exercise.Exercise

data class WorkoutWithDetails(
    @Embedded
    val workout: Workout,

    @Relation(parentColumn = "id", entityColumn = "workoutId")
    val exercises: List<Exercise>,

    @Relation(parentColumn = "id", entityColumn = "workoutId")
    val equipments: List<Equipment>
)
