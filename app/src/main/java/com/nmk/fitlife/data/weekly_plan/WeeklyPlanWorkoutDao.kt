package com.nmk.fitlife.data.weekly_plan

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyPlanWorkoutDao {
    @Insert
    suspend fun insert(link: WeeklyPlanWorkout)

    @Query(
        """
        SELECT * FROM weekly_plan_workouts 
        WHERE weeklyPlanId = :planId
    """
    )
    fun getWorkoutsForPlan(planId: Int): Flow<List<WeeklyPlanWorkout>>

    @Delete
    suspend fun delete(mapping: WeeklyPlanWorkout)
}