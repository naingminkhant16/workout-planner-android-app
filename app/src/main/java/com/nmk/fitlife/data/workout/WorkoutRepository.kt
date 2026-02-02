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

    fun getByUserId(userId: Int): Flow<List<Workout>> = workoutDao.getByUserId(userId)

    suspend fun getById(workoutId: Int): Workout? = workoutDao.getById(workoutId)

    fun getWeeklyWorkouts(
        userId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<WeeklyWorkoutDto>> = workoutDao.getWeeklyWorkoutList(
        userId,
        startDate,
        endDate
    )

    fun getWorkouts(
        userId: Int
    ): Flow<List<WeeklyWorkoutDto>> = workoutDao.getWorkoutList(
        userId
    )

    suspend fun insertWorkoutWithDetails(
        workout: Workout,
        exercises: List<Exercise>,
        equipments: List<Equipment>
    ): Int {
        val workoutId = workoutDao.insert(workout).toInt()

        exercises.forEach {
            exerciseDao.insert(it.copy(id = 0, workoutId = workoutId))
        }

        equipments.forEach {
            equipmentDao.insert(it.copy(id = 0, workoutId = workoutId))
        }
        return workoutId
    }

    suspend fun updateWorkoutWithDetails(
        workout: Workout,
        exercises: List<Exercise>,
        equipments: List<Equipment>
    ) {
        // Update workout
        workoutDao.update(workout)

        // Delete old exercises and equipments
        exerciseDao.deleteByWorkoutId(workout.id)
        equipmentDao.deleteByWorkoutId(workout.id)

        exercises.forEach {
            exerciseDao.insert(it.copy(workoutId = workout.id))
        }

        equipments.forEach {
            equipmentDao.insert(it.copy(workoutId = workout.id))
        }
    }

    suspend fun makeWorkoutAsCompleted(workoutId: Int) = workoutDao.markWorkoutCompleted(workoutId)

    suspend fun removeWorkoutFromWeeklyPlan(workoutId: Int) =
        workoutDao.removeWorkoutFromWeeklyPlan(workoutId)

    suspend fun deleteWorkout(workout: Workout) = workoutDao.delete(workout)
}