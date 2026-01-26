package com.nmk.fitlife.data.weekly_plan

import com.nmk.fitlife.service.getEndOfWeekDate
import com.nmk.fitlife.service.getStartOfWeekDate
import kotlinx.coroutines.flow.Flow

class WeeklyPlanRepository(
    private val weeklyPlanDao: WeeklyPlanDao,
    private val weeklyPlanWorkoutDao: WeeklyPlanWorkoutDao
) {
    fun getByUserIdAndStartEndDates(userId: Int): Flow<List<WeeklyPlan>> =
        weeklyPlanDao.getByUserIdAndStartEndDates(
            userId,
            getStartOfWeekDate(),
            getEndOfWeekDate()
        )

    suspend fun createNewWeeklyPlan(userId: Int): Long {
        return weeklyPlanDao.insert(
            WeeklyPlan(
                userId = userId,
                startDate = getStartOfWeekDate(),
                endDate = getEndOfWeekDate()
            )
        )
    }

    suspend fun createWeeklyPlanWorkout(weeklyPlanWorkout: WeeklyPlanWorkout) {
        weeklyPlanWorkoutDao.insert(weeklyPlanWorkout)
    }
}