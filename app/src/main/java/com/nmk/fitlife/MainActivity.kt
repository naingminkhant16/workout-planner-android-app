package com.nmk.fitlife

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
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
import com.nmk.fitlife.data.workout.WorkoutRepository
import com.nmk.fitlife.data.workout.WorkoutViewModel
import com.nmk.fitlife.data.workout.WorkoutViewModelFactory
import com.nmk.fitlife.service.getEndOfWeekDate
import com.nmk.fitlife.service.getStartOfWeekDate
import com.nmk.fitlife.ui.adapter.TemplateWorkoutItemAdapter
import com.nmk.fitlife.ui.adapter.WeeklyWorkoutItemAdapter
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private lateinit var templateWorkoutRV: RecyclerView
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var weeklyWorkoutRV: RecyclerView
    private var AUTH_ID by Delegates.notNull<Int>()
    private lateinit var tvNoWorkout: TextView
    private val workoutViewModel: WorkoutViewModel by viewModels {
        val db = AppDatabase.getDatabase(this@MainActivity)
        WorkoutViewModelFactory(
            WorkoutRepository(
                db.workoutDao(),
                db.exerciseDao(),
                db.equipmentDao()
            ),
            ExerciseRepository(db.exerciseDao()),
            EquipmentRepository(db.equipmentDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(findViewById(R.id.toolBar))

        val authPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        AUTH_ID = authPrefs.getInt("id", 0)

        if (AUTH_ID == 0) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        tvNoWorkout = findViewById(R.id.tvNoWorkouts)

        initializeDates()

        loadRecyclerViews()

        loadTemplateWorkouts()

        loadWeeklyWorkouts()
    }

    private fun loadRecyclerViews() {
        // Recycler Views
        // Template workouts
        templateWorkoutRV = findViewById(R.id.rvTemplateWorkouts)
        templateWorkoutRV.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL,
            false
        )

        // Weekly workouts
        weeklyWorkoutRV = findViewById(R.id.rvWeeklyWorkouts)
        weeklyWorkoutRV.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
    }

    private fun loadTemplateWorkouts() {
        lifecycleScope.launch {
            workoutViewModel.templateWorkouts.collect { templateWorkouts ->
                templateWorkoutRV.adapter =
                    TemplateWorkoutItemAdapter(this@MainActivity, templateWorkouts)
            }
        }
    }

    private fun loadWeeklyWorkouts() {
        lifecycleScope.launch {
            workoutViewModel.getWeeklyWorkouts(
                AUTH_ID,
                getStartOfWeekDate(),
                getEndOfWeekDate()
            ).collect { weeklyWorkouts ->
                tvNoWorkout.visibility = View.GONE
                if (weeklyWorkouts.size > 0) {
                    weeklyWorkoutRV.adapter =
                        WeeklyWorkoutItemAdapter(this@MainActivity, weeklyWorkouts)
                } else {
                    tvNoWorkout.visibility = View.VISIBLE
                    tvNoWorkout.setText(R.string.no_workout_fot_this_week_yet)
                }
            }
        }
    }

    private fun initializeDates() {
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        tvStartDate.text = getStartOfWeekDate()
        tvEndDate.text = getEndOfWeekDate()
    }


}