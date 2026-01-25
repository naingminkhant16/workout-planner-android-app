package com.nmk.fitlife.data.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmk.fitlife.data.equipment.Equipment
import com.nmk.fitlife.data.equipment.EquipmentRepository
import com.nmk.fitlife.data.exercise.Exercise
import com.nmk.fitlife.data.exercise.ExerciseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val equipmentRepository: EquipmentRepository
) : ViewModel() {
    val templateWorkouts: StateFlow<List<Workout>> =
        workoutRepository.getTemplateWorkouts().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getWorkoutsByUserId(userId: Int): StateFlow<List<Workout>> {
        return workoutRepository.getByUserId(userId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun getWeeklyWorkouts(
        userId: Int,
        startDate: String,
        endDate: String
    ): StateFlow<List<WeeklyWorkoutDto>> {
        return workoutRepository.getWeeklyWorkouts(userId, startDate, endDate).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun createWorkoutFromTemplate(template: Workout) {
        viewModelScope.launch {
            val newWorkout = template.copy(
                id = 0,
                isTemplate = false,
                isCompleted = false,
                createdAt = System.currentTimeMillis().toString()
            )

            val exercises =
                exerciseRepository.getExercisesByWorkoutId(template.id).toList().flatten()

            val equipments =
                equipmentRepository.getEquipmentsByWorkoutId(template.id).toList().flatten()

            workoutRepository.insertWorkoutWithDetails(
                workout = newWorkout,
                exercises = exercises,
                equipments = equipments
            )
        }
    }

    fun createWorkout(workout: Workout, exercises: List<Exercise>, equipments: List<Equipment>) {
        viewModelScope.launch {
            workoutRepository.insertWorkoutWithDetails(workout, exercises, equipments)
        }
    }
}