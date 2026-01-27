package com.nmk.fitlife

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nmk.fitlife.data.database.AppDatabase
import com.nmk.fitlife.data.equipment.EquipmentRepository
import com.nmk.fitlife.data.exercise.ExerciseRepository
import com.nmk.fitlife.data.weekly_plan.WeeklyPlanRepository
import com.nmk.fitlife.data.workout.WorkoutRepository
import com.nmk.fitlife.data.workout.WorkoutViewModel
import com.nmk.fitlife.data.workout.WorkoutViewModelFactory
import com.nmk.fitlife.service.getEndOfWeekDate
import com.nmk.fitlife.service.getStartOfWeekDate
import com.nmk.fitlife.ui.adapter.WeeklyWorkoutItemAdapter
import kotlinx.coroutines.launch

class MyRecordsActivity : AppCompatActivity() {
    private lateinit var rv: RecyclerView
    private lateinit var adapter: WeeklyWorkoutItemAdapter
    private val workoutViewModel: WorkoutViewModel by viewModels {
        val db = AppDatabase.getDatabase(this@MyRecordsActivity)
        WorkoutViewModelFactory(
            WorkoutRepository(
                db.workoutDao(),
                db.exerciseDao(),
                db.equipmentDao()
            ),
            ExerciseRepository(db.exerciseDao()),
            EquipmentRepository(db.equipmentDao()),
            WeeklyPlanRepository(db.weeklyPlanDao(), db.weeklyPlanWorkoutDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_records)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val authPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val AUTH_ID = authPrefs.getInt("id", 0)

        rv = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )


        lifecycleScope.launch {
            workoutViewModel.getWeeklyWorkouts(
                AUTH_ID,
                getStartOfWeekDate(),
                getEndOfWeekDate()
            ).collect { weeklyWorkouts ->
                adapter = WeeklyWorkoutItemAdapter(
                    this@MyRecordsActivity,
                    weeklyWorkouts
                ) {}
                rv.adapter = adapter
            }
        }
    }
}