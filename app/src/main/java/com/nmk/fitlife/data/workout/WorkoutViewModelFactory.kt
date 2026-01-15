package com.nmk.fitlife.data.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nmk.fitlife.data.equipment.EquipmentRepository
import com.nmk.fitlife.data.exercise.ExerciseRepository

class WorkoutViewModelFactory(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val equipmentRepository: EquipmentRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(workoutRepository, exerciseRepository, equipmentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    
}