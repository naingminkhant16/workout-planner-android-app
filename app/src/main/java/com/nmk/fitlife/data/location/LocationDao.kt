package com.nmk.fitlife.data.location

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: Location): Long

    @Query("SELECT * FROM locations WHERE userId=:userId")
    fun getByUserId(userId: Int): Flow<List<Location>>

    @Delete
    suspend fun delete(location: Location)
}