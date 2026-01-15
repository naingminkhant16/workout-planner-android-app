package com.nmk.fitlife.data.exercise

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insert(exercise: Exercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(exercise: List<Exercise>): List<Long>

    @Query("SELECT * FROM exercises WHERE workoutId=:workoutId")
    fun getByWorkoutId(workoutId: Int): Flow<List<Exercise>>

    @Update
    suspend fun update(exercise: Exercise)

    @Delete
    suspend fun delete(exercise: Exercise)
}