package com.nmk.fitlife.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nmk.fitlife.data.equipment.Equipment
import com.nmk.fitlife.data.equipment.EquipmentDao
import com.nmk.fitlife.data.exercise.Exercise
import com.nmk.fitlife.data.exercise.ExerciseDao
import com.nmk.fitlife.data.location.Location
import com.nmk.fitlife.data.location.LocationDao
import com.nmk.fitlife.data.user.User
import com.nmk.fitlife.data.user.UserDao
import com.nmk.fitlife.data.weekly_plan.WeeklyPlan
import com.nmk.fitlife.data.weekly_plan.WeeklyPlanDao
import com.nmk.fitlife.data.weekly_plan.WeeklyPlanWorkout
import com.nmk.fitlife.data.weekly_plan.WeeklyPlanWorkoutDao
import com.nmk.fitlife.data.workout.Workout
import com.nmk.fitlife.data.workout.WorkoutDao

@Database(
    entities = [
        User::class,
        WeeklyPlan::class,
        WeeklyPlanWorkout::class,
        Workout::class,
        Location::class,
        Exercise::class,
        Equipment::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun weeklyPlanDao(): WeeklyPlanDao
    abstract fun weeklyPlanWorkoutDao(): WeeklyPlanWorkoutDao
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitlife_db"
                )
                    .addCallback(AppDatabaseCallback(context))
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}