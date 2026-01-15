package com.nmk.fitlife.data.database

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nmk.fitlife.data.equipment.Equipment
import com.nmk.fitlife.data.exercise.Exercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(context)

            TemplateData.defaultWorkouts().forEach { workout ->
                val workoutId = database.workoutDao().insert(workout).toInt()

                // Insert exercises and equipments for each workout
                val (exercises, equipments) = when (workout.title) {
                    "Full Body Beginner" -> {
                        TemplateData.exercisesForWorkoutTemplateA(workoutId) to
                                TemplateData.equipmentForWorkoutTemplateA(workoutId)
                    }

                    "Cardio Blast" -> {
                        TemplateData.exercisesForWorkoutTemplateB(workoutId) to
                                TemplateData.equipmentForWorkoutTemplateB(workoutId)
                    }

                    "Abs Workout Beginner" -> {
                        TemplateData.exercisesForWorkoutTemplateC(workoutId) to
                                TemplateData.equipmentForWorkoutTemplateC(workoutId)
                    }

                    else -> emptyList<Exercise>() to emptyList<Equipment>()
                }

                if (exercises.isNotEmpty()) {
                    database.exerciseDao().insertMany(exercises)
                }
                if (equipments.isNotEmpty()) {
                    database.equipmentDao().insertMany(equipments)
                }
            }
        }
    }
}