package com.nmk.fitlife.data.database

import com.nmk.fitlife.data.equipment.Equipment
import com.nmk.fitlife.data.exercise.Exercise
import com.nmk.fitlife.data.workout.Workout

object TemplateData {
    fun defaultWorkouts(): List<Workout> {
        return listOf(
            Workout(
                title = "Full Body Beginner",
                description = "Beginner full body workout",
                userId = null,
                isTemplate = true,
                isCompleted = false,
                locationId = null,
                createdAt = System.currentTimeMillis().toString(),
            ),
            Workout(
                title = "Cardio Blast",
                description = "High intensity cardio workout",
                userId = null,
                isTemplate = true,
                isCompleted = false,
                locationId = null,
                createdAt = System.currentTimeMillis().toString()
            ),
            Workout(
                title = "Abs Workout Beginner",
                description = "Abs workout for absolute beginner",
                userId = null,
                isTemplate = true,
                isCompleted = false,
                locationId = null,
                createdAt = System.currentTimeMillis().toString()
            )
        )
    }

    fun exercisesForWorkoutTemplateA(workoutId: Int): List<Exercise> {
        return listOf(
            Exercise(
                workoutId = workoutId,
                name = "Push Ups",
                sets = 3,
                reps = 12,
                instructions = "Keep your back straight",
                imageUrl = null
            ),
            Exercise(
                workoutId = workoutId,
                name = "Squats",
                sets = 3,
                reps = 15,
                instructions = "Lower hips until thighs are parallel",
                imageUrl = null
            )
        )
    }

    fun exercisesForWorkoutTemplateB(workoutId: Int): List<Exercise> {
        return listOf(
            Exercise(
                workoutId = workoutId,
                name = "Jumping Jacks",
                sets = 4,
                reps = 30,
                instructions = "Maintain a fast pace and stay on your toes.",
                imageUrl = null
            ),
            Exercise(
                workoutId = workoutId,
                name = "Burpees",
                sets = 3,
                reps = 10,
                instructions = "Chest to the floor, then jump explosively at the top.",
                imageUrl = null
            ),
            Exercise(
                workoutId = workoutId,
                name = "Mountain Climbers",
                sets = 3,
                reps = 20,
                instructions = "Keep your core tight and drive knees toward your chest.",
                imageUrl = null
            )
        )
    }

    fun exercisesForWorkoutTemplateC(workoutId: Int): List<Exercise> {
        return listOf(
            Exercise(
                workoutId = workoutId,
                name = "Crunches",
                sets = 3,
                reps = 15,
                instructions = "Lift shoulders off the floor using only your abs.",
                imageUrl = null
            ),
            Exercise(
                workoutId = workoutId,
                name = "Sit-ups",
                sets = 3,
                reps = 15,
                instructions = "Lie flat on your back with knees bent at about 90 degrees, feet flat on the floor, and arms either crossed on your chest or hands gently behind your head.",
                imageUrl = null
            ),
            Exercise(
                workoutId = workoutId,
                name = "Leg Raises",
                sets = 3,
                reps = 12,
                instructions = "Lower your legs slowly without touching the ground.",
                imageUrl = null
            )
        )
    }

    fun equipmentForWorkoutTemplateA(workoutId: Int): List<Equipment> {
        // Full Body Beginner usually requires minimal gear
        return listOf(
            Equipment(
                name = "Yoga Mat",
                remark = "For floor comfort during push ups",
                workoutId = workoutId
            ),
            Equipment(
                name = "Sturdy Chair",
                remark = "Optional: for balance during squats",
                workoutId = workoutId
            )
        )
    }

    fun equipmentForWorkoutTemplateB(workoutId: Int): List<Equipment> {
        // Cardio Blast
        return listOf(
            Equipment(
                name = "Jump Rope",
                remark = "Used for warm-up or high intensity intervals",
                workoutId = workoutId
            ),
            Equipment(
                name = "Stopwatch",
                remark = "To track rest intervals",
                workoutId = workoutId
            )
        )
    }

    fun equipmentForWorkoutTemplateC(workoutId: Int): List<Equipment> {
        // Abs Workout Beginner
        return listOf(
            Equipment(
                name = "Exercise Mat",
                remark = "Essential for back support during leg raises",
                workoutId = workoutId
            ),
            Equipment(
                name = "Small Towel",
                remark = "Can be used for neck support during crunches",
                workoutId = workoutId
            )
        )
    }
}
