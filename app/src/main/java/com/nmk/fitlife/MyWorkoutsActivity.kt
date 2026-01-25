package com.nmk.fitlife

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
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
import com.nmk.fitlife.ui.adapter.TemplateWorkoutItemAdapter
import kotlinx.coroutines.launch

class MyWorkoutsActivity : AppCompatActivity() {
    private lateinit var backIv: ImageView
    private lateinit var rv: RecyclerView
    private val workoutViewModel: WorkoutViewModel by viewModels {
        val db = AppDatabase.getDatabase(this@MyWorkoutsActivity)
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
        setContentView(R.layout.activity_my_workouts)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backIv = findViewById(R.id.ivBack)
        rv = findViewById(R.id.rvMyWorkouts)
        rv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        val authPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        val authId = authPrefs.getInt("id", 0)

        lifecycleScope.launch {
            workoutViewModel.getWorkoutsByUserId(authId).collect { workouts ->
                rv.adapter = TemplateWorkoutItemAdapter(this@MyWorkoutsActivity, workouts)
            }
        }

        backIv.setOnClickListener {
            startActivity(Intent(this@MyWorkoutsActivity, MainActivity::class.java))
            finish()
        }
    }
}