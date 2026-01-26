package com.nmk.fitlife.data.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmk.fitlife.data.equipment.Equipment
import com.nmk.fitlife.data.equipment.EquipmentRepository
import com.nmk.fitlife.data.exercise.Exercise
import com.nmk.fitlife.data.exercise.ExerciseRepository
import com.nmk.fitlife.data.weekly_plan.WeeklyPlanRepository
import com.nmk.fitlife.data.weekly_plan.WeeklyPlanWorkout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val equipmentRepository: EquipmentRepository,
    private val weeklyPlanRepository: WeeklyPlanRepository
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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun createWorkoutFromTemplate(
        template: Workout,
        authId: Int,
        dayOfWeek: String
    ) {
        viewModelScope.launch {
            try {
                // Copy from template workout and create new
                val newWorkout = Workout(
                    title = template.title,
                    description = template.description,
                    userId = authId,
                    isTemplate = false,
                    isCompleted = false,
                    createdAt = System.currentTimeMillis().toString(),
                    locationId = null
                )

                // Get exercises and equipments from template workout
                val exercises = exerciseRepository
                    .getExercisesByWorkoutId(template.id)
                    .first()

                val equipments = equipmentRepository
                    .getEquipmentsByWorkoutId(template.id)
                    .first()

                // Insert workout with details
                val newWorkoutId = workoutRepository.insertWorkoutWithDetails(
                    workout = newWorkout,
                    exercises = exercises,
                    equipments = equipments
                )

                // Get current weekly plan
                val weeklyPlans = weeklyPlanRepository
                    .getByUserIdAndStartEndDates(authId)
                    .first()

                val weeklyPlanId = if (weeklyPlans.isEmpty())
                    weeklyPlanRepository.createNewWeeklyPlan(authId).toInt()
                else
                    weeklyPlans.first().id

                // Add workout to weekly plan
                weeklyPlanRepository.createWeeklyPlanWorkout(
                    WeeklyPlanWorkout(
                        weeklyPlanId = weeklyPlanId,
                        workoutId = newWorkoutId,
                        dayOfWeek = dayOfWeek
                    )
                )
            } catch (e: Exception) {
                println("Error during workout creation: ${e.message}")
                throw Exception(e)
            }
        }
    }


    fun addToWeeklyPlan(workout: Workout, authId: Int, dayOfWeek: String) {
        viewModelScope.launch {
            val weeklyPlans = weeklyPlanRepository
                .getByUserIdAndStartEndDates(authId)
                .first()

            val weeklyPlanId = if (weeklyPlans.isEmpty())
                weeklyPlanRepository.createNewWeeklyPlan(authId).toInt()
            else
                weeklyPlans.first().id

            weeklyPlanRepository.createWeeklyPlanWorkout(
                WeeklyPlanWorkout(
                    weeklyPlanId = weeklyPlanId,
                    workoutId = workout.id,
                    dayOfWeek = dayOfWeek
                )
            )
        }

    }


    fun createWorkout(workout: Workout, exercises: List<Exercise>, equipments: List<Equipment>) {
        viewModelScope.launch {
            workoutRepository.insertWorkoutWithDetails(workout, exercises, equipments)
        }
    }

    suspend fun getWorkoutById(workoutId: Int): Workout? = workoutRepository.getById(workoutId)

    fun getExercisesByWorkoutId(workoutId: Int): StateFlow<List<Exercise>> =
        exerciseRepository.getExercisesByWorkoutId(workoutId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getEquipmentsByWorkoutId(workoutId: Int): StateFlow<List<Equipment>> =
        equipmentRepository.getEquipmentsByWorkoutId(workoutId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun updateWorkoutWithDetails(
        updatedWorkout: Workout,
        exercises: List<Exercise>,
        equipments: List<Equipment>
    ) = workoutRepository.updateWorkoutWithDetails(updatedWorkout, exercises, equipments)

}