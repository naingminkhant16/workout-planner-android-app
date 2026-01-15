package com.nmk.fitlife.data.exercise

import kotlinx.coroutines.flow.Flow

class ExerciseRepository(
    private val exerciseDao: ExerciseDao
) {
    fun getExercisesByWorkoutId(workoutId: Int): Flow<List<Exercise>> =
        exerciseDao.getByWorkoutId(workoutId)

}