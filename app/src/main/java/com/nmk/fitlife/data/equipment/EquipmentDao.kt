package com.nmk.fitlife.data.equipment

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Insert
    suspend fun insert(equipment: Equipment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(equipment: List<Equipment>): List<Long>

    @Query("SELECT * FROM equipments WHERE workoutId=:workoutId")
    fun getByWorkoutId(workoutId: Int): Flow<List<Equipment>>

    @Delete
    suspend fun delete(equipment: Equipment)

    @Query("DELETE FROM equipments WHERE workoutId=:workoutId")
    suspend fun deleteByWorkoutId(workoutId: Int)
}