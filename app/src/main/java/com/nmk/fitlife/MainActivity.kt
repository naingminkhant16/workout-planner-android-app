package com.nmk.fitlife

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nmk.fitlife.data.database.AppDatabase
import com.nmk.fitlife.data.equipment.EquipmentRepository
import com.nmk.fitlife.data.exercise.ExerciseRepository
import com.nmk.fitlife.data.weekly_plan.WeeklyPlanRepository
import com.nmk.fitlife.data.workout.WeeklyWorkoutDto
import com.nmk.fitlife.data.workout.WorkoutRepository
import com.nmk.fitlife.data.workout.WorkoutViewModel
import com.nmk.fitlife.data.workout.WorkoutViewModelFactory
import com.nmk.fitlife.service.getEndOfWeekDate
import com.nmk.fitlife.service.getStartOfWeekDate
import com.nmk.fitlife.ui.adapter.TemplateWorkoutItemAdapter
import com.nmk.fitlife.ui.adapter.WeeklyWorkoutItemAdapter
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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
    private lateinit var weeklyWorkoutItemAdapter: WeeklyWorkoutItemAdapter
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
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(weeklyWorkoutRV)
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

                    weeklyWorkoutItemAdapter = WeeklyWorkoutItemAdapter(
                        this@MainActivity,
                        weeklyWorkouts
                    ) { workout ->
                        showShareOptions(workout)
                    }

                    weeklyWorkoutRV.adapter = weeklyWorkoutItemAdapter
                } else {
                    tvNoWorkout.visibility = View.VISIBLE
                    tvNoWorkout.setText(R.string.no_workout_fot_this_week_yet)
                }
            }
        }
    }

    private fun showShareOptions(weeklyWorkout: WeeklyWorkoutDto) {
        AlertDialog.Builder(this)
            .setTitle("Share Workout")
            .setItems(
                arrayOf(
                    "Share exercise list via SMS",
                    "Share equipment list via SMS"
                )
            ) { _, which ->
                when (which) {
                    0 -> shareExerciseList(weeklyWorkout)
                    1 -> shareEquipmentList(weeklyWorkout)
                }
            }
            .show()
    }

    private fun shareExerciseList(weeklyWorkoutDto: WeeklyWorkoutDto) {
        lifecycleScope.launch {
            val exercises = workoutViewModel.getExercisesByWorkoutId(weeklyWorkoutDto.workoutId)
                .filter { it.isNotEmpty() }
                .first()
            
            val message = buildString {
                append("🏋️ Workout: ${weeklyWorkoutDto.title}\n")
                append("📅 Day: ${weeklyWorkoutDto.dayOfWeek}\n\n")
                append("Exercises:\n")

                exercises.forEachIndexed { index, ex ->
                    append("${index + 1}. ${ex.name} - ${ex.sets}x${ex.reps}\n")
                }
            }
            sendSms(message)
        }
    }

    private fun shareEquipmentList(weeklyWorkoutDto: WeeklyWorkoutDto) {
        lifecycleScope.launch {
            val equipments =
                workoutViewModel.getEquipmentsByWorkoutId(weeklyWorkoutDto.workoutId)
                    .filter { it.isNotEmpty() }
                    .first()

            val message = buildString {
                append("🏋️ Workout: ${weeklyWorkoutDto.title}\n")
                append("📅 Day: ${weeklyWorkoutDto.dayOfWeek}\n\n")
                append("Equipment Needed:\n")

                equipments.forEachIndexed { index, eq ->
                    append("${index + 1}. ${eq.name}\n")
                }
            }
            sendSms(message)
        }
    }

    private fun sendSms(message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "smsto:".toUri()
            putExtra("sms_body", message)
        }
        startActivity(intent)
    }

    private fun initializeDates() {
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        tvStartDate.text = getStartOfWeekDate()
        tvEndDate.text = getEndOfWeekDate()
    }

    val swipeHandler =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            private val completeColor = "#4CAF50".toColorInt()
            private val deleteColor = "#F44336".toColorInt()
            private val background = ColorDrawable()


            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) return 0

                val workout = weeklyWorkoutItemAdapter.getItem(position)

                if (workout.isCompleted) {
                    return 0
                }

                return ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val currentWorkout = weeklyWorkoutItemAdapter.getItem(position)


                if (direction == ItemTouchHelper.RIGHT) {
                    // Make completed
                    workoutViewModel.makeWorkoutAsCompleted(currentWorkout.workoutId)

                    Toast.makeText(
                        this@MainActivity,
                        "Workout completed",
                        Toast.LENGTH_SHORT
                    ).show()

                } else if (direction == ItemTouchHelper.LEFT) {
                    // Remove from weekly plan
                    workoutViewModel.removeWorkoutFromWeeklyPlan(currentWorkout.workoutId)

                    Toast.makeText(
                        this@MainActivity,
                        "Removed from weekly plan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) return

                val workout = weeklyWorkoutItemAdapter.getItem(position)
                if (workout.isCompleted) return

                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - 64) / 2

                if (dX > 0) {
                    // RIGHT swipe (Complete)
                    background.color = completeColor
                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
                    background.draw(c)

                    val icon = ContextCompat.getDrawable(
                        recyclerView.context,
                        R.drawable.outline_check_24
                    )!!
                    val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = iconLeft + icon.intrinsicWidth
                    val iconBottom = iconTop + icon.intrinsicHeight

                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    icon.draw(c)

                } else if (dX < 0) {
                    // LEFT swipe (Delete)
                    background.color = deleteColor
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    background.draw(c)

                    val icon = ContextCompat.getDrawable(
                        recyclerView.context,
                        R.drawable.outline_delete_24
                    )!!
                    val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                    val iconRight = itemView.right - iconMargin
                    val iconLeft = iconRight - icon.intrinsicWidth
                    val iconBottom = iconTop + icon.intrinsicHeight

                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    icon.draw(c)
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
}