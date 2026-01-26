package com.nmk.fitlife.data.workout

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insert(workout: Workout): Long

    @Query("SELECT * FROM workouts w WHERE w.userId=:userId AND w.isCompleted=0 AND NOT EXISTS(SELECT 1 FROM weekly_plan_workouts wpw WHERE wpw.workoutId=w.id)")
    fun getByUserId(userId: Int): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE id=:id")
    suspend fun getById(id: Int): Workout?

    @Query("SELECT * FROM workouts WHERE isTemplate=1")
    fun getTemplateWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE locationId=:locationId")
    fun getByLocationId(locationId: Int): Flow<List<Workout>>

    @Query(
        """
    SELECT 
        w.id AS workoutId,
        w.title,
        w.description,
        w.isCompleted,
        wpw.dayOfWeek,
        wp.startDate,
        wp.endDate
    FROM workouts w
    INNER JOIN weekly_plan_workouts wpw
        ON w.id = wpw.workoutId
    INNER JOIN weekly_plans wp
        ON wp.id = wpw.weeklyPlanId
    WHERE wp.userId = :userId
     AND startDate=:weekStart
     AND endDate=:weekEnd
    ORDER BY 
        CASE wpw.dayOfWeek
            WHEN 'Monday' THEN 1
            WHEN 'Tuesday' THEN 2
            WHEN 'Wednesday' THEN 3
            WHEN 'Thursday' THEN 4
            WHEN 'Friday' THEN 5
            WHEN 'Saturday' THEN 6
            WHEN 'Sunday' THEN 7
        END
    """
    )
    fun getWeeklyWorkoutList(
        userId: Int,
        weekStart: String,
        weekEnd: String
    ): Flow<List<WeeklyWorkoutDto>>


    @Update
    suspend fun update(workout: Workout)

    @Delete
    suspend fun delete(workout: Workout)
}