package com.nmk.fitlife

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
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
import com.nmk.fitlife.data.weekly_plan.WeeklyPlanRepository
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
    private lateinit var btnCreateCustomWorkout: Button
    private val workoutViewModel: WorkoutViewModel by viewModels {
        val db = AppDatabase.getDatabase(this@MainActivity)
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
        btnCreateCustomWorkout = findViewById(R.id.btnCreateCustomWorkout)

        initializeDates()

        loadRecyclerViews()

        loadTemplateWorkouts()

        loadWeeklyWorkouts()

        btnCreateCustomWorkout.setOnClickListener {
            startActivity(Intent(this, CreateCustomWorkoutActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tool_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_action_logout -> {
                val editor = getSharedPreferences("auth_prefs", MODE_PRIVATE).edit()
                editor.clear()
                editor.apply()

                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }

            R.id.item_my_workouts -> {
                startActivity(Intent(this, MyWorkoutsActivity::class.java))
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val logoutItem = menu?.findItem(R.id.item_action_logout)
        if (logoutItem != null) {
            val spanString = SpannableString(logoutItem.title.toString())
            spanString.setSpan(ForegroundColorSpan(Color.RED), 0, spanString.length, 0)
            logoutItem.title = spanString
        }

        val myWorkoutsItem = menu?.findItem(R.id.item_my_workouts)
        if (myWorkoutsItem != null) {
            val spanString = SpannableString(myWorkoutsItem.title.toString())
            spanString.setSpan(ForegroundColorSpan(Color.WHITE), 0, spanString.length, 0)
            myWorkoutsItem.title = spanString
        }

        return super.onPrepareOptionsMenu(menu)
    }

    private fun loadRecyclerViews() {
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
                println(weeklyWorkouts)
                if (weeklyWorkouts.isNotEmpty()) {
                    tvNoWorkout.visibility = View.GONE
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