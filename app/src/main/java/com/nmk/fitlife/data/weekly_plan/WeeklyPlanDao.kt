package com.nmk.fitlife.data.weekly_plan

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyPlanDao {
    @Insert
    suspend fun insert(plan: WeeklyPlan): Long

    @Query("SELECT * FROM weekly_plans WHERE userId=:userId AND startDate=:startDate AND endDate=:endDate")
    fun getByUserIdAndStartEndDates(
        userId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<WeeklyPlan>>

    @Delete
    suspend fun delete(plan: WeeklyPlan)
}