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

    @Query("SELECT * FROM workouts WHERE userId=:userId")
    fun getByUserId(userId: Int): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE id=:id")
    suspend fun getById(id: Int): Workout?

    @Query("SELECT * FROM workouts WHERE isTemplate=1")
    fun getTemplateWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE locationId=:locationId")
    fun getByLocationId(locationId: Int): Flow<List<Workout>>

    @Update
    suspend fun update(workout: Workout)

    @Delete
    suspend fun delete(workout: Workout)
}