package com.nmk.fitlife.data.equipment

import kotlinx.coroutines.flow.Flow

class EquipmentRepository(private val equipmentDao: EquipmentDao) {
    fun getEquipmentsByWorkoutId(workoutId: Int): Flow<List<Equipment>> =
        equipmentDao.getByWorkoutId(workoutId)
}