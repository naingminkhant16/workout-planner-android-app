package com.nmk.fitlife.data.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nmk.fitlife.data.equipment.EquipmentRepository
import com.nmk.fitlife.data.exercise.ExerciseRepository
import com.nmk.fitlife.data.weekly_plan.WeeklyPlanRepository

class WorkoutViewModelFactory(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val equipmentRepository: EquipmentRepository,
    private val weeklyPlanRepository: WeeklyPlanRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(
                workoutRepository,
                exerciseRepository,
                equipmentRepository,
                weeklyPlanRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}