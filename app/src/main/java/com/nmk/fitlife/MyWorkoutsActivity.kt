package com.nmk.fitlife

import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
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
import com.nmk.fitlife.data.workout.WorkoutRepository
import com.nmk.fitlife.data.workout.WorkoutViewModel
import com.nmk.fitlife.data.workout.WorkoutViewModelFactory
import com.nmk.fitlife.ui.adapter.TemplateWorkoutItemAdapter
import kotlinx.coroutines.launch

class MyWorkoutsActivity : AppCompatActivity() {
    private lateinit var backIv: ImageView
    private lateinit var rv: RecyclerView
    private lateinit var templateWorkoutItemAdapter: TemplateWorkoutItemAdapter
    private val workoutViewModel: WorkoutViewModel by viewModels {
        val db = AppDatabase.getDatabase(this@MyWorkoutsActivity)
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
        setContentView(R.layout.activity_my_workouts)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backIv = findViewById(R.id.ivBack)
        rv = findViewById(R.id.rvMyWorkouts)
        rv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(rv)

        val authPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        val authId = authPrefs.getInt("id", 0)

        lifecycleScope.launch {
            workoutViewModel.getWorkoutsByUserId(authId).collect { workouts ->
                templateWorkoutItemAdapter = TemplateWorkoutItemAdapter(
                    this@MyWorkoutsActivity,
                    workouts
                )
                rv.adapter = templateWorkoutItemAdapter
            }
        }

        backIv.setOnClickListener {
            startActivity(Intent(this@MyWorkoutsActivity, MainActivity::class.java))
            finish()
        }
    }

    val swipeHandler =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return ItemTouchHelper.LEFT
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position = viewHolder.adapterPosition
                val workout = templateWorkoutItemAdapter.getItem(position)
                if (direction == ItemTouchHelper.LEFT) {

                    AlertDialog.Builder(this@MyWorkoutsActivity)
                        .setTitle("Are you sure?")
                        .setMessage("Do you really want to delete this workout?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            workoutViewModel.delete(workout)
                            dialog.dismiss()
                            templateWorkoutItemAdapter.notifyDataSetChanged()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                            templateWorkoutItemAdapter.notifyDataSetChanged()
                        }

                    Toast.makeText(this@MyWorkoutsActivity, "Workout deleted", Toast.LENGTH_SHORT)
                        .show()
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

                val deleteColor = "#F44336".toColorInt()
                val background = ColorDrawable()

                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - 64) / 2

                if (dX < 0) {
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