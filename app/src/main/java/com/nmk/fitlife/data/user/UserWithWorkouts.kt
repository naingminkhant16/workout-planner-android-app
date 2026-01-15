package com.nmk.fitlife.data.user

import androidx.room.Embedded
import androidx.room.Relation
import com.nmk.fitlife.data.workout.Workout

data class UserWithWorkouts(
    @Embedded
    val user: User,

    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val workouts: List<Workout>
)
