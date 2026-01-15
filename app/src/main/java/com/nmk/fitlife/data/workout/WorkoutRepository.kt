package com.nmk.fitlife.data.workout

import com.nmk.fitlife.data.equipment.Equipment
import com.nmk.fitlife.data.equipment.EquipmentDao
import com.nmk.fitlife.data.exercise.Exercise
import com.nmk.fitlife.data.exercise.ExerciseDao
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val equipmentDao: EquipmentDao
) {

    fun getTemplateWorkouts(): Flow<List<Workout>> = workoutDao.getTemplateWorkouts()

    suspend fun insertWorkoutWithDetails(
        workout: Workout,
        exercises: List<Exercise>,
        equipments: List<Equipment>
    ) {
        val workoutId = workoutDao.insert(workout).toInt()

        exercises.forEach {
            exerciseDao.insert(it.copy(workoutId = workoutId))
        }

        equipments.forEach {
            equipmentDao.insert(it.copy(workoutId = workoutId))
        }
    }
}